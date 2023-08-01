package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.client.RealmsError;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RetryCallException extends RealmsServiceException {
    public static final int DEFAULT_DELAY = 5;
    public final int delaySeconds;

    public RetryCallException(int param0, int param1) {
        super(RealmsError.CustomError.retry(param1));
        if (param0 >= 0 && param0 <= 120) {
            this.delaySeconds = param0;
        } else {
            this.delaySeconds = 5;
        }

    }
}
