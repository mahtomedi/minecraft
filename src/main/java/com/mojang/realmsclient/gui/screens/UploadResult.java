package com.mojang.realmsclient.gui.screens;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UploadResult {
    public final int statusCode;
    public final String errorMessage;

    public UploadResult(int param0, String param1) {
        this.statusCode = param0;
        this.errorMessage = param1;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private int statusCode = -1;
        private String errorMessage;

        public UploadResult.Builder withStatusCode(int param0) {
            this.statusCode = param0;
            return this;
        }

        public UploadResult.Builder withErrorMessage(String param0) {
            this.errorMessage = param0;
            return this;
        }

        public UploadResult build() {
            return new UploadResult(this.statusCode, this.errorMessage);
        }
    }
}
