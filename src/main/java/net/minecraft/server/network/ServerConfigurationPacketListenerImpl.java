package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nullable;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.config.JoinWorldTask;
import net.minecraft.server.network.config.ServerResourcePackConfigurationTask;
import net.minecraft.server.players.PlayerList;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.flag.FeatureFlags;
import org.slf4j.Logger;

public class ServerConfigurationPacketListenerImpl extends ServerCommonPacketListenerImpl implements TickablePacketListener, ServerConfigurationPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component DISCONNECT_REASON_INVALID_DATA = Component.translatable("multiplayer.disconnect.invalid_player_data");
    private final GameProfile gameProfile;
    private final Queue<ConfigurationTask> configurationTasks = new ConcurrentLinkedQueue<>();
    @Nullable
    private ConfigurationTask currentTask;
    private ClientInformation clientInformation;

    public ServerConfigurationPacketListenerImpl(MinecraftServer param0, Connection param1, CommonListenerCookie param2) {
        super(param0, param1, param2);
        this.gameProfile = param2.gameProfile();
        this.clientInformation = param2.clientInformation();
    }

    @Override
    protected GameProfile playerProfile() {
        return this.gameProfile;
    }

    @Override
    public void onDisconnect(Component param0) {
        LOGGER.info("{} lost connection: {}", this.gameProfile, param0.getString());
        super.onDisconnect(param0);
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public void startConfiguration() {
        this.send(new ClientboundCustomPayloadPacket(new BrandPayload(this.server.getServerModName())));
        LayeredRegistryAccess<RegistryLayer> var0 = this.server.registries();
        this.send(new ClientboundUpdateEnabledFeaturesPacket(FeatureFlags.REGISTRY.toNames(this.server.getWorldData().enabledFeatures())));
        this.send(new ClientboundRegistryDataPacket(new RegistryAccess.ImmutableRegistryAccess(RegistrySynchronization.networkedRegistries(var0)).freeze()));
        this.send(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(var0)));
        this.addOptionalTasks();
        this.configurationTasks.add(new JoinWorldTask());
        this.startNextTask();
    }

    public void returnToWorld() {
        this.configurationTasks.add(new JoinWorldTask());
        this.startNextTask();
    }

    private void addOptionalTasks() {
        this.server.getServerResourcePack().ifPresent(param0 -> this.configurationTasks.add(new ServerResourcePackConfigurationTask(param0)));
    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket param0) {
        this.clientInformation = param0.information();
    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket param0) {
        super.handleResourcePackResponse(param0);
        if (param0.getAction() != ServerboundResourcePackPacket.Action.ACCEPTED) {
            this.finishCurrentTask(ServerResourcePackConfigurationTask.TYPE);
        }

    }

    @Override
    public void handleConfigurationFinished(ServerboundFinishConfigurationPacket param0) {
        this.connection.suspendInboundAfterProtocolChange();
        PacketUtils.ensureRunningOnSameThread(param0, this, this.server);
        this.finishCurrentTask(JoinWorldTask.TYPE);

        try {
            PlayerList var0 = this.server.getPlayerList();
            if (var0.getPlayer(this.gameProfile.getId()) != null) {
                this.disconnect(PlayerList.DUPLICATE_LOGIN_DISCONNECT_MESSAGE);
                return;
            }

            Component var1 = var0.canPlayerLogin(this.connection.getRemoteAddress(), this.gameProfile);
            if (var1 != null) {
                this.disconnect(var1);
                return;
            }

            ServerPlayer var2 = var0.getPlayerForLogin(this.gameProfile, this.clientInformation);
            var0.placeNewPlayer(this.connection, var2, this.createCookie(this.clientInformation));
            this.connection.resumeInboundAfterProtocolChange();
        } catch (Exception var5) {
            LOGGER.error("Couldn't place player in world", (Throwable)var5);
            this.connection.send(new ClientboundDisconnectPacket(DISCONNECT_REASON_INVALID_DATA));
            this.connection.disconnect(DISCONNECT_REASON_INVALID_DATA);
        }

    }

    @Override
    public void tick() {
        this.keepConnectionAlive();
    }

    private void startNextTask() {
        if (this.currentTask != null) {
            throw new IllegalStateException("Task " + this.currentTask.type().id() + " has not finished yet");
        } else if (this.isAcceptingMessages()) {
            ConfigurationTask var0 = this.configurationTasks.poll();
            if (var0 != null) {
                this.currentTask = var0;
                var0.start(this::send);
            }

        }
    }

    private void finishCurrentTask(ConfigurationTask.Type param0) {
        ConfigurationTask.Type var0 = this.currentTask != null ? this.currentTask.type() : null;
        if (!param0.equals(var0)) {
            throw new IllegalStateException("Unexpected request for task finish, current task: " + var0 + ", requested: " + param0);
        } else {
            this.currentTask = null;
            this.startNextTask();
        }
    }
}
