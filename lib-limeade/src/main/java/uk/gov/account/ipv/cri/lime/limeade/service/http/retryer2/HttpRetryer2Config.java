package uk.gov.account.ipv.cri.lime.limeade.service.http.retryer2;

import java.util.List;

public interface HttpRetryer2Config {
    String metricSafeEndpointName();

    /*
        408 Request Timeout
        425 Too Early
        429 Too Many Requests
        500 Internal Server Error
        502 Bad Gateway
        503 Service Unavailable
        504 Gateway Timeout
    */
    default List<Integer> retryStatusCodes() {
        return List.of(408, 425, 429, 500, 502, 503, 504);
    }

    default List<Integer> successStatusCodes() {
        return List.of(200);
    }

    default int maxRetries() {
        return 0;
    }

    default long maxRetryDelayMs() {
        return 12800L;
    }
}
