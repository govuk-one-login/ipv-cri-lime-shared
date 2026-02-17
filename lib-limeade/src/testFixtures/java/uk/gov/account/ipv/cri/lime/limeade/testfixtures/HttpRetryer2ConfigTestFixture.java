package uk.gov.account.ipv.cri.lime.limeade.testfixtures;

import uk.gov.account.ipv.cri.lime.limeade.service.http.retryer2.HttpRetryer2Config;

import java.util.List;

public record HttpRetryer2ConfigTestFixture(
        String metricSafeEndpointName,
        List<Integer> retryStatusCodes,
        List<Integer> successStatusCodes,
        int maxRetries)
        implements HttpRetryer2Config {}
