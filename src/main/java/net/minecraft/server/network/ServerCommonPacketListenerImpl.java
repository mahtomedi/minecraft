package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.util.VisibleForDebug;
import org.slf4j.Logger;

public abstract class ServerCommonPacketListenerImpl implements ServerCommonPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int LATENCY_CHECK_INTERVAL = 15000;
    private static final Component TIMEOUT_DISCONNECTION_MESSAGE = Component.translatable("disconnect.timeout");
    protected final MinecraftServer server;
    protected final Connection connection;
    private long keepAliveTime;
    private boolean keepAlivePending;
    private long keepAliveChallenge;
    private int latency;
    private volatile boolean suspendFlushingOnServerThread = false;

    public ServerCommonPacketListenerImpl(MinecraftServer param0, Connection param1, CommonListenerCookie param2) {
        this.server = param0;
        this.connection = param1;
        this.keepAliveTime = Util.getMillis();
        this.latency = param2.latency();
    }

    @Override
    public void onDisconnect(Component param0) {
        if (this.isSingleplayerOwner()) {
            LOGGER.info("Stopping singleplayer server as player logged out");
            this.server.halt(false);
        }

    }

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket param0) {
        if (this.keepAlivePending && param0.getId() == this.keepAliveChallenge) {
            int var0 = (int)(Util.getMillis() - this.keepAliveTime);
            this.latency = (this.latency * 3 + var0) / 4;
            this.keepAlivePending = false;
        } else if (!this.isSingleplayerOwner()) {
            this.disconnect(TIMEOUT_DISCONNECTION_MESSAGE);
        }

    }

    @Override
    public void handlePong(ServerboundPongPacket param0) {
    }

    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket param0) {
    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.server);
        if (param0.action() == ServerboundResourcePackPacket.Action.DECLINED && this.server.isResourcePackRequired()) {
            LOGGER.info("Disconnecting {} due to resource pack {} rejection", this.playerProfile().getName(), param0.id());
            this.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
        }

    }

    protected void keepConnectionAlive() {
        this.server.getProfiler().push("keepAlive");
        long var0 = Util.getMillis();
        if (var0 - this.keepAliveTime >= 15000L) {
            if (this.keepAlivePending) {
                this.disconnect(TIMEOUT_DISCONNECTION_MESSAGE);
            } else {
                this.keepAlivePending = true;
                this.keepAliveTime = var0;
                this.keepAliveChallenge = var0;
                this.send(new ClientboundKeepAlivePacket(this.keepAliveChallenge));
            }
        }

        this.server.getProfiler().pop();
    }

    public void suspendFlushing() {
        this.suspendFlushingOnServerThread = true;
    }

    public void resumeFlushing() {
        this.suspendFlushingOnServerThread = false;
        this.connection.flushChannel();
    }

    public void send(Packet<?> param0) {
        this.send(param0, null);
    }

    public void send(Packet<?> param0, @Nullable PacketSendListener param1) {
        boolean var0 = !this.suspendFlushingOnServerThread || !this.server.isSameThread();

        try {
            this.connection.send(param0, param1, var0);
        } catch (Throwable var7) {
            CrashReport var2 = CrashReport.forThrowable(var7, "Sending packet");
            CrashReportCategory var3 = var2.addCategory("Packet being sent");
            var3.setDetail("Packet class", () -> param0.getClass().getCanonicalName());
            throw new ReportedException(var2);
        }
    }

    public void disconnect(Component param0) {
        this.connection.send(new ClientboundDisconnectPacket(param0), PacketSendListener.thenRun(() -> this.connection.disconnect(param0)));
        this.connection.setReadOnly();
        this.server.executeBlocking(this.connection::handleDisconnection);
    }

    protected boolean isSingleplayerOwner() {
        return this.server.isSingleplayerOwner(this.playerProfile());
    }

    protected abstract GameProfile playerProfile();

    @VisibleForDebug
    public GameProfile getOwner() {
        return this.playerProfile();
    }

    public int latency() {
        return this.latency;
    }

    protected CommonListenerCookie createCookie(ClientInformation param0) {
        return new CommonListenerCookie(this.playerProfile(), this.latency, param0);
    }
}
