package trrp.lab1.service;

import com.github.scribejava.apis.FlickrApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth10aService;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RequestExecutor {
    private Future<OAuth1AccessToken> accessTokenF;
    private OAuth1AccessToken accessToken;
    private OAuth10aService service;

    public RequestExecutor() throws IOException, ExecutionException, InterruptedException, URISyntaxException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, ClassNotFoundException, NoSuchPaddingException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyStoreException, KeyManagementException {
        CallbackServer cbs = new CallbackServer();
        AesService aesService = new AesService();

        cbs.start();
        service = new ServiceBuilder("f883b87cc98185f4b1af160cbfa68fa4")
                .apiSecret("0801318d66c893bc")
                .callback(cbs.getAuthUrl())
                .build(FlickrApi.instance());

        if (aesService.isInitialised()) {
            accessToken = aesService.getAccessToken();
        } else {
            authenticate(cbs);
            accessToken = accessTokenF.get();
            aesService.setAccessToken(accessToken);
        }
    }

    private void authenticate(CallbackServer cbs) throws InterruptedException, ExecutionException, IOException, URISyntaxException {
        OAuth1RequestToken requestToken = service.getRequestToken();
        accessTokenF = cbs.getOAuth1VerifierCF().thenApply(oauthVerifier -> {
            try {
                return service.getAccessToken(requestToken, oauthVerifier);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
        String authUrl = service.getAuthorizationUrl(requestToken);
        Desktop.getDesktop().browse(new URI(authUrl));
    }

    public Response execute(OAuthRequest request) throws ExecutionException, InterruptedException, IOException {
        service.signRequest(accessToken, request);
        return service.execute(request);
    }
}
