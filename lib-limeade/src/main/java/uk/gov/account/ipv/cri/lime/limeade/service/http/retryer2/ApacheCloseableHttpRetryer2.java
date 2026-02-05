package uk.gov.account.ipv.cri.lime.limeade.service.http.retryer2;

import uk.gov.account.ipv.cri.lime.limeade.util.http.HTTPReply;
import uk.gov.account.ipv.cri.lime.limeade.util.http.HTTPReplyHelper;
import uk.gov.account.ipv.cri.lime.limeade.util.metrics.MetricsProbe;
import uk.gov.account.ipv.cri.lime.limeade.util.timing.SleepHelper;
import uk.gov.account.ipv.cri.lime.limeade.util.timing.StopWatch;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * implementation HttpRetryer2 via Apache 4.5 CloseableHttpClient, supporting
 * HttpRetryer2EndpointMetrics One HTTP-Retryer2 should be used per API endpoint
 */
public class ApacheCloseableHttpRetryer2 implements HttpRetryer2 {

    private static final Logger LOGGER = LogManager.getLogger();

    private final String endpointName;
    private final StopWatch stopWatch = new StopWatch();
    private final SleepHelper sleepHelper;
    private final CloseableHttpClient closeableHttpClient;

    private final List<Integer> retryStatusCodes;
    private final List<Integer> successStatusCodes;

    private final int maxRetries;
    private final MetricsProbe metricsProbe;

    public ApacheCloseableHttpRetryer2(
            HttpRetryer2Config httpRetryer2Config,
            CloseableHttpClient closeableHttpClient,
            MetricsProbe metricsProbe) {

        // Will be used in metric and logging
        this.endpointName =
                httpRetryer2Config.metricSafeEndpointName().replaceAll("\\s+", "").toLowerCase();

        this.retryStatusCodes = httpRetryer2Config.retryStatusCodes();
        this.successStatusCodes = httpRetryer2Config.successStatusCodes();

        this.closeableHttpClient = closeableHttpClient;
        this.metricsProbe = metricsProbe;

        this.sleepHelper = new SleepHelper(httpRetryer2Config.maxRetryDelayMs());
        this.maxRetries = httpRetryer2Config.maxRetries();

        LOGGER.info("Max retries configured as {}", maxRetries);
    }

    @Override
    public String getEndpointName() {
        return endpointName;
    }

    public HTTPReply sendHTTPRequestRetryIfAllowed(HttpUriRequest request) throws IOException {

        // 0 is initial request, > 0 are retries
        int tryCount = 0;
        boolean retry = false;
        int statusCode = 0;
        HTTPReply reply = null;

        do {
            stopWatch.start();

            if (retry) {
                HttpRetryer2MetricsUtil.captureHttpRetryerSendRetryMetric(
                        getEndpointName(), metricsProbe, tryCount);
            }

            // Wait before sending request (0ms for first try)
            sleepHelper.busyWaitWithExponentialBackOff(tryCount);

            try (CloseableHttpResponse httpResponse = closeableHttpClient.execute(request)) {

                statusCode = httpResponse.getStatusLine().getStatusCode();

                retry = retryStatusCodes.contains(statusCode);

                reply = HTTPReplyHelper.retrieveResponse(httpResponse, getEndpointName());

                long latencyMs = stopWatch.stop();

                HttpRetryer2MetricsUtil.captureHttpRetryerSendMetric(
                        getEndpointName(), metricsProbe, statusCode, latencyMs);

                if (retry) {
                    LOGGER.warn(
                            "{} {} found retryable statusCode - {}",
                            ApacheCloseableHttpRetryer2.class.getSimpleName(),
                            getEndpointName(),
                            statusCode);
                }

                LOGGER.info(
                        "{} HTTPRequestRetry - totalRequests {}, retries {}, retryNeeded {}, statusCode {} latencyMs {}",
                        getEndpointName(),
                        tryCount + 1,
                        tryCount,
                        retry,
                        statusCode,
                        latencyMs);
            } catch (IOException e) {
                long latencyMs = stopWatch.stop();

                HttpRetryer2MetricsUtil.captureHttpRetryerSendFailMetric(
                        getEndpointName(), metricsProbe, e, latencyMs);

                retry = determineIfToRetryOrThrowFinalException(tryCount, e);
            }
        } while (retry && (tryCount++ < maxRetries));

        captureFinalSendOutcomeMetric(statusCode, tryCount);

        return reply;
    }

    private boolean determineIfToRetryOrThrowFinalException(
            int tryCount, IOException caughtIOException) throws IOException {

        boolean hasRetriesLeft = (tryCount < maxRetries);

        // Retryable exceptions - logic reads as "Not any of"
        boolean isRetryableException =
                (caughtIOException instanceof ConnectTimeoutException)
                        || (caughtIOException instanceof SocketTimeoutException);

        // Only for simplifying logging
        boolean retry = hasRetriesLeft && isRetryableException;

        // All state is in this one log - no further logs needed
        LOGGER.info(
                "{} HTTPRequestRetry {} - totalRequests {}, retries {}, retrying {} - Failure {} reason {}",
                getEndpointName(),
                caughtIOException.getMessage(),
                tryCount + 1,
                tryCount,
                retry,
                caughtIOException.getClass().getCanonicalName(),
                caughtIOException.getMessage());

        if (!isRetryableException) {
            // Fatal non-retryable exception
            HttpRetryer2MetricsUtil.captureHttpRetryerFinalSendOutcomeMetric(
                    getEndpointName(),
                    metricsProbe,
                    HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.NON_RETRYABLE_EXCEPTION);
            throw caughtIOException;
        } else if (hasRetriesLeft) {
            // We retry on certain IOExceptions - if we have retries left and there were no fatal
            // exceptions
            return true;
        } else {
            // Max Retries reached by exceptions
            HttpRetryer2MetricsUtil.captureHttpRetryerFinalSendOutcomeMetric(
                    getEndpointName(),
                    metricsProbe,
                    HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.MAX_RETRIES_REACHED);
            // This ends the retrying
            throw caughtIOException;
        }
    }

    private void captureFinalSendOutcomeMetric(int lastStatusCode, int finalTryCount) {
        LOGGER.info(
                "{} HTTPRequestRetry Exited lastStatusCode {}", getEndpointName(), lastStatusCode);

        if (successStatusCodes.contains(lastStatusCode)) {
            HttpRetryer2MetricsUtil.captureHttpRetryerFinalSendOutcomeMetric(
                    getEndpointName(),
                    metricsProbe,
                    HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.SEND_OK);
        } else if (finalTryCount < maxRetries) {
            // Reachable when the remote api responds initially with a retryable status code, then
            // during a retry with a non-retryable status code.
            HttpRetryer2MetricsUtil.captureHttpRetryerFinalSendOutcomeMetric(
                    getEndpointName(),
                    metricsProbe,
                    HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.NON_RETRYABLE_STATUS);
        } else {
            // Max Retries reached by status code
            HttpRetryer2MetricsUtil.captureHttpRetryerFinalSendOutcomeMetric(
                    getEndpointName(),
                    metricsProbe,
                    HttpRetryer2MetricsUtil.FinalSendOutcomeMetric.MAX_RETRIES_REACHED);
        }
    }
}
