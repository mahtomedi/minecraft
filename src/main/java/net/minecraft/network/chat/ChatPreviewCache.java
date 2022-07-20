package net.minecraft.network.chat;

import javax.annotation.Nullable;

public class ChatPreviewCache {
    @Nullable
    private ChatPreviewCache.Result result;

    public void set(String param0, Component param1) {
        this.result = new ChatPreviewCache.Result(param0, param1);
    }

    @Nullable
    public Component pull(String param0) {
        ChatPreviewCache.Result var0 = this.result;
        if (var0 != null && var0.matches(param0)) {
            this.result = null;
            return var0.preview();
        } else {
            return null;
        }
    }

    static record Result(String query, Component preview) {
        public boolean matches(String param0) {
            return this.query.equals(param0);
        }
    }
}
