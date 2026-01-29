package uk.gov.di.ipv.cri.lime.limeade.util.metrics;

import software.amazon.cloudwatchlogs.emf.model.DimensionSet;
import software.amazon.cloudwatchlogs.emf.model.Unit;
import software.amazon.lambda.powertools.metrics.MetricsUtils;

public class MetricsProbe {

    public MetricsProbe() {
        // Intended
    }

    /**
     * Send a metric using specific custom dimensions - NOTE To enable switching metric dimension
     * MetricsUtils will auto flush all pending metrics immediately and after each call to this
     * method *
     */
    public void captureMetric(
            String metricName, DimensionSet dimensionSet, double value, Unit unit) {
        MetricsUtils.withMetricsLogger(
                metricsLogger -> {
                    metricsLogger.setDimensions(dimensionSet);
                    metricsLogger.putMetric(metricName, value, unit);
                });
    }
}
