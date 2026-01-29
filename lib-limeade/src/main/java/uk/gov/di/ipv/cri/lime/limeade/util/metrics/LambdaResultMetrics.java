package uk.gov.di.ipv.cri.lime.limeade.util.metrics;

public interface LambdaResultMetrics {
    MetricsProbe getMetricsProbe();

    String getLambdaName();
}
