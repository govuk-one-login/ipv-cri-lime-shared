package uk.gov.account.ipv.cri.lime.limeade.util.http;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.account.ipv.cri.lime.limeade.annotation.ExcludeConstructorFromGeneratedCoverageReport;

public final class HTTPConnectionKeepAliveStrategyFactory {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(HTTPConnectionKeepAliveStrategyFactory.class);

    @ExcludeConstructorFromGeneratedCoverageReport
    private HTTPConnectionKeepAliveStrategyFactory() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }

    // See https://hc.apache.org/httpcomponents-client-4.5.x/current/tutorial/html/connmgmt.html
    public static ConnectionKeepAliveStrategy createHTTPConnectionKeepAliveStrategy(
            long defaultKeepAliveSeconds, boolean useRemoteHeaderValue) {

        return (response, context) -> {
            long requestedKeepAliveSeconds = defaultKeepAliveSeconds;

            if (useRemoteHeaderValue) {

                // Will used the remote value if present or fallback to keepAliveSeconds
                requestedKeepAliveSeconds =
                        retrieveRemoteHeaderKeepAliveHeaderIfPresent(
                                defaultKeepAliveSeconds, response);
            }

            // Use our requestedKeepAlive if sensible, else use a default
            long keepAliveMillis =
                    requestedKeepAliveSeconds > 0
                            ? (requestedKeepAliveSeconds * 1000)
                            : (defaultKeepAliveSeconds * 1000);

            LOGGER.info("Using Keep-Alive of {}ms", keepAliveMillis);

            return keepAliveMillis;
        };
    }

    // Honour 'keep-alive' header if present
    private static long retrieveRemoteHeaderKeepAliveHeaderIfPresent(
            long fallbackKeepAliveSeconds, HttpResponse response) {
        HeaderElementIterator it =
                new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));

        while (it.hasNext()) {
            HeaderElement headerElement = it.nextElement();
            String headerParam = headerElement.getName();
            String headerValue = headerElement.getValue();
            if (headerValue != null && headerParam.equalsIgnoreCase("timeout")) {
                try {
                    // Use the remote header values
                    long remoteValueSeconds = Long.parseLong(headerValue);

                    LOGGER.info(
                            "Remote Header has timeout present with value of {} seconds",
                            remoteValueSeconds);

                    return remoteValueSeconds;
                } catch (NumberFormatException ignore) {
                    // Junk in the header - Do nothing with exception
                    // Fall through to our fallbackKeepAliveSeconds
                }
            }
        }

        return fallbackKeepAliveSeconds;
    }
}
