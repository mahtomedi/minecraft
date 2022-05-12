package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatPreviewPacket;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ClientChatPreview {
    private static final long MIN_REQUEST_INTERVAL_MS = 100L;
    private static final long MAX_REQUEST_INTERVAL_MS = 1000L;
    private static final long PREVIEW_VALID_AFTER_MS = 200L;
    private final Minecraft minecraft;
    private final ClientChatPreview.QueryIdGenerator queryIdGenerator = new ClientChatPreview.QueryIdGenerator();
    @Nullable
    private ClientChatPreview.PendingPreview scheduledPreview;
    @Nullable
    private ClientChatPreview.PendingPreview pendingPreview;
    private long lastRequestTime;
    @Nullable
    private ClientChatPreview.Preview preview;

    public ClientChatPreview(Minecraft param0) {
        this.minecraft = param0;
    }

    public void tick() {
        ClientChatPreview.PendingPreview var0 = this.scheduledPreview;
        if (var0 != null) {
            long var1 = Util.getMillis();
            if (this.isRequestReady(var1)) {
                this.sendRequest(var0, var1);
                this.scheduledPreview = null;
            }

        }
    }

    private void sendRequest(ClientChatPreview.PendingPreview param0, long param1) {
        ClientPacketListener var0 = this.minecraft.getConnection();
        if (var0 != null) {
            var0.send(new ServerboundChatPreviewPacket(param0.id(), param0.query()));
            this.pendingPreview = param0;
        } else {
            this.pendingPreview = null;
        }

        this.lastRequestTime = param1;
    }

    private boolean isRequestReady(long param0) {
        ClientPacketListener var0 = this.minecraft.getConnection();
        if (var0 == null) {
            return true;
        } else if (param0 < this.getEarliestNextRequest()) {
            return false;
        } else {
            return this.pendingPreview == null || param0 >= this.getLatestNextRequest();
        }
    }

    private long getEarliestNextRequest() {
        return this.lastRequestTime + 100L;
    }

    private long getLatestNextRequest() {
        return this.lastRequestTime + 1000L;
    }

    public void clear() {
        this.preview = null;
        this.scheduledPreview = null;
        this.pendingPreview = null;
    }

    public void request(String param0) {
        param0 = normalizeQuery(param0);
        if (param0.isEmpty()) {
            this.preview = new ClientChatPreview.Preview(Util.getMillis(), param0, null);
            this.scheduledPreview = null;
            this.pendingPreview = null;
        } else {
            ClientChatPreview.PendingPreview var0 = this.scheduledPreview != null ? this.scheduledPreview : this.pendingPreview;
            if (var0 == null || !var0.matches(param0)) {
                this.scheduledPreview = new ClientChatPreview.PendingPreview(this.queryIdGenerator.next(), param0);
            }

        }
    }

    public void handleResponse(int param0, @Nullable Component param1) {
        if (this.scheduledPreview != null || this.pendingPreview != null) {
            if (this.pendingPreview != null && this.pendingPreview.matches(param0)) {
                Component var0 = (Component)(param1 != null ? param1 : Component.literal(this.pendingPreview.query()));
                this.preview = new ClientChatPreview.Preview(Util.getMillis(), this.pendingPreview.query(), var0);
                this.pendingPreview = null;
            } else {
                this.preview = null;
            }

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

    public boolean isActive() {
        return this.preview != null || this.scheduledPreview != null || this.pendingPreview != null;
    }

    static String normalizeQuery(String param0) {
        return StringUtils.normalizeSpace(param0.trim());
    }

    @OnlyIn(Dist.CLIENT)
    static record PendingPreview(int id, String query) {
        public boolean matches(int param0) {
            return this.id == param0;
        }

        public boolean matches(String param0) {
            return this.query.equals(param0);
        }
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

    @OnlyIn(Dist.CLIENT)
    static class QueryIdGenerator {
        private static final int MAX_STEP = 100;
        private final RandomSource random = RandomSource.createNewThreadLocalInstance();
        private int lastId;

        public int next() {
            int var0 = this.lastId + this.random.nextInt(100);
            this.lastId = var0;
            return var0;
        }
    }
}
