package net.minecraft.util;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Crypt {
    private static final Logger LOGGER = LogManager.getLogger();

    @OnlyIn(Dist.CLIENT)
    public static SecretKey generateSecretKey() {
        try {
            KeyGenerator var0 = KeyGenerator.getInstance("AES");
            var0.init(128);
            return var0.generateKey();
        } catch (NoSuchAlgorithmException var11) {
            throw new Error(var11);
        }
    }

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator var0 = KeyPairGenerator.getInstance("RSA");
            var0.initialize(1024);
            return var0.generateKeyPair();
        } catch (NoSuchAlgorithmException var11) {
            var11.printStackTrace();
            LOGGER.error("Key pair generation failed!");
            return null;
        }
    }

    public static byte[] digestData(String param0, PublicKey param1, SecretKey param2) {
        try {
            return digestData("SHA-1", param0.getBytes("ISO_8859_1"), param2.getEncoded(), param1.getEncoded());
        } catch (UnsupportedEncodingException var4) {
            var4.printStackTrace();
            return null;
        }
    }

    private static byte[] digestData(String param0, byte[]... param1) {
        try {
            MessageDigest var0 = MessageDigest.getInstance(param0);

            for(byte[] var1 : param1) {
                var0.update(var1);
            }

            return var0.digest();
        } catch (NoSuchAlgorithmException var7) {
            var7.printStackTrace();
            return null;
        }
    }

    public static PublicKey byteToPublicKey(byte[] param0) {
        try {
            EncodedKeySpec var0 = new X509EncodedKeySpec(param0);
            KeyFactory var1 = KeyFactory.getInstance("RSA");
            return var1.generatePublic(var0);
        } catch (NoSuchAlgorithmException var3) {
        } catch (InvalidKeySpecException var4) {
        }

        LOGGER.error("Public key reconstitute failed!");
        return null;
    }

    public static SecretKey decryptByteToSecretKey(PrivateKey param0, byte[] param1) {
        return new SecretKeySpec(decryptUsingKey(param0, param1), "AES");
    }

    @OnlyIn(Dist.CLIENT)
    public static byte[] encryptUsingKey(Key param0, byte[] param1) {
        return cipherData(1, param0, param1);
    }

    public static byte[] decryptUsingKey(Key param0, byte[] param1) {
        return cipherData(2, param0, param1);
    }

    private static byte[] cipherData(int param0, Key param1, byte[] param2) {
        try {
            return setupCipher(param0, param1.getAlgorithm(), param1).doFinal(param2);
        } catch (IllegalBlockSizeException var4) {
            var4.printStackTrace();
        } catch (BadPaddingException var5) {
            var5.printStackTrace();
        }

        LOGGER.error("Cipher data failed!");
        return null;
    }

    private static Cipher setupCipher(int param0, String param1, Key param2) {
        try {
            Cipher var0 = Cipher.getInstance(param1);
            var0.init(param0, param2);
            return var0;
        } catch (InvalidKeyException var4) {
            var4.printStackTrace();
        } catch (NoSuchAlgorithmException var5) {
            var5.printStackTrace();
        } catch (NoSuchPaddingException var6) {
            var6.printStackTrace();
        }

        LOGGER.error("Cipher creation failed!");
        return null;
    }

    public static Cipher getCipher(int param0, Key param1) {
        try {
            Cipher var0 = Cipher.getInstance("AES/CFB8/NoPadding");
            var0.init(param0, param1, new IvParameterSpec(param1.getEncoded()));
            return var0;
        } catch (GeneralSecurityException var3) {
            throw new RuntimeException(var3);
        }
    }
}
