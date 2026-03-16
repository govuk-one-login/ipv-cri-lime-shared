package uk.gov.account.ipv.cri.lime.limeade.service.http.retryer2;

import software.amazon.lambda.powertools.metrics.model.DimensionSet;
import software.amazon.lambda.powertools.metrics.model.MetricUnit;
import uk.gov.account.ipv.cri.lime.limeade.annotation.ExcludeClassFromGeneratedCoverageReport;
import uk.gov.account.ipv.cri.lime.limeade.util.metrics.metricsprobe.MetricsProbe;

public class HttpRetryer2MetricsUtil {

    private static final String HTTP_RETRYER = "http_retryer";

    private HttpRetryer2MetricsUtil() {
        // Utility Class
    }

    /**
     * Records a successful send from the HttpRetryer2, capturing the response latency as the metric
     * value. Filter by DIMENSION_ENDPOINT_NAME to see all successful responses for an endpoint.
     * Filter by DIMENSION_RESPONSE_STATUS_CODE to separate expected from unexpected status codes
     * returned by the remote API.
     *
     * <p>When querying in CloudWatch, use SampleCount to count occurrences rather than Sum.
     */
    public static void captureHttpRetryerSendMetric(
            String endpointName,
            MetricsProbe metricsProbe,
            int statusCode,
            long responseLatencyMs) {
        metricsProbe.captureMetric(
                HttpRetryer2MetricsUtil.HTTP_RETRYER,
                DimensionSet.of(
                        SendSuccessMetric.DIMENSION_ENDPOINT_NAME,
                        endpointName,
                        SendSuccessMetric.DIMENSION_RESPONSE_STATUS_CODE,
                        String.valueOf(statusCode)),
                responseLatencyMs,
                MetricUnit.MILLISECONDS);
    }

    /**
     * Records a failed send from the HttpRetryer2, capturing the time elapsed before failure as the
     * metric value. A failure here means the request could not be sent at all, either the
     * connection could not be established, or no response was received after the socket connected.
     *
     * <p>When querying in CloudWatch, use SampleCount to count occurrences rather than Sum.
     */
    public static void captureHttpRetryerSendFailMetric(
            String endpointName, MetricsProbe metricsProbe, Exception e, long responseLatencyMs) {
        metricsProbe.captureMetric(
                HttpRetryer2MetricsUtil.HTTP_RETRYER,
                DimensionSet.of(
                        SendFailureMetric.DIMENSION_ENDPOINT_NAME,
                        endpointName,
                        SendFailureMetric.DIMENSION_FAILURE_CAUSE,
                        e.getClass().getSimpleName()),
                responseLatencyMs,
                MetricUnit.MILLISECONDS);
    }

    /**
     * If Retrying is enabled, the try count reached is recorded. Large retry counts should be
     * investigated if sustained
     */
    public static void captureHttpRetryerSendRetryMetric(
            String endpointName, MetricsProbe metricsProbe, int tryCount) {
        metricsProbe.captureMetric(
                HttpRetryer2MetricsUtil.HTTP_RETRYER,
                DimensionSet.of(
                        SendRetryMetric.DIMENSION_ENDPOINT_NAME,
                        endpointName,
                        SendRetryMetric.DIMENSION_TRY_COUNT,
                        String.valueOf(tryCount)),
                1, // Always 1 - single journey
                MetricUnit.COUNT);
    }

    // When the final http retryer finished what was the outcome
    // This can be all valid status codes after success or max retries
    public static void captureHttpRetryerFinalSendOutcomeMetric(
            String endpointName,
            MetricsProbe metricsProbe,
            FinalSendOutcomeMetric finalSendMetric) {
        metricsProbe.captureMetric(
                HttpRetryer2MetricsUtil.HTTP_RETRYER,
                DimensionSet.of(
                        FinalSendOutcomeMetric.DIMENSION_ENDPOINT_NAME,
                        endpointName,
                        FinalSendOutcomeMetric.DIMENSION_SEND_OUTCOME,
                        finalSendMetric.toString()),
                1, // Always 1 - single journey
                MetricUnit.COUNT);
    }

    public enum FinalSendOutcomeMetric {
        SEND_OK("send_ok"),
        NON_RETRYABLE_STATUS("non_retryable_status"),
        MAX_RETRIES_REACHED("max_retries_reached"),
        NON_RETRYABLE_EXCEPTION("non_retryable_exception");

        private final String name;

        FinalSendOutcomeMetric(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static final String DIMENSION_ENDPOINT_NAME = "endpoint_name";
        public static final String DIMENSION_SEND_OUTCOME = "send_outcome";
    }

    @ExcludeClassFromGeneratedCoverageReport
    public enum SendSuccessMetric {
        SUCCESS("success");

        private final String name;

        SendSuccessMetric(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static final String DIMENSION_ENDPOINT_NAME = "endpoint_name";
        public static final String DIMENSION_RESPONSE_STATUS_CODE = "response_status_code";
    }

    @ExcludeClassFromGeneratedCoverageReport
    public enum SendFailureMetric {
        FAILURE("failure");

        private final String name;

        SendFailureMetric(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static final String DIMENSION_ENDPOINT_NAME = "endpoint_name";
        public static final String DIMENSION_FAILURE_CAUSE = "failure_cause";
    }

    @ExcludeClassFromGeneratedCoverageReport
    public enum SendRetryMetric {
        RETRY("retry");

        private final String name;

        SendRetryMetric(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static final String DIMENSION_ENDPOINT_NAME = "endpoint_name";
        public static final String DIMENSION_TRY_COUNT = "try_count";
    }
}
