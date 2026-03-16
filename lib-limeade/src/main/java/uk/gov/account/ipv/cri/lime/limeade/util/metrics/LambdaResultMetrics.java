package uk.gov.account.ipv.cri.lime.limeade.util.metrics;

import uk.gov.account.ipv.cri.lime.limeade.util.metrics.metricsprobe.MetricsProbe;

public interface LambdaResultMetrics {
    MetricsProbe getMetricsProbe();

    String getLambdaName();
}
