package uk.gov.account.ipv.cri.lime.limeade.util.http;

import java.util.Map;

public record HTTPReply(int statusCode, Map<String, String> responseHeaders, String responseBody) {}
