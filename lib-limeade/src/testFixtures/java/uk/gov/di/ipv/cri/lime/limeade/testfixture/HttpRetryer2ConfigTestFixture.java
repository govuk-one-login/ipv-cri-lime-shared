package uk.gov.di.ipv.cri.lime.limeade.testfixture;

import uk.gov.di.ipv.cri.lime.limeade.service.http.retryer2.HttpRetryer2Config;

import java.util.List;

public record HttpRetryer2ConfigTestFixture(
        String metricSafeEndpointName,
        List<Integer> retryStatusCodes,
        List<Integer> successStatusCodes,
        int maxRetries)
        implements HttpRetryer2Config {}
