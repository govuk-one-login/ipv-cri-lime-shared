package uk.gov.account.ipv.cri.lime.limeade.domain.result;

import uk.gov.account.ipv.cri.lime.limeade.annotation.ExcludeClassFromGeneratedCoverageReport;

// WARNING this class is not used for error logs, it contains flags that control CRI logic
// These flags are generic.
// Changes/Additions will require updating the error handing.
@ExcludeClassFromGeneratedCoverageReport
public enum ThirdPartyApiError {
    CRI_FAILED_TO_CREATE_HTTP_REQUEST, // Cri Error
    HTTP_CONNECTION_ERROR, // M1C
    HTTP_STATUS_CODE_UNEXPECTED, // M1C
    FAILED_TO_MAP_API_RESPONSE, // CRI Error
    API_RESPONSE_VALIDATION_FAILED, // M1C
    API_RESPONSE_INDICATED_ERROR, // M1C - (For endpoints that can indicate errors via api response)
}
