package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.security.PrivateKey;
import java.security.Signature;
import org.slf4j.Logger;

public interface Signer {
    Logger LOGGER = LogUtils.getLogger();

    byte[] sign(SignatureUpdater var1);

    default byte[] sign(byte[] param0) {
        return this.sign((SignatureUpdater)(param1 -> param1.update(param0)));
    }

    static Signer from(PrivateKey param0, String param1) {
        return param2 -> {
            try {
                Signature var1x = Signature.getInstance(param1);
                var1x.initSign(param0);
                param2.update(var1x::update);
                return var1x.sign();
            } catch (Exception var4) {
                throw new IllegalStateException("Failed to sign message", var4);
            }
        };
    }
}
