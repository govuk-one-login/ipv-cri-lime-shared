package uk.gov.account.ipv.cri.lime.limeade.http.retryer2;

import uk.gov.account.ipv.cri.lime.limeade.service.http.retryer2.ApacheCloseableHttpRetryer2;
import uk.gov.account.ipv.cri.lime.limeade.service.http.retryer2.HttpRetryer2MetricsUtil;
import uk.gov.account.ipv.cri.lime.limeade.testfixtures.HttpResponseFixtures;
import uk.gov.account.ipv.cri.lime.limeade.testfixtures.HttpRetryer2ConfigTestFixture;
import uk.gov.account.ipv.cri.lime.limeade.testfixtures.MetricsProbeTestHelper;
import uk.gov.account.ipv.cri.lime.limeade.util.metrics.MetricsProbe;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudwatchlogs.emf.model.DimensionSet;
import software.amazon.cloudwatchlogs.emf.model.Unit;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApacheCloseableHttpRetryer2Test {

    @Mock private CloseableHttpClient mockCloseableHttpClient;
    @Mock private MetricsProbe mockMetricsProbe;
    @Mock private HttpPost mockPostRequest;

    private static final String TEST_ENDPOINT_NAME = "unit_test_endpoint";
    private static final List<Integer> TEST_RETRY_STATUS_CODES = List.of(300, 400, 500);
    private static final List<Integer> TEST_SUCCESS_STATUS_CODES = List.of(200, 201, 202);

    private static final long TEST_LATENCY_EPSILON_MS = 500;

    @ParameterizedTest
    @CsvSource({
        "200, false, 1", // No Retry Expected / MaxRetries 1
        "201, false, 2", // No Retry Expected / MaxRetries 2
        "202, false, 3", // No Retry Expected / MaxRetries 3
        "300, true, 1", // Retry Expected / MaxRetries 1
        "400, true, 2", // Retry Expected / MaxRetries 2
        "500, true, 3", // Retry Expected / MaxRetries 3
    })
    void shouldOnlyRetryWhenStatusIsAMatchingRetryCode(
            int statusCode, boolean retryExpected, int testMaxRetries) throws IOException {

        ApacheCloseableHttpRetryer2 httpRetryer2 =
                new ApacheCloseableHttpRetryer2(
                        configFixtureWithRetries(testMaxRetries),
                        mockCloseableHttpClient,
                        mockMetricsProbe);

        CloseableHttpResponse testCloseableHttpResponse =
                HttpResponseFixtures.createHttpResponse(statusCode, null, "", false);

        when(mockCloseableHttpClient.execute(any(HttpPost.class)))
                .thenReturn(testCloseableHttpResponse);

        httpRetryer2.sendHTTPRequestRetryIfAllowed(mockPostRequest);

        ArgumentCaptor<String> metricNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<DimensionSet> dimensionSetArgumentCaptor =
                ArgumentCaptor.forClass(DimensionSet.class);
        ArgumentCaptor<Double> valueArgumentCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Unit> unitArgumentCaptor = ArgumentCaptor.forClass(Unit.class);
        ArgumentCaptor.forClass(Unit.class);

        InOrder inOrderMockMetricsProbeSequence = inOrder(mockMetricsProbe);

        int mockHttpClientExpectedTimes = 1; // 1 for the initial attempt
        if (retryExpected) {
            mockHttpClientExpectedTimes += testMaxRetries;

            inOrderMockMetricsProbeSequence
                    .verify(mockMetricsProbe, times((testMaxRetries * 2) + 2))
                    .captureMetric(
                            metricNameCaptor.capture(),
                            dimensionSetArgumentCaptor.capture(),
                            valueArgumentCaptor.capture(),
                            unitArgumentCaptor.capture());

            checkSendStatusCodeRetryThenMaxRetriesMetricSequence(
                    testMaxRetries + 1,
                    statusCode,
                    metricNameCaptor,
                    dimensionSetArgumentCaptor,
                    valueArgumentCaptor,
                    unitArgumentCaptor);
        } else {
            inOrderMockMetricsProbeSequence
                    .verify(mockMetricsProbe, times(2))
                    .captureMetric(
                            metricNameCaptor.capture(),
                            dimensionSetArgumentCaptor.capture(),
                            valueArgumentCaptor.capture(),
                            unitArgumentCaptor.capture());

            MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                    0,
                    "http_retryer",
                    DimensionSet.of(
                            HttpRetryer2MetricsUtil.SendSuccessMetric.DIMENSION_ENDPOINT_NAME,
                            TEST_ENDPOINT_NAME,
                            HttpRetryer2MetricsUtil.SendSuccessMetric
                                    .DIMENSION_RESPONSE_STATUS_CODE,
                            String.valueOf(statusCode)),
                    1,
                    TEST_LATENCY_EPSILON_MS,
                    Unit.MILLISECONDS,
                    metricNameCaptor,
                    dimensionSetArgumentCaptor,
                    valueArgumentCaptor,
                    unitArgumentCaptor);

            MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                    1,
                    "http_retryer",
                    DimensionSet.of(
                            HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.DIMENSION_ENDPOINT_NAME,
                            TEST_ENDPOINT_NAME,
                            HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.DIMENSION_SEND_OUTCOME,
                            HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.SEND_OK
                                    .toString()),
                    1,
                    0,
                    Unit.COUNT,
                    metricNameCaptor,
                    dimensionSetArgumentCaptor,
                    valueArgumentCaptor,
                    unitArgumentCaptor);
        }
        verifyNoMoreInteractions(mockMetricsProbe);
        inOrderMockMetricsProbeSequence.verifyNoMoreInteractions();

        verify(mockCloseableHttpClient, times(mockHttpClientExpectedTimes))
                .execute(any(HttpPost.class));
        verifyNoMoreInteractions(mockCloseableHttpClient);
    }

    @ParameterizedTest
    @CsvSource({
        "IOException, false, 1", // No Retry / MaxRetries 1
        "ConnectTimeoutException, true, 2", // Retry Expected / MaxRetries 2
        "SocketTimeoutException, true, 3", // Retry Expected / MaxRetries 3
    })
    void shouldOnlyRetryWhenIOExceptionIsARetryableException(
            String exceptionToThrow, boolean retryExpected, int testMaxRetries) throws IOException {

        ApacheCloseableHttpRetryer2 httpRetryer2 =
                new ApacheCloseableHttpRetryer2(
                        configFixtureWithRetries(testMaxRetries),
                        mockCloseableHttpClient,
                        mockMetricsProbe);

        final Exception exception;

        if (exceptionToThrow.equals("ConnectTimeoutException")) {
            exception = new ConnectTimeoutException("TestConnectTimeoutException");
        } else if (exceptionToThrow.equals("SocketTimeoutException")) {
            exception = new SocketTimeoutException("TestSocketTimeoutException");
        } else {
            exception = new IOException("TestGeneralIOException");
        }

        when(mockCloseableHttpClient.execute(any(HttpPost.class))).thenThrow(exception);

        IOException thrownException =
                assertThrows(
                        IOException.class,
                        () -> httpRetryer2.sendHTTPRequestRetryIfAllowed(mockPostRequest),
                        "Expected IOException");

        ArgumentCaptor<String> metricNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<DimensionSet> dimensionSetArgumentCaptor =
                ArgumentCaptor.forClass(DimensionSet.class);
        ArgumentCaptor<Double> valueArgumentCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Unit> unitArgumentCaptor = ArgumentCaptor.forClass(Unit.class);
        ArgumentCaptor.forClass(Unit.class);

        InOrder inOrderMockMetricsProbeSequence = inOrder(mockMetricsProbe);

        int mockHttpClientExpectedTimes = 1;
        if (retryExpected) {
            mockHttpClientExpectedTimes += testMaxRetries;

            inOrderMockMetricsProbeSequence
                    .verify(mockMetricsProbe, times((testMaxRetries * 2) + 2))
                    .captureMetric(
                            metricNameCaptor.capture(),
                            dimensionSetArgumentCaptor.capture(),
                            valueArgumentCaptor.capture(),
                            unitArgumentCaptor.capture());

            checkSendExceptionRetryThenMaxRetriesMetricSequence(
                    mockHttpClientExpectedTimes,
                    thrownException,
                    metricNameCaptor,
                    dimensionSetArgumentCaptor,
                    valueArgumentCaptor,
                    unitArgumentCaptor);

            if (exceptionToThrow.equals("ConnectTimeoutException")) {
                assertInstanceOf(ConnectTimeoutException.class, thrownException);
            } else if (exceptionToThrow.equals("SocketTimeoutException")) {
                assertInstanceOf(SocketTimeoutException.class, thrownException);
            }
        } else {
            inOrderMockMetricsProbeSequence
                    .verify(mockMetricsProbe, times(2))
                    .captureMetric(
                            metricNameCaptor.capture(),
                            dimensionSetArgumentCaptor.capture(),
                            valueArgumentCaptor.capture(),
                            unitArgumentCaptor.capture());

            MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                    0,
                    "http_retryer",
                    DimensionSet.of(
                            HttpRetryer2MetricsUtil.SendFailureMetric.DIMENSION_ENDPOINT_NAME,
                            TEST_ENDPOINT_NAME,
                            HttpRetryer2MetricsUtil.SendFailureMetric.DIMENSION_FAILURE_CAUSE,
                            thrownException.getClass().getSimpleName()),
                    1,
                    TEST_LATENCY_EPSILON_MS,
                    Unit.MILLISECONDS,
                    metricNameCaptor,
                    dimensionSetArgumentCaptor,
                    valueArgumentCaptor,
                    unitArgumentCaptor);

            MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                    1,
                    "http_retryer",
                    DimensionSet.of(
                            HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.DIMENSION_ENDPOINT_NAME,
                            TEST_ENDPOINT_NAME,
                            HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.DIMENSION_SEND_OUTCOME,
                            "non_retryable_exception"), // Fatal
                    1,
                    0,
                    Unit.COUNT,
                    metricNameCaptor,
                    dimensionSetArgumentCaptor,
                    valueArgumentCaptor,
                    unitArgumentCaptor);

            assertInstanceOf(IOException.class, thrownException);
        }
        verifyNoMoreInteractions(mockMetricsProbe);
        inOrderMockMetricsProbeSequence.verifyNoMoreInteractions();

        verify(mockCloseableHttpClient, times(mockHttpClientExpectedTimes))
                .execute(any(HttpPost.class));
        verifyNoMoreInteractions(mockCloseableHttpClient);
    }

    @Test
    void shouldCaptureSendErrorMetricIfRemoteAPIReturnsNonRetryableStatusDuringARetry()
            throws IOException {

        int expectedHttpCalls = 2;

        ApacheCloseableHttpRetryer2 httpRetryer2 =
                new ApacheCloseableHttpRetryer2(
                        configFixtureWithRetries(3), mockCloseableHttpClient, mockMetricsProbe);

        CloseableHttpResponse initialRetryableCloseableHttpResponse =
                HttpResponseFixtures.createHttpResponse(500, null, "", false);

        CloseableHttpResponse nonRetryableCloseableHttpResponse =
                HttpResponseFixtures.createHttpResponse(999, null, "", false);

        when(mockCloseableHttpClient.execute(any(HttpPost.class)))
                .thenReturn(initialRetryableCloseableHttpResponse)
                .thenReturn(nonRetryableCloseableHttpResponse);

        httpRetryer2.sendHTTPRequestRetryIfAllowed(mockPostRequest);

        ArgumentCaptor<String> metricNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<DimensionSet> dimensionSetArgumentCaptor =
                ArgumentCaptor.forClass(DimensionSet.class);
        ArgumentCaptor<Double> valueArgumentCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Unit> unitArgumentCaptor = ArgumentCaptor.forClass(Unit.class);
        ArgumentCaptor.forClass(Unit.class);

        InOrder inOrderMockMetricsProbeSequence = inOrder(mockMetricsProbe);
        inOrderMockMetricsProbeSequence
                .verify(mockMetricsProbe, times(expectedHttpCalls + 2))
                .captureMetric(
                        metricNameCaptor.capture(),
                        dimensionSetArgumentCaptor.capture(),
                        valueArgumentCaptor.capture(),
                        unitArgumentCaptor.capture());

        MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                0,
                "http_retryer",
                DimensionSet.of(
                        HttpRetryer2MetricsUtil.SendSuccessMetric.DIMENSION_ENDPOINT_NAME,
                        TEST_ENDPOINT_NAME,
                        HttpRetryer2MetricsUtil.SendSuccessMetric.DIMENSION_RESPONSE_STATUS_CODE,
                        String.valueOf(500)),
                1,
                TEST_LATENCY_EPSILON_MS,
                Unit.MILLISECONDS,
                metricNameCaptor,
                dimensionSetArgumentCaptor,
                valueArgumentCaptor,
                unitArgumentCaptor);

        MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                1,
                "http_retryer",
                DimensionSet.of(
                        HttpRetryer2MetricsUtil.SendRetryMetric.DIMENSION_ENDPOINT_NAME,
                        TEST_ENDPOINT_NAME,
                        HttpRetryer2MetricsUtil.SendRetryMetric.DIMENSION_TRY_COUNT,
                        String.valueOf(1)),
                1,
                0,
                Unit.COUNT,
                metricNameCaptor,
                dimensionSetArgumentCaptor,
                valueArgumentCaptor,
                unitArgumentCaptor);

        MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                2,
                "http_retryer",
                DimensionSet.of(
                        HttpRetryer2MetricsUtil.SendSuccessMetric.DIMENSION_ENDPOINT_NAME,
                        TEST_ENDPOINT_NAME,
                        HttpRetryer2MetricsUtil.SendSuccessMetric.DIMENSION_RESPONSE_STATUS_CODE,
                        String.valueOf(999)),
                1,
                TEST_LATENCY_EPSILON_MS,
                Unit.MILLISECONDS,
                metricNameCaptor,
                dimensionSetArgumentCaptor,
                valueArgumentCaptor,
                unitArgumentCaptor);

        MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                3,
                "http_retryer",
                DimensionSet.of(
                        HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.DIMENSION_ENDPOINT_NAME,
                        TEST_ENDPOINT_NAME,
                        HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.DIMENSION_SEND_OUTCOME,
                        "non_retryable_status"),
                1,
                0,
                Unit.COUNT,
                metricNameCaptor,
                dimensionSetArgumentCaptor,
                valueArgumentCaptor,
                unitArgumentCaptor);

        verifyNoMoreInteractions(mockMetricsProbe);
        inOrderMockMetricsProbeSequence.verifyNoMoreInteractions();
        verify(mockCloseableHttpClient, times(expectedHttpCalls)).execute(any(HttpPost.class));
        verifyNoMoreInteractions(mockCloseableHttpClient);
    }

    private void checkSendStatusCodeRetryThenMaxRetriesMetricSequence(
            int maxSequence,
            int statusCode,
            ArgumentCaptor<String> metricNameCaptor,
            ArgumentCaptor<DimensionSet> dimensionSetArgumentCaptor,
            ArgumentCaptor<Double> valueArgumentCaptor,
            ArgumentCaptor<Unit> unitArgumentCaptor) {

        int retry = 0;
        int metric = 0;

        // Checks for a Sequence of
        // captureHttpRetryerSendMetric followed by a captureHttpRetryerSendRetryMetric
        // With the last in the sequence being
        // captureHttpRetryerSendMetric followed by a captureHttpRetryerFinalSendMetric
        // (MAX_RETRIES_REACHED)

        for (int sequence = 0; sequence < maxSequence; sequence++) {

            System.out.println(sequence);

            MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                    0,
                    "http_retryer",
                    DimensionSet.of(
                            HttpRetryer2MetricsUtil.SendSuccessMetric.DIMENSION_ENDPOINT_NAME,
                            TEST_ENDPOINT_NAME,
                            HttpRetryer2MetricsUtil.SendSuccessMetric
                                    .DIMENSION_RESPONSE_STATUS_CODE,
                            String.valueOf(statusCode)),
                    1,
                    TEST_LATENCY_EPSILON_MS,
                    Unit.MILLISECONDS,
                    metricNameCaptor,
                    dimensionSetArgumentCaptor,
                    valueArgumentCaptor,
                    unitArgumentCaptor);

            metric++;

            boolean last = (sequence + 1 >= maxSequence);
            if (!last) {
                MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                        metric,
                        "http_retryer",
                        DimensionSet.of(
                                HttpRetryer2MetricsUtil.SendRetryMetric.DIMENSION_ENDPOINT_NAME,
                                TEST_ENDPOINT_NAME,
                                HttpRetryer2MetricsUtil.SendRetryMetric.DIMENSION_TRY_COUNT,
                                String.valueOf(retry = retry + 1)),
                        1,
                        0,
                        Unit.COUNT,
                        metricNameCaptor,
                        dimensionSetArgumentCaptor,
                        valueArgumentCaptor,
                        unitArgumentCaptor);

            } else {
                MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                        metric++,
                        "http_retryer",
                        DimensionSet.of(
                                HttpRetryer2MetricsUtil.FinalSendOutcomeMetric
                                        .DIMENSION_ENDPOINT_NAME,
                                TEST_ENDPOINT_NAME,
                                HttpRetryer2MetricsUtil.FinalSendOutcomeMetric
                                        .DIMENSION_SEND_OUTCOME,
                                HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.MAX_RETRIES_REACHED
                                        .toString()),
                        1,
                        0,
                        Unit.COUNT,
                        metricNameCaptor,
                        dimensionSetArgumentCaptor,
                        valueArgumentCaptor,
                        unitArgumentCaptor);
            }
            metric++;
        }
    }

    private void checkSendExceptionRetryThenMaxRetriesMetricSequence(
            int maxSequence,
            Exception caughtIOException,
            ArgumentCaptor<String> metricNameCaptor,
            ArgumentCaptor<DimensionSet> dimensionSetArgumentCaptor,
            ArgumentCaptor<Double> valueArgumentCaptor,
            ArgumentCaptor<Unit> unitArgumentCaptor) {

        int retry = 0;
        int metric = 0;

        // Checks for a Sequence of
        // captureHttpRetryerSendMetric followed by a captureHttpRetryerSendRetryMetric
        // With the last in the sequence being
        // captureHttpRetryerSendMetric followed by a captureHttpRetryerFinalSendMetric
        // (MAX_RETRIES_REACHED)

        for (int sequence = 0; sequence < maxSequence; sequence++) {

            System.out.println(sequence);

            MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                    metric,
                    "http_retryer",
                    DimensionSet.of(
                            HttpRetryer2MetricsUtil.SendFailureMetric.DIMENSION_ENDPOINT_NAME,
                            TEST_ENDPOINT_NAME,
                            HttpRetryer2MetricsUtil.SendFailureMetric.DIMENSION_FAILURE_CAUSE,
                            caughtIOException.getClass().getSimpleName()),
                    1,
                    TEST_LATENCY_EPSILON_MS,
                    Unit.MILLISECONDS,
                    metricNameCaptor,
                    dimensionSetArgumentCaptor,
                    valueArgumentCaptor,
                    unitArgumentCaptor);

            metric++;

            boolean last = (sequence + 1 >= maxSequence);
            if (!last) {
                MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                        metric,
                        "http_retryer",
                        DimensionSet.of(
                                HttpRetryer2MetricsUtil.SendRetryMetric.DIMENSION_ENDPOINT_NAME,
                                TEST_ENDPOINT_NAME,
                                HttpRetryer2MetricsUtil.SendRetryMetric.DIMENSION_TRY_COUNT,
                                String.valueOf(retry = retry + 1)),
                        1,
                        0,
                        Unit.COUNT,
                        metricNameCaptor,
                        dimensionSetArgumentCaptor,
                        valueArgumentCaptor,
                        unitArgumentCaptor);

            } else {
                MetricsProbeTestHelper.metricsProbeArgumentVerifier(
                        metric++,
                        "http_retryer",
                        DimensionSet.of(
                                HttpRetryer2MetricsUtil.FinalSendOutcomeMetric
                                        .DIMENSION_ENDPOINT_NAME,
                                TEST_ENDPOINT_NAME,
                                HttpRetryer2MetricsUtil.FinalSendOutcomeMetric
                                        .DIMENSION_SEND_OUTCOME,
                                HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.MAX_RETRIES_REACHED
                                        .toString()),
                        1,
                        0,
                        Unit.COUNT,
                        metricNameCaptor,
                        dimensionSetArgumentCaptor,
                        valueArgumentCaptor,
                        unitArgumentCaptor);
            }
            metric++;
        }
    }

    private HttpRetryer2ConfigTestFixture configFixtureWithRetries(int testMaxRetries) {
        return new HttpRetryer2ConfigTestFixture(
                TEST_ENDPOINT_NAME,
                TEST_RETRY_STATUS_CODES,
                TEST_SUCCESS_STATUS_CODES,
                testMaxRetries);
    }
}
