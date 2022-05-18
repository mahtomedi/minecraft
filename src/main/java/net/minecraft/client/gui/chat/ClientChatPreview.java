package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ClientChatPreview {
    private static final long PREVIEW_VALID_AFTER_MS = 200L;
    private boolean enabled;
    @Nullable
    private String lastQuery;
    @Nullable
    private String scheduledRequest;
    private final ChatPreviewRequests requests;
    @Nullable
    private ClientChatPreview.Preview preview;

    public ClientChatPreview(Minecraft param0) {
        this.requests = new ChatPreviewRequests(param0);
    }

    public void tick() {
        String var0 = this.scheduledRequest;
        if (var0 != null && this.requests.trySendRequest(var0, Util.getMillis())) {
            this.scheduledRequest = null;
        }

    }

    public void update(String param0) {
        this.enabled = true;
        param0 = normalizeQuery(param0);
        if (!param0.isEmpty()) {
            if (!param0.equals(this.lastQuery)) {
                this.lastQuery = param0;
                this.sendOrScheduleRequest(param0);
            }
        } else {
            this.clear();
        }

    }

    private void sendOrScheduleRequest(String param0) {
        if (!this.requests.trySendRequest(param0, Util.getMillis())) {
            this.scheduledRequest = param0;
        } else {
            this.scheduledRequest = null;
        }

    }

    public void disable() {
        this.enabled = false;
        this.clear();
    }

    private void clear() {
        this.lastQuery = null;
        this.scheduledRequest = null;
        this.preview = null;
        this.requests.clear();
    }

    public void handleResponse(int param0, @Nullable Component param1) {
        String var0 = this.requests.handleResponse(param0);
        if (var0 != null) {
            Component var1 = (Component)(param1 != null ? param1 : Component.literal(var0));
            this.preview = new ClientChatPreview.Preview(Util.getMillis(), var0, var1);
        }

    }

    @Nullable
    public Component peek() {
        return Util.mapNullable(this.preview, ClientChatPreview.Preview::response);
    }

    @Nullable
    public Component pull(String param0) {
        if (this.preview != null && this.preview.canPull(param0)) {
            Component var0 = this.preview.response();
            this.preview = null;
            return var0;
        } else {
            return null;
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    static String normalizeQuery(String param0) {
        return StringUtils.normalizeSpace(param0.trim());
    }

    @OnlyIn(Dist.CLIENT)
    static record Preview(long receivedTimeStamp, String query, @Nullable Component response) {
        public Preview(long param0, String param1, @Nullable Component param2) {
            param1 = ClientChatPreview.normalizeQuery(param1);
            this.receivedTimeStamp = param0;
            this.query = param1;
            this.response = param2;
        }

        public boolean canPull(String param0) {
            if (this.query.equals(ClientChatPreview.normalizeQuery(param0))) {
                long var0 = this.receivedTimeStamp + 200L;
                return Util.getMillis() >= var0;
            } else {
                return false;
            }
        }
    }
}
