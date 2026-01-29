package uk.gov.di.ipv.cri.lime.limeade.util.metrics;

import uk.gov.di.ipv.cri.lime.limeade.annotation.ExcludeClassFromGeneratedCoverageReport;
import software.amazon.cloudwatchlogs.emf.model.DimensionSet;
import software.amazon.cloudwatchlogs.emf.model.Unit;

public class LambdaResultMetricUtil {

    private LambdaResultMetricUtil() {
        // Utility Class
    }

    public static void captureResultMetric(
            LambdaResultMetrics lambdaResultMetrics, Result result) {
        lambdaResultMetrics
                .getMetricsProbe()
                .captureMetric(
                        LambdaResultMetric.LAMBDA_RESULT.toString(),
                        DimensionSet.of(
                                LambdaResultMetric.DIMENSION_LAMBDA,
                                        lambdaResultMetrics.getLambdaName(),
                                LambdaResultMetric.DIMENSION_RESULT, result.toString()),
                        1, // Always 1 - single journey
                        Unit.COUNT);
    }

    @ExcludeClassFromGeneratedCoverageReport
    public enum LambdaResultMetric {
        LAMBDA_RESULT("lambda_result");

        private final String name;

        LambdaResultMetric(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static final String DIMENSION_RESULT = "result";
        public static final String DIMENSION_LAMBDA = "lambda";
    }

    @ExcludeClassFromGeneratedCoverageReport
    public enum Result {
        SUCCESS("success"),
        ERROR("error");

        private final String name;

        Result(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
