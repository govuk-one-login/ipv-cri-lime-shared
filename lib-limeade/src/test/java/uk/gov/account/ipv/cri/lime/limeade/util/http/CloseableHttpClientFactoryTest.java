package uk.gov.account.ipv.cri.lime.limeade.util.http;

import org.apache.http.HttpException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.account.ipv.cri.lime.limeade.service.http.CloseableHttpClientFactory;

import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.account.ipv.cri.lime.limeade.http.mtls.KeyCertHelperTest.TEST_ROOT_CRT;
import static uk.gov.account.ipv.cri.lime.limeade.http.mtls.KeyCertHelperTest.TEST_TLS_CRT;
import static uk.gov.account.ipv.cri.lime.limeade.http.mtls.KeyCertHelperTest.TEST_TLS_KEY;

class CloseableHttpClientFactoryTest {

    @Test
    void shouldReturnHttpClientWithTLS() throws HttpException {

        CloseableHttpClient closeableHttpClient =
                CloseableHttpClientFactory.generateTLSHttpClient();

        assertNotNull(closeableHttpClient);
    }

    @ParameterizedTest
    @CsvSource({
        "CertificateException",
        "InvalidKeySpecException",
    })
    void shouldCatchExceptionAndThrowHttpClientExceptionForExceptionsGeneratingMTLSHttpClient(
            String exceptionName) {

        String base64TLSCertString = TEST_TLS_CRT;
        String base64TLSKeyString = TEST_TLS_KEY;
        String base64TLSRootCertString = TEST_ROOT_CRT;
        String base64TLSIntCertString = TEST_TLS_CRT;

        String badData = new String(Base64.getEncoder().encode("TEST1234".getBytes()));

        Class<? extends Throwable> expectedExceptionClass =
                switch (exceptionName) {
                    case "CertificateException" -> {
                        base64TLSCertString = badData;
                        yield CertificateException.class;
                    }
                    case "InvalidKeySpecException" -> {
                        base64TLSKeyString = badData;
                        yield InvalidKeySpecException.class;
                    }
                    default ->
                            throw new IllegalStateException("Unexpected value: " + exceptionName);
                };

        String finalBase64TLSCertString = base64TLSCertString;
        String finalBase64TLSKeyString = base64TLSKeyString;
        Throwable thrownException =
                assertThrows(
                        expectedExceptionClass,
                        () ->
                                CloseableHttpClientFactory.generateMTLSHttpClient(
                                        finalBase64TLSCertString,
                                        finalBase64TLSKeyString,
                                        base64TLSRootCertString,
                                        base64TLSIntCertString),
                        "An Error Message");

        assertNotNull(expectedExceptionClass);
        assertEquals(expectedExceptionClass, thrownException.getClass());
    }

    @Test
    void shouldReturnHTTPClientWithMTLS() {

        CloseableHttpClient closeableHttpClient =
                assertDoesNotThrow(
                        () ->
                                CloseableHttpClientFactory.generateMTLSHttpClient(
                                        TEST_TLS_CRT, TEST_TLS_KEY, TEST_ROOT_CRT, TEST_TLS_CRT));

        assertNotNull(closeableHttpClient);
    }
}
