package com.mojang.realmsclient.exception;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RetryCallException extends RealmsServiceException {
    public final int delaySeconds;

    public RetryCallException(int param0) {
        super(503, "Retry operation", -1, "");
        if (param0 >= 0 && param0 <= 120) {
            this.delaySeconds = param0;
        } else {
            this.delaySeconds = 5;
        }

    }
}
