package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.client.RealmsError;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsServiceException extends Exception {
    public final int httpResultCode;
    public final String rawResponse;
    @Nullable
    public final RealmsError realmsError;

    public RealmsServiceException(int param0, String param1, RealmsError param2) {
        super(param1);
        this.httpResultCode = param0;
        this.rawResponse = param1;
        this.realmsError = param2;
    }

    public RealmsServiceException(int param0, String param1) {
        super(param1);
        this.httpResultCode = param0;
        this.rawResponse = param1;
        this.realmsError = null;
    }

    @Override
    public String getMessage() {
        if (this.realmsError != null) {
            String var0 = "mco.errorMessage." + this.realmsError.getErrorCode();
            String var1 = I18n.exists(var0) ? I18n.get(var0) : this.realmsError.getErrorMessage();
            return String.format(Locale.ROOT, "Realms service error (%d/%d) %s", this.httpResultCode, this.realmsError.getErrorCode(), var1);
        } else {
            return String.format(Locale.ROOT, "Realms service error (%d) %s", this.httpResultCode, this.rawResponse);
        }
    }

    public int realmsErrorCodeOrDefault(int param0) {
        return this.realmsError != null ? this.realmsError.getErrorCode() : param0;
    }
}
