package uk.gov.account.ipv.cri.lime.limeade.util.http;

import uk.gov.account.ipv.cri.lime.limeade.annotation.ExcludeConstructorFromGeneratedCoverageReport;

import java.util.Map;

public class HTTPReply {
    public final int statusCode;
    public final Map<String, String> responseHeaders;
    public final String responseBody;

    public HTTPReply(int statusCode, Map<String, String> responseHeaders, String responseBody) {
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    @ExcludeConstructorFromGeneratedCoverageReport
    private HTTPReply() {
        statusCode = -1;
        responseHeaders = null;
        responseBody = null;

        throw new IllegalStateException("Not valid to call no args constructor for this class");
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
