package uk.gov.account.ipv.cri.lime.limeade.util.metrics.metricsprobe;

import software.amazon.lambda.powertools.metrics.model.DimensionSet;
import software.amazon.lambda.powertools.metrics.model.MetricUnit;

public interface MetricsProbe {
    void captureMetric(String metricName, DimensionSet dimensionSet, double value, MetricUnit unit);
}
