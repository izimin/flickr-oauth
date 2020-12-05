package trrp.lab1.service;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.google.gson.*;
import trrp.lab1.model.Comment;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Flickr {

    private RequestExecutor requestExecutor;

    public Flickr() throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, URISyntaxException, ExecutionException, InvalidKeyException, InterruptedException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException, UnrecoverableKeyException, CertificateException, KeyStoreException, KeyManagementException {
        requestExecutor = new RequestExecutor();
    }

    private String createUrl(String method, String format, String otherParams) {
        return String.format(
                "https://www.flickr.com/services/rest?" +
                        "method=%s&" +
                        "api_key=YOUR" +
                        "format=%s&" +
                        "nojsoncallback=1" +
                        "%s", method, format, otherParams);
    }

    public List<Comment> getComments(Long photoId) throws Exception {

        OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET,
                createUrl("flickr.photos.comments.getList", "json", "&photo_id=" + photoId));

        Response response = requestExecutor.execute(oAuthRequest);
        if (response.isSuccessful()) {
            JsonParser parser = new JsonParser();
            JsonArray comments = ((JsonObject) parser.parse(response.getBody())).getAsJsonObject("comments").getAsJsonArray("comment");
            Gson gson = new Gson();
            return Arrays.asList(gson.fromJson(comments, Comment[].class));
        } else {
            throw new Exception("Не удалось получить список комментариев :(");
        }
    }

    public String addComment(Long photoId, String commentText) throws Exception {

        OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET,
                createUrl("flickr.photos.comments.addComment", "json",
                        String.format("&photo_id=%d&comment_text=%s", photoId, commentText)));
        Response response = requestExecutor.execute(oAuthRequest);

        if (!response.isSuccessful()) {
            throw new Exception("Не удалось добавить комментарий :(");
        }

        JsonParser parser = new JsonParser();
        JsonElement commentId = ((JsonObject) parser.parse(response.getBody())).getAsJsonObject("comment").get("id");

        return commentId.toString();
    }

    public void editComment(Long photoId, String commentId, String commentText) throws Exception {

        OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET,
                createUrl("flickr.photos.comments.editComment", "json",
                        String.format("&photo_id=%d&comment_id=%s&comment_text=%s", photoId, commentId, commentText)));
        Response response = requestExecutor.execute(oAuthRequest);

        if (!response.isSuccessful()) {
            throw new Exception("Не удалось изменить комментарий :(");
        }
    }

    public void deleteComment(String commentId) throws Exception {
        OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET,
                createUrl("flickr.photos.comments.deleteComment", "json", "&comment_id=" + commentId));
        Response response = requestExecutor.execute(oAuthRequest);

        if (!response.isSuccessful()) {
            throw new Exception("Не удалось удалить комментарий :(");
        }
    }
}
