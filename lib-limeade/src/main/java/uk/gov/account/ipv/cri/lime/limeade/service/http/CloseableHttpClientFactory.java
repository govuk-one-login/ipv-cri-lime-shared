package uk.gov.account.ipv.cri.lime.limeade.service.http;

import org.apache.http.HttpException;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import uk.gov.account.ipv.cri.lime.limeade.service.http.mtls.KeyCertHelper;
import uk.gov.account.ipv.cri.lime.limeade.util.http.HTTPConnectionKeepAliveStrategyFactory;

import javax.net.ssl.SSLContext;

import java.io.IOException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

public class CloseableHttpClientFactory {

    // Used internally at runtime when loading/retrieving keys into/from the SSL Keystore
    private static final char[] RANDOM_RUN_TIME_KEYSTORE_PASSWORD =
            UUID.randomUUID().toString().toCharArray();

    private CloseableHttpClientFactory() {
        /* Intended */
    }

    // SSL context with TLS
    public static CloseableHttpClient generateTLSHttpClient() throws HttpException {
        try {
            SSLContext sslContext = SSLContexts.custom().setProtocol("TLSv1.2").build();

            ConnectionKeepAliveStrategy connectionKeepAliveStrategy =
                    HTTPConnectionKeepAliveStrategyFactory.createHTTPConnectionKeepAliveStrategy(
                            120, true);

            return HttpClients.custom()
                    .setKeepAliveStrategy(connectionKeepAliveStrategy)
                    .setSSLContext(sslContext)
                    .setDefaultSocketConfig(SocketConfig.custom().setTcpNoDelay(true).build())
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new HttpException(e.getMessage());
        }
    }

    public static CloseableHttpClient generateMTLSHttpClient(
            String base64TLSCertString,
            String base64TLSKeyString,
            String base64TLSRootCertString,
            String base64TLSIntCertString)
            throws NoSuchAlgorithmException,
                    InvalidKeySpecException,
                    CertificateException,
                    KeyStoreException,
                    IOException,
                    UnrecoverableKeyException,
                    KeyManagementException {

        Certificate tlsCert = KeyCertHelper.getDecodedX509Certificate(base64TLSCertString);

        PrivateKey tlsKey = KeyCertHelper.getDecodedPrivateRSAKey(base64TLSKeyString);

        KeyStore keystoreTLS = createKeyStore(tlsCert, tlsKey);

        Certificate tlsRootCert = KeyCertHelper.getDecodedX509Certificate(base64TLSRootCertString);

        Certificate tlsIntCert = KeyCertHelper.getDecodedX509Certificate(base64TLSIntCertString);

        KeyStore trustStore = createTrustStore(new Certificate[] {tlsRootCert, tlsIntCert});

        SSLContext sslContext = sslContextSetup(keystoreTLS, trustStore);

        return HttpClients.custom().setSSLContext(sslContext).build();
    }

    private static SSLContext sslContextSetup(KeyStore clientTls, KeyStore caBundle)
            throws UnrecoverableKeyException,
                    NoSuchAlgorithmException,
                    KeyStoreException,
                    KeyManagementException {
        return SSLContexts.custom()
                .loadKeyMaterial(clientTls, RANDOM_RUN_TIME_KEYSTORE_PASSWORD)
                .loadTrustMaterial(caBundle, null)
                .build();
    }

    private static KeyStore createKeyStore(Certificate cert, Key key)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, RANDOM_RUN_TIME_KEYSTORE_PASSWORD);

        keyStore.setKeyEntry(
                "TlSKey", key, RANDOM_RUN_TIME_KEYSTORE_PASSWORD, new Certificate[] {cert});
        keyStore.setCertificateEntry("my-ca-1", cert);
        return keyStore;
    }

    private static KeyStore createTrustStore(Certificate[] certificates)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        int k = 0;
        for (Certificate cert : certificates) {
            k++;
            keyStore.setCertificateEntry("my-ca-" + k, cert);
        }
        return keyStore;
    }
}
