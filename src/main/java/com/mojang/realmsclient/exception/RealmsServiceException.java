package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.client.RealmsError;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsServiceException extends Exception {
    public final int httpResultCode;
    public final String httpResponseContent;
    public final int errorCode;
    public final String errorMsg;

    public RealmsServiceException(int param0, String param1, RealmsError param2) {
        super(param1);
        this.httpResultCode = param0;
        this.httpResponseContent = param1;
        this.errorCode = param2.getErrorCode();
        this.errorMsg = param2.getErrorMessage();
    }

    public RealmsServiceException(int param0, String param1, int param2, String param3) {
        super(param1);
        this.httpResultCode = param0;
        this.httpResponseContent = param1;
        this.errorCode = param2;
        this.errorMsg = param3;
    }

    @Override
    public String toString() {
        if (this.errorCode == -1) {
            return "Realms (" + this.httpResultCode + ") " + this.httpResponseContent;
        } else {
            String var0 = "mco.errorMessage." + this.errorCode;
            String var1 = I18n.get(var0);
            return (var1.equals(var0) ? this.errorMsg : var1) + " - " + this.errorCode;
        }
    }
}
