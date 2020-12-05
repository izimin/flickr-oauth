package trrp.lab1.service;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.CompletableFuture;

public class CallbackServer {

    private final HttpsServer server;
    private final CompletableFuture<String> oAuth1VerifierCF = new CompletableFuture<>();

    public CallbackServer() throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        // setup the socket address
        InetSocketAddress address = new InetSocketAddress(8000);

        // initialise the HTTPS server
        server = HttpsServer.create(address, 0);
        SSLContext sslContext = SSLContext.getInstance("TLS");

        // initialise the keystore
        char[] password = "password".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream("testkey.jks");
        ks.load(fis, password);

        // setup the key manager factory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        // setup the trust manager factory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        // setup the HTTPS context and parameters
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        if (server != null) {
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext context = getSSLContext();
                        SSLEngine engine = context.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // Set the SSL parameters
                        SSLParameters sslParameters = context.getSupportedSSLParameters();
                        params.setSSLParameters(sslParameters);

                    } catch (Exception ex) {
                        System.out.println("Failed to create HTTPS port");
                    }
                }
            });

            server.createContext("/", new OAuth1CallbackHandler(this));
            server.setExecutor(null); // creates a default executor
        }
    }

    String getAuthUrl() {
        return "https://localhost:8000";
    }

    public void start() {
        server.start();
    }

    CompletableFuture<String> getOAuth1VerifierCF() {
        return oAuth1VerifierCF;
    }

    void setOAuth1Verifier(String value) {
        oAuth1VerifierCF.complete(value);
        server.stop(0);
    }
}
