package uk.gov.account.ipv.cri.lime.limeade.util.http;

import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.account.ipv.cri.lime.limeade.testfixtures.HttpResponseFixtures;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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

    static Stream<String> unusableHeaderValues() {
        return Stream.of(
                "MAX=117", // invalid param name with valid value
                "TIMEOUT=ABC", // valid param name with non-numeric value
                "TIMEOUT=-999", // timeout param with negative value
                "asdasldjaslkjd", // garbage
                "TIMEOUT" // valid param with no value (null)
                );
    }

    @ParameterizedTest
    @MethodSource("unusableHeaderValues")
    void shouldFallBackToConfiguredKeepAliveForUnusableHeaderValues(String headerValue) {
        Map<String, String> testHeaders = new HashMap<>();
        testHeaders.put("Keep-Alive", headerValue);

        ConnectionKeepAliveStrategy connectionKeepAliveStrategy =
                HTTPConnectionKeepAliveStrategyFactory.createHTTPConnectionKeepAliveStrategy(
                        120, true);

        long actualKeepAliveMs =
                connectionKeepAliveStrategy.getKeepAliveDuration(
                        HttpResponseFixtures.createHttpResponse(200, testHeaders, "", false), null);

        assertEquals(120, actualKeepAliveMs / 1000);
    }

    static Stream<Map<String, String>> absentKeepAliveHeaders() {
        return Stream.of(null, new HashMap<>());
    }

    @ParameterizedTest
    @MethodSource("absentKeepAliveHeaders")
    void shouldFallBackToConfiguredKeepAliveWhenNoKeepAliveHeaderPresent(
            Map<String, String> headers) {
        ConnectionKeepAliveStrategy connectionKeepAliveStrategy =
                HTTPConnectionKeepAliveStrategyFactory.createHTTPConnectionKeepAliveStrategy(
                        120, true);

        long actualKeepAliveMs =
                connectionKeepAliveStrategy.getKeepAliveDuration(
                        HttpResponseFixtures.createHttpResponse(200, headers, "", false), null);

        assertEquals(120, actualKeepAliveMs / 1000);
    }
}
