package uk.gov.account.ipv.cri.lime.limeade.http.retryer2;

import org.junit.jupiter.api.Test;
import uk.gov.account.ipv.cri.lime.limeade.service.http.retryer2.HttpRetryer2Config;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpRetryer2ConfigTest {

    private final HttpRetryer2Config underTest = () -> "metric-name";

    @Test
    void shouldReturnDefaultRetryStatusCodes() {
        assertEquals(List.of(408, 425, 429, 500, 502, 503, 504), underTest.retryStatusCodes());
    }

    @Test
    void shouldReturnDefaultSuccessStatusCodes() {
        assertEquals(List.of(200), underTest.successStatusCodes());
    }

    @Test
    void shouldReturnDefaultMaxRetries() {
        assertEquals(0, underTest.maxRetries());
    }
}
