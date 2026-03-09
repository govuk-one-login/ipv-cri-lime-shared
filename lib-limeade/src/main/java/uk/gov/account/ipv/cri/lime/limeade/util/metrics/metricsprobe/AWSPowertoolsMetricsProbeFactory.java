package uk.gov.account.ipv.cri.lime.limeade.util.metrics.metricsprobe;

import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.metrics.MetricsFactory;
import software.amazon.lambda.powertools.metrics.model.DimensionSet;
import software.amazon.lambda.powertools.metrics.model.MetricUnit;

public class AWSPowertoolsMetricsProbeFactory {
    private static final String METRICS_NAMESPACE = System.getenv("POWERTOOLS_METRICS_NAMESPACE");

    private AWSPowertoolsMetricsProbeFactory() {
        // Intended
    }

    public static MetricsProbe getMetricsProbe() {
        return MetricsProbeSingletonHelper.INSTANCE;
    }

    private static class MetricsProbeSingletonHelper {
        private static final MetricsProbe INSTANCE = new PowertoolsMetricsProbe();
    }

    // Thread safe due to instantiation at classloading
    private static class PowertoolsMetricsProbe implements MetricsProbe {
        private static final Metrics METRICS = MetricsFactory.getMetricsInstance();

        private PowertoolsMetricsProbe() {
            // Intended
        }

        /**
         * Captures a metric with custom dimensions. Calls to this method flush immediately to allow
         * switching metric dimensions between calls.
         */
        public void captureMetric(
                String metricName, DimensionSet dimensionSet, double value, MetricUnit unit) {
            METRICS.flushSingleMetric(metricName, value, unit, METRICS_NAMESPACE, dimensionSet);
        }
    }
}
