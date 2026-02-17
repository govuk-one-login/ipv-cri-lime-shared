package uk.gov.account.ipv.cri.lime.limeade.service.http;

import org.apache.http.HttpException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import uk.gov.account.ipv.cri.lime.limeade.annotation.ExcludeClassFromGeneratedCoverageReport;

import javax.net.ssl.SSLContext;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@ExcludeClassFromGeneratedCoverageReport
public class CloseableHttpClientFactory {

    private CloseableHttpClientFactory() {
        /* Intended */
    }

    // SSL context with TLS
    public static CloseableHttpClient generateTLSHttpClient() throws HttpException {
        try {
            SSLContext sslContext = SSLContexts.custom().setProtocol("TLSv1.2").build();

            return HttpClients.custom().setSSLContext(sslContext).build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new HttpException(e.getMessage());
        }
    }
}
