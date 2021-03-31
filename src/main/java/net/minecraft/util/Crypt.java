package net.minecraft.util;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypt {
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final int SYMMETRIC_BITS = 128;
    private static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final int ASYMMETRIC_BITS = 1024;
    private static final String BYTE_ENCODING = "ISO_8859_1";
    private static final String HASH_ALGORITHM = "SHA-1";

    public static SecretKey generateSecretKey() throws CryptException {
        try {
            KeyGenerator var0 = KeyGenerator.getInstance("AES");
            var0.init(128);
            return var0.generateKey();
        } catch (Exception var11) {
            throw new CryptException(var11);
        }
    }

    public static KeyPair generateKeyPair() throws CryptException {
        try {
            KeyPairGenerator var0 = KeyPairGenerator.getInstance("RSA");
            var0.initialize(1024);
            return var0.generateKeyPair();
        } catch (Exception var11) {
            throw new CryptException(var11);
        }
    }

    public static byte[] digestData(String param0, PublicKey param1, SecretKey param2) throws CryptException {
        try {
            return digestData(param0.getBytes("ISO_8859_1"), param2.getEncoded(), param1.getEncoded());
        } catch (Exception var4) {
            throw new CryptException(var4);
        }
    }

    private static byte[] digestData(byte[]... param0) throws Exception {
        MessageDigest var0 = MessageDigest.getInstance("SHA-1");

        for(byte[] var1 : param0) {
            var0.update(var1);
        }

        return var0.digest();
    }

    public static PublicKey byteToPublicKey(byte[] param0) throws CryptException {
        try {
            EncodedKeySpec var0 = new X509EncodedKeySpec(param0);
            KeyFactory var1 = KeyFactory.getInstance("RSA");
            return var1.generatePublic(var0);
        } catch (Exception var3) {
            throw new CryptException(var3);
        }
    }

    public static SecretKey decryptByteToSecretKey(PrivateKey param0, byte[] param1) throws CryptException {
        byte[] var0 = decryptUsingKey(param0, param1);

        try {
            return new SecretKeySpec(var0, "AES");
        } catch (Exception var4) {
            throw new CryptException(var4);
        }
    }

    public static byte[] encryptUsingKey(Key param0, byte[] param1) throws CryptException {
        return cipherData(1, param0, param1);
    }

    public static byte[] decryptUsingKey(Key param0, byte[] param1) throws CryptException {
        return cipherData(2, param0, param1);
    }

    private static byte[] cipherData(int param0, Key param1, byte[] param2) throws CryptException {
        try {
            return setupCipher(param0, param1.getAlgorithm(), param1).doFinal(param2);
        } catch (Exception var4) {
            throw new CryptException(var4);
        }
    }

    private static Cipher setupCipher(int param0, String param1, Key param2) throws Exception {
        Cipher var0 = Cipher.getInstance(param1);
        var0.init(param0, param2);
        return var0;
    }

    public static Cipher getCipher(int param0, Key param1) throws CryptException {
        try {
            Cipher var0 = Cipher.getInstance("AES/CFB8/NoPadding");
            var0.init(param0, param1, new IvParameterSpec(param1.getEncoded()));
            return var0;
        } catch (Exception var3) {
            throw new CryptException(var3);
        }
    }
}
