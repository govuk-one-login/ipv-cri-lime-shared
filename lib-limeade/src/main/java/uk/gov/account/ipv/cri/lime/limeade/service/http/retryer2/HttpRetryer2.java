package uk.gov.account.ipv.cri.lime.limeade.service.http.retryer2;

import uk.gov.account.ipv.cri.lime.limeade.util.http.HTTPReply;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;

public interface HttpRetryer2 {
    // Interface used for HttpRetryer2 to abstract away from the CloseableHttpClient client in
    // HttpRetryer
    HTTPReply sendHTTPRequestRetryIfAllowed(HttpUriRequest request) throws IOException;

    String getEndpointName();
}
