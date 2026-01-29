package uk.gov.di.ipv.cri.lime.limeade.service.http.retryer2;

import uk.gov.di.ipv.cri.lime.limeade.annotation.ExcludeClassFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.lime.limeade.util.metrics.MetricsProbe;
import software.amazon.cloudwatchlogs.emf.model.DimensionSet;
import software.amazon.cloudwatchlogs.emf.model.Unit;

public class HttpRetryer2MetricsUtil {

    private static final String HTTP_RETRYER = "http_retryer";

    private HttpRetryer2MetricsUtil() {
        // Utility Class
    }

    /**
     * Records a successful send from the HttpRetryer2. Note Response Latency is captured as the
     * metric UNIT Use Sample Counts to get occurrences of this metric (Not Unit Summation) Filter
     * via exact DIMENSION_ENDPOINT_NAME will locate all successful responses. Filtering via
     * DIMENSION_RESPONSE_STATUS_CODE will allow separating expected and unexpected status code
     * indicators from the remote API
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
                Unit.MILLISECONDS);
    }

    /**
     * Records a failed send from the HttpRetryer2. Note Response Latency is captured as the metric
     * UNIT Use Sample Counts to get occurrences of this metric (Not Unit Summation) Failure in this
     * context means the request was unable to be sent at all. This could be due to being unable to
     * connect or not receiving any response post initial socket connection.
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
                Unit.MILLISECONDS);
    }

    /**
     * If Retrying is enabled, the try count reached is recorded Large retry counts should be
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
                Unit.COUNT);
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
                Unit.COUNT);
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
