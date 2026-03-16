package uk.gov.account.ipv.cri.lime.limeade.util.http;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class HTTPReplyHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPReplyHelper.class);

    private HTTPReplyHelper() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }

    // Small helper to avoid duplicating this code for each endpoint and api
    public static HTTPReply retrieveResponse(HttpResponse response, String endpointName)
            throws IOException {
        try {
            Map<String, String> responseHeaders = getAllHeaders(response);

            String mappedBody = EntityUtils.toString(response.getEntity());

            // EntityUtils can return null
            String responseBody =
                    (mappedBody) == null
                            ? String.format("No %s response body text found", endpointName)
                            : mappedBody;
            int httpStatusCode = response.getStatusLine().getStatusCode();

            return new HTTPReply(httpStatusCode, responseHeaders, responseBody);
        } catch (IOException e) {

            LOGGER.error("IOException retrieving {} response body", endpointName);

            throw e;
        }
    }

    private static Map<String, String> getAllHeaders(HttpResponse response) {

        Map<String, String> headers = new HashMap<>();

        Header[] apacheHeaders = response.getAllHeaders();

        for (Header apacheHeader : apacheHeaders) {
            headers.put(apacheHeader.getName(), apacheHeader.getValue());
        }

        return headers;
    }
}
