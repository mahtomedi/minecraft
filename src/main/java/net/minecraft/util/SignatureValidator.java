package net.minecraft.util;

import com.mojang.authlib.yggdrasil.ServicesKeyInfo;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.logging.LogUtils;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Collection;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public interface SignatureValidator {
    SignatureValidator NO_VALIDATION = (param0, param1) -> true;
    Logger LOGGER = LogUtils.getLogger();

    boolean validate(SignatureUpdater var1, byte[] var2);

    default boolean validate(byte[] param0, byte[] param1) {
        return this.validate((SignatureUpdater)(param1x -> param1x.update(param0)), param1);
    }

    private static boolean verifySignature(SignatureUpdater param0, byte[] param1, Signature param2) throws SignatureException {
        param0.update(param2::update);
        return param2.verify(param1);
    }

    static SignatureValidator from(PublicKey param0, String param1) {
        return (param2, param3) -> {
            try {
                Signature var1x = Signature.getInstance(param1);
                var1x.initVerify(param0);
                return verifySignature(param2, param3, var1x);
            } catch (Exception var5) {
                LOGGER.error("Failed to verify signature", (Throwable)var5);
                return false;
            }
        };
    }

    @Nullable
    static SignatureValidator from(ServicesKeySet param0, ServicesKeyType param1) {
        Collection<ServicesKeyInfo> var0 = param0.keys(param1);
        return var0.isEmpty() ? null : (param1x, param2) -> var0.stream().anyMatch(param2x -> {
                Signature var0x = param2x.signature();

                try {
                    return verifySignature(param1x, param2, var0x);
                } catch (SignatureException var5) {
                    LOGGER.error("Failed to verify Services signature", (Throwable)var5);
                    return false;
                }
            });
    }
}
