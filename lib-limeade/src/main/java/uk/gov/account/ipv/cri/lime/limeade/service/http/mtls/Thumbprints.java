package uk.gov.account.ipv.cri.lime.limeade.service.http.mtls;

import uk.gov.account.ipv.cri.lime.limeade.annotation.ExcludeClassFromGeneratedCoverageReport;

@ExcludeClassFromGeneratedCoverageReport
public record Thumbprints(String sha1Thumbprint, String sha256Thumbprint) {}
