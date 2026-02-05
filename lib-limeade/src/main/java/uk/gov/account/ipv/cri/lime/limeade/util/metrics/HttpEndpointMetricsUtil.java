package uk.gov.account.ipv.cri.lime.limeade.util.metrics;

import software.amazon.cloudwatchlogs.emf.model.DimensionSet;
import software.amazon.cloudwatchlogs.emf.model.Unit;
import uk.gov.account.ipv.cri.lime.limeade.annotation.ExcludeClassFromGeneratedCoverageReport;
import uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy;

@ExcludeClassFromGeneratedCoverageReport
public class HttpEndpointMetricsUtil {

    private HttpEndpointMetricsUtil() {
        // Utility Class
    }

    public static void captureHttpEndpointRequestStateMetric(
            HttpEndpointMetrics httpEndpointMetrics,
            MetricsProbe metricsProbe,
            Strategy strategy,
            RequestState requestState) {
        metricsProbe.captureMetric(
                HttpEndpointRequestMetric.HTTP_ENDPOINT.toString(),
                DimensionSet.of(
                        HttpEndpointRequestMetric.DIMENSION_REQUEST_NAME,
                        httpEndpointMetrics.getEndpointName(),
                        HttpEndpointRequestMetric.DIMENSION_DIRECTION,
                        Direction.REQUEST.toString(),
                        HttpEndpointRequestMetric.DIMENSION_STRATEGY,
                        strategy.toString(),
                        HttpEndpointRequestMetric.DIMENSION_STATE,
                        requestState.toString()),
                1, // Always 1 - single journey
                Unit.COUNT);
    }

    public static void captureHttpEndpointResponseStateMetric(
            HttpEndpointMetrics httpEndpointMetrics,
            MetricsProbe metricsProbe,
            Strategy strategy,
            ResponseState responseState) {
        metricsProbe.captureMetric(
                HttpEndpointRequestMetric.HTTP_ENDPOINT.toString(),
                DimensionSet.of(
                        HttpEndpointRequestMetric.DIMENSION_REQUEST_NAME,
                        httpEndpointMetrics.getEndpointName(),
                        HttpEndpointRequestMetric.DIMENSION_DIRECTION,
                        Direction.RESPONSE.toString(),
                        HttpEndpointRequestMetric.DIMENSION_STRATEGY,
                        strategy.toString(),
                        HttpEndpointRequestMetric.DIMENSION_STATE,
                        responseState.toString()),
                1, // Always 1 - single journey
                Unit.COUNT);
    }

    /**
     * For some token APIs certain status codes indicate unrecoverable critical failures.
     *
     * @param httpEndpointMetrics
     * @param metricsProbe
     */
    public static void captureHttpEndpointResponseStatusCodeAlertMetric(
            HttpEndpointMetrics httpEndpointMetrics, MetricsProbe metricsProbe, Strategy strategy) {
        metricsProbe.captureMetric(
                HttpEndpointRequestMetric.HTTP_ENDPOINT.toString(),
                DimensionSet.of(
                        HttpEndpointRequestMetric.DIMENSION_REQUEST_NAME,
                        httpEndpointMetrics.getEndpointName(),
                        HttpEndpointRequestMetric.DIMENSION_DIRECTION,
                        Direction.RESPONSE.toString(),
                        HttpEndpointRequestMetric.DIMENSION_STRATEGY,
                        strategy.toString(),
                        HttpEndpointRequestMetric.DIMENSION_STATUS_CODE_ALERT,
                        "critical_status_code"),
                1, // Always 1 - single journey
                Unit.COUNT);
    }

    /**
     * This captures the latency of the http response. Note In contrast to the other metrics in this
     * helper, HTTP_ENDPOINT is not the metric name DIMENSION_LATENCY_MS is the metricName and with
     * the latency value stored in value. We should not capture unique or highly variable values in
     * dimensions
     *
     * @param httpEndpointMetrics
     * @param metricsProbe
     * @param latencyMs
     */
    public static void captureHttpEndpointResponseLatencyMetric(
            HttpEndpointMetrics httpEndpointMetrics,
            MetricsProbe metricsProbe,
            Strategy strategy,
            long latencyMs) {
        metricsProbe.captureMetric(
                HttpEndpointLatencyMetric.HTTP_ENDPOINT_LATENCY_MS.toString(),
                DimensionSet.of(
                        HttpEndpointLatencyMetric.DIMENSION_REQUEST_NAME,
                        httpEndpointMetrics.getEndpointName(),
                        HttpEndpointLatencyMetric.DIMENSION_DIRECTION,
                        Direction.RESPONSE.toString(),
                        HttpEndpointRequestMetric.DIMENSION_STRATEGY,
                        strategy.toString()),
                latencyMs, // raw latency value
                Unit.MILLISECONDS);
    }

    @ExcludeClassFromGeneratedCoverageReport
    public enum HttpEndpointRequestMetric {
        HTTP_ENDPOINT("http_endpoint");

        private final String name;

        HttpEndpointRequestMetric(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static final String DIMENSION_REQUEST_NAME = "request_name";
        public static final String DIMENSION_DIRECTION = "direction";
        public static final String DIMENSION_STATE = "state";
        public static final String DIMENSION_STATUS_CODE_ALERT = "status_code_alert";
        public static final String DIMENSION_STRATEGY = "strategy";
    }

    @ExcludeClassFromGeneratedCoverageReport
    public enum HttpEndpointLatencyMetric {
        HTTP_ENDPOINT_LATENCY_MS("http_endpoint_latency_ms");

        private final String name;

        HttpEndpointLatencyMetric(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static final String DIMENSION_REQUEST_NAME = "request_name";
        public static final String DIMENSION_DIRECTION = "direction";
    }

    @ExcludeClassFromGeneratedCoverageReport
    public enum Direction {
        REQUEST("request"),
        RESPONSE("response");

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @ExcludeClassFromGeneratedCoverageReport
    public enum RequestState {
        CREATE_OK("created_ok"),
        CREATE_FAIL("create_fail"),
        SEND_OK("send_ok"),
        SEND_ERROR("send_error");

        private final String name;

        RequestState(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @ExcludeClassFromGeneratedCoverageReport
    public enum ResponseState {
        HTTP_STATUS_CODE_EXPECTED("http_status_code_expected"),
        HTTP_STATUS_CODE_UNEXPECTED("http_status_code_unexpected"),
        API_RESPONSE_VALID("api_response_valid"),
        API_RESPONSE_INVALID("api_response_invalid");

        private final String name;

        ResponseState(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
