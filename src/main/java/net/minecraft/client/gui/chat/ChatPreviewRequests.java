package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundChatPreviewPacket;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatPreviewRequests {
    private static final long MIN_REQUEST_INTERVAL_MS = 100L;
    private static final long MAX_REQUEST_INTERVAL_MS = 1000L;
    private final Minecraft minecraft;
    private final ChatPreviewRequests.QueryIdGenerator queryIdGenerator = new ChatPreviewRequests.QueryIdGenerator();
    @Nullable
    private ChatPreviewRequests.PendingPreview pending;
    private long lastRequestTime;

    public ChatPreviewRequests(Minecraft param0) {
        this.minecraft = param0;
    }

    public boolean trySendRequest(String param0, long param1) {
        ClientPacketListener var0 = this.minecraft.getConnection();
        if (var0 == null) {
            this.clear();
            return true;
        } else if (this.pending != null && this.pending.matches(param0)) {
            return true;
        } else if (!this.minecraft.isLocalServer() && !this.isRequestReady(param1)) {
            return false;
        } else {
            ChatPreviewRequests.PendingPreview var1 = new ChatPreviewRequests.PendingPreview(this.queryIdGenerator.next(), param0);
            this.pending = var1;
            this.lastRequestTime = param1;
            var0.send(new ServerboundChatPreviewPacket(var1.id(), var1.query()));
            return true;
        }
    }

    @Nullable
    public String handleResponse(int param0) {
        if (this.pending != null && this.pending.matches(param0)) {
            String var0 = this.pending.query;
            this.pending = null;
            return var0;
        } else {
            return null;
        }
    }

    private boolean isRequestReady(long param0) {
        long var0 = this.lastRequestTime + 100L;
        if (param0 < var0) {
            return false;
        } else {
            long var1 = this.lastRequestTime + 1000L;
            return this.pending == null || param0 >= var1;
        }
    }

    public void clear() {
        this.pending = null;
        this.lastRequestTime = 0L;
    }

    public boolean isPending() {
        return this.pending != null;
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
