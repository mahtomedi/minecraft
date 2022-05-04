package net.minecraft.util;

import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.network.FriendlyByteBuf;

public class Crypt {
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final int SYMMETRIC_BITS = 128;
    private static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final int ASYMMETRIC_BITS = 1024;
    private static final String BYTE_ENCODING = "ISO_8859_1";
    private static final String HASH_ALGORITHM = "SHA-1";
    public static final String SIGNING_ALGORITHM = "SHA256withRSA";
    private static final String PEM_RSA_PRIVATE_KEY_HEADER = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String PEM_RSA_PRIVATE_KEY_FOOTER = "-----END RSA PRIVATE KEY-----";
    public static final String RSA_PUBLIC_KEY_HEADER = "-----BEGIN RSA PUBLIC KEY-----";
    private static final String RSA_PUBLIC_KEY_FOOTER = "-----END RSA PUBLIC KEY-----";
    public static final String MIME_LINE_SEPARATOR = "\r\n";
    public static final Codec<PublicKey> PUBLIC_KEY_CODEC = Codec.STRING.comapFlatMap(param0 -> {
        try {
            return DataResult.success(stringToRsaPublicKey(param0));
        } catch (CryptException var2) {
            return DataResult.error(var2.getMessage());
        }
    }, Crypt::rsaPublicKeyToString);
    public static final Codec<PrivateKey> PRIVATE_KEY_CODEC = Codec.STRING.comapFlatMap(param0 -> {
        try {
            return DataResult.success(stringToPemRsaPrivateKey(param0));
        } catch (CryptException var2) {
            return DataResult.error(var2.getMessage());
        }
    }, Crypt::pemRsaPrivateKeyToString);

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

    private static <T extends Key> T rsaStringToKey(String param0, String param1, String param2, Crypt.ByteArrayToKeyFunction<T> param3) throws CryptException {
        int var0 = param0.indexOf(param1);
        if (var0 != -1) {
            var0 += param1.length();
            int var1 = param0.indexOf(param2, var0);
            param0 = param0.substring(var0, var1 + 1);
        }

        return param3.apply(Base64.getMimeDecoder().decode(param0));
    }

    public static PrivateKey stringToPemRsaPrivateKey(String param0) throws CryptException {
        return rsaStringToKey(param0, "-----BEGIN RSA PRIVATE KEY-----", "-----END RSA PRIVATE KEY-----", Crypt::byteToPrivateKey);
    }

    public static PublicKey stringToRsaPublicKey(String param0) throws CryptException {
        return rsaStringToKey(param0, "-----BEGIN RSA PUBLIC KEY-----", "-----END RSA PUBLIC KEY-----", Crypt::byteToPublicKey);
    }

    public static String rsaPublicKeyToString(PublicKey param0) {
        if (!"RSA".equals(param0.getAlgorithm())) {
            throw new IllegalArgumentException("Public key must be RSA");
        } else {
            return "-----BEGIN RSA PUBLIC KEY-----\r\n" + Base64.getMimeEncoder().encodeToString(param0.getEncoded()) + "\r\n-----END RSA PUBLIC KEY-----\r\n";
        }
    }

    public static String pemRsaPrivateKeyToString(PrivateKey param0) {
        if (!"RSA".equals(param0.getAlgorithm())) {
            throw new IllegalArgumentException("Private key must be RSA");
        } else {
            return "-----BEGIN RSA PRIVATE KEY-----\r\n"
                + Base64.getMimeEncoder().encodeToString(param0.getEncoded())
                + "\r\n-----END RSA PRIVATE KEY-----\r\n";
        }
    }

    private static PrivateKey byteToPrivateKey(byte[] param0x) throws CryptException {
        try {
            EncodedKeySpec var0 = new PKCS8EncodedKeySpec(param0x);
            KeyFactory var1 = KeyFactory.getInstance("RSA");
            return var1.generatePrivate(var0);
        } catch (Exception var3) {
            throw new CryptException(var3);
        }
    }

    public static PublicKey byteToPublicKey(byte[] param0x) throws CryptException {
        try {
            EncodedKeySpec var0 = new X509EncodedKeySpec(param0x);
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

    interface ByteArrayToKeyFunction<T extends Key> {
        T apply(byte[] var1) throws CryptException;
    }

    public static record SaltSignaturePair(long salt, byte[] signature) {
        public static final Crypt.SaltSignaturePair EMPTY = new Crypt.SaltSignaturePair(0L, ByteArrays.EMPTY_ARRAY);

        public SaltSignaturePair(FriendlyByteBuf param0) {
            this(param0.readLong(), param0.readByteArray());
        }

        public boolean isValid() {
            return this.signature.length > 0;
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeLong(this.salt);
            param0.writeByteArray(this.signature);
        }

        public byte[] saltAsBytes() {
            return Longs.toByteArray(this.salt);
        }
    }

    public static class SaltSupplier {
        private static final SecureRandom secureRandom = new SecureRandom();

        public static long getLong() {
            return secureRandom.nextLong();
        }
    }
}
