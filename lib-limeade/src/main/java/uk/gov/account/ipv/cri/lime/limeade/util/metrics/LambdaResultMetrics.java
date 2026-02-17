package uk.gov.account.ipv.cri.lime.limeade.util.metrics;

public interface LambdaResultMetrics {
    MetricsProbe getMetricsProbe();

    String getLambdaName();
}
