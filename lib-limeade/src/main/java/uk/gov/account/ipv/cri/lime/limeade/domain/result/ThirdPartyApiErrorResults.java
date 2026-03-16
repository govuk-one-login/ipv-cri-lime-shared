package uk.gov.account.ipv.cri.lime.limeade.domain.result;

import uk.gov.account.ipv.cri.lime.limeade.annotation.ExcludeClassFromGeneratedCoverageReport;

import java.util.ArrayList;
import java.util.List;

@ExcludeClassFromGeneratedCoverageReport
public class ThirdPartyApiErrorResults {
    private final List<ThirdPartyApiError> errors = new ArrayList<>();

    public ThirdPartyApiErrorResults() {
        // intended
    }

    public void recordError(ThirdPartyApiError error) {
        this.errors.add(error);
    }

    public List<ThirdPartyApiError> getErrors() {
        return this.errors;
    }
}
