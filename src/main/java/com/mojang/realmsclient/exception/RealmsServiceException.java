package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.client.RealmsError;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsServiceException extends Exception {
    public final RealmsError realmsError;

    public RealmsServiceException(RealmsError param0) {
        this.realmsError = param0;
    }

    @Override
    public String getMessage() {
        return this.realmsError.logMessage();
    }
}
