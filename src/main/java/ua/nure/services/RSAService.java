package ua.nure.services;

import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class RSAService {
    public String encryptString(String message, String keyString) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Key publicKey = getStringAsKey(keyString);

        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] secretMessageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);

        return Base64.getEncoder().encodeToString(encryptedMessageBytes);
    }

    public String decryptString(String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Key privateKey = getKey("private");
        byte[] messageBytes = Base64.getDecoder().decode(message);

        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decryptedMessageBytes = decryptCipher.doFinal(messageBytes);
        return new String(decryptedMessageBytes, StandardCharsets.UTF_8);
    }

    public Key getStringAsKey(String keyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return keyFactory.generatePublic(keySpec);
    }

    public String getKeyAsString(String filename) throws NoSuchAlgorithmException {
        Key key = getKey(filename);
        byte[] keyBytes = key.getEncoded();
        return Base64.getEncoder().encodeToString(keyBytes);
    }
    public Key getKey(String fileName) throws NoSuchAlgorithmException {
        Key key;

        try  {
            File keyFile = new File(fileName + ".key");
            byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            if (fileName.equals("public")) {
                EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                key = keyFactory.generatePublic(keySpec);
            } else if (fileName.equals("private")) {
                EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
                key = keyFactory.generatePrivate(keySpec);
            } else {
                key = null;
            }
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            generateKeys();
            key = getKey(fileName);
        }

        return key;
    }

    private void generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        int keysSize = 2048;
        generator.initialize(keysSize);
        KeyPair keyPair = generator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        saveKeys(privateKey, publicKey);
    }

    private void saveKeys(PrivateKey privateKey, PublicKey publicKey) {
        try (FileOutputStream fos = new FileOutputStream("public.key")) {
            fos.write(publicKey.getEncoded());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileOutputStream fos = new FileOutputStream("private.key")) {
            fos.write(privateKey.getEncoded());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
