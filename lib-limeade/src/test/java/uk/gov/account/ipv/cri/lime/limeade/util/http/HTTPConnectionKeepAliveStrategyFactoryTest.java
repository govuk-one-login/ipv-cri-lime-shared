package uk.gov.account.ipv.cri.lime.limeade.util.http;

import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.account.ipv.cri.lime.limeade.testfixtures.HttpResponseFixtures;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HTTPConnectionKeepAliveStrategyFactoryTest {

    @ParameterizedTest
    @CsvSource({"30, false", "1000, true"})
    void shouldCreateHTTPConnectionKeepAliveStrategy(
            long keepAliveSeconds, boolean useRemoteHeader) {
        Map<String, String> testHeaders = new HashMap<>();

        ConnectionKeepAliveStrategy connectionKeepAliveStrategy =
                HTTPConnectionKeepAliveStrategyFactory.createHTTPConnectionKeepAliveStrategy(
                        keepAliveSeconds, useRemoteHeader);

        long expectedkeepAliveSeconds = keepAliveSeconds;

        if (useRemoteHeader) {
            expectedkeepAliveSeconds = 50;
            testHeaders.put("Keep-Alive", "timeout=" + expectedkeepAliveSeconds);
        }

        long actualKeepAliveMs =
                connectionKeepAliveStrategy.getKeepAliveDuration(
                        HttpResponseFixtures.createHttpResponse(200, testHeaders, "", false), null);
        long actualKeepAliveSeconds = actualKeepAliveMs / 1000;

        assertEquals(expectedkeepAliveSeconds, actualKeepAliveSeconds);
    }

    @Test
    void shouldCreateHTTPConnectionKeepAliveStrategyWhenRemoteHasInvalidHeader() {
        Map<String, String> testHeaders = new HashMap<>();

        ConnectionKeepAliveStrategy connectionKeepAliveStrategy =
                HTTPConnectionKeepAliveStrategyFactory.createHTTPConnectionKeepAliveStrategy(
                        120, true);

        long expectedKeepAliveSeconds = 120;
        testHeaders.put("Keep-Alive", "TIMEOUT=\"ABC\"");

        long actualKeepAliveMs =
                connectionKeepAliveStrategy.getKeepAliveDuration(
                        HttpResponseFixtures.createHttpResponse(200, testHeaders, "", false), null);
        long actualKeepAliveSeconds = actualKeepAliveMs / 1000;

        assertEquals(expectedKeepAliveSeconds, actualKeepAliveSeconds);
    }

    @Test
    void shouldCreateHTTPConnectionKeepAliveStrategyWhenRemoteHasInvalidHeader2() {
        Map<String, String> testHeaders = new HashMap<>();

        ConnectionKeepAliveStrategy connectionKeepAliveStrategy =
                HTTPConnectionKeepAliveStrategyFactory.createHTTPConnectionKeepAliveStrategy(
                        120, true);

        long expectedKeepAliveSeconds = 120;
        testHeaders.put("Keep-Alive", "TIMEOUT=");

        long actualKeepAliveMs =
                connectionKeepAliveStrategy.getKeepAliveDuration(
                        HttpResponseFixtures.createHttpResponse(200, testHeaders, "", false), null);
        long actualKeepAliveSeconds = actualKeepAliveMs / 1000;

        assertEquals(expectedKeepAliveSeconds, actualKeepAliveSeconds);
    }

    @Test
    void shouldCreateHTTPConnectionKeepAliveStrategyWhenRemoteHasNoTimeoutHeader() {
        Map<String, String> testHeaders = new HashMap<>();

        ConnectionKeepAliveStrategy connectionKeepAliveStrategy =
                HTTPConnectionKeepAliveStrategyFactory.createHTTPConnectionKeepAliveStrategy(
                        120, true);

        long expectedKeepAliveSeconds = 120;

        long actualKeepAliveMs =
                connectionKeepAliveStrategy.getKeepAliveDuration(
                        HttpResponseFixtures.createHttpResponse(200, testHeaders, "", false), null);
        long actualKeepAliveSeconds = actualKeepAliveMs / 1000;

        assertEquals(expectedKeepAliveSeconds, actualKeepAliveSeconds);
    }

    @Test
    void shouldCreateHTTPConnectionKeepAliveStrategyWhenRemoteHasNoHeaders() {

        ConnectionKeepAliveStrategy connectionKeepAliveStrategy =
                HTTPConnectionKeepAliveStrategyFactory.createHTTPConnectionKeepAliveStrategy(
                        120, true);

        long expectedKeepAliveSeconds = 120;

        long actualKeepAliveMs =
                connectionKeepAliveStrategy.getKeepAliveDuration(
                        HttpResponseFixtures.createHttpResponse(200, null, "", false), null);
        long actualKeepAliveSeconds = actualKeepAliveMs / 1000;

        assertEquals(expectedKeepAliveSeconds, actualKeepAliveSeconds);
    }
}
