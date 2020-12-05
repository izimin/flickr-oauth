package trrp.lab1.service;

import com.github.scribejava.core.model.OAuth1AccessToken;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class AesService {
    private File file;
    private SecretKeySpec key;
    private Cipher cipher;

    public AesService(String fileName) throws NoSuchAlgorithmException, NoSuchPaddingException {
        file = new File(fileName);
        initKey();
    }

    public AesService() throws NoSuchAlgorithmException, NoSuchPaddingException {
        file = new File("access_token.tt");
        initKey();
    }

    private void initKey() throws NoSuchPaddingException, NoSuchAlgorithmException {
        cipher = Cipher.getInstance("AES");
        this.key = new SecretKeySpec("TrrpLab1Flickr12".getBytes(), "AES");
    }

    public void encrypt(OAuth1AccessToken accessToken) throws InvalidKeyException, IOException, IllegalBlockSizeException {
        cipher.init(Cipher.ENCRYPT_MODE, key);

        SealedObject sealedObj = new SealedObject(accessToken, cipher);
        ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file.getAbsolutePath())));

        out.writeObject(sealedObj);
        out.close();
    }

    public OAuth1AccessToken decrypt() throws InvalidKeyException, IOException, ClassNotFoundException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.DECRYPT_MODE, key);

        SealedObject sealedObject;
        ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file.getAbsolutePath())));
        sealedObject = (SealedObject) in.readObject();
        OAuth1AccessToken accessToken = (OAuth1AccessToken) sealedObject.getObject(cipher);
        in.close();

        return accessToken;
    }

    public boolean isInitialised() {
        return file.exists();
    }

    public OAuth1AccessToken getAccessToken()
            throws InvalidKeyException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException, IOException {
        return decrypt();
    }

    public void setAccessToken(OAuth1AccessToken accessToken)
            throws InvalidKeyException, IOException, IllegalBlockSizeException {
        encrypt(accessToken);
    }

    public void clear() {
        file.delete();
    }
}
