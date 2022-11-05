package ua.nure.services;

import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class RSAService {

    private Key getKey(String fileName) throws NoSuchAlgorithmException {
        Key key;

        try  {
            File keyFile = new File(fileName);
            byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

            if (fileName.contains("public")) {
                key = keyFactory.generatePublic(keySpec);
            } else if (fileName.contains("private")) {
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
