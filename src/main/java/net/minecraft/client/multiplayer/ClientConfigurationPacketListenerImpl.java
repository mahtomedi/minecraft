package net.minecraft.client.multiplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientConfigurationPacketListenerImpl extends ClientCommonPacketListenerImpl implements TickablePacketListener, ClientConfigurationPacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GameProfile localGameProfile;
    private RegistryAccess.Frozen receivedRegistries;
    private FeatureFlagSet enabledFeatures;

    public ClientConfigurationPacketListenerImpl(Minecraft param0, Connection param1, CommonListenerCookie param2) {
        super(param0, param1, param2);
        this.localGameProfile = param2.localGameProfile();
        this.receivedRegistries = param2.receivedRegistries();
        this.enabledFeatures = param2.enabledFeatures();
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    @Override
    protected RegistryAccess.Frozen registryAccess() {
        return this.receivedRegistries;
    }

    @Override
    protected void handleCustomPayload(CustomPacketPayload param0) {
        this.handleUnknownCustomPayload(param0);
    }

    private void handleUnknownCustomPayload(CustomPacketPayload param0) {
        LOGGER.warn("Unknown custom packet payload: {}", param0.id());
    }

    @Override
    public void handleRegistryData(ClientboundRegistryDataPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        RegistryAccess.Frozen var0 = ClientRegistryLayer.createRegistryAccess()
            .replaceFrom(ClientRegistryLayer.REMOTE, param0.registryHolder())
            .compositeAccess();
        if (!this.connection.isMemoryConnection()) {
            var0.registries().forEach(param0x -> param0x.value().resetTags());
        }

        this.receivedRegistries = var0;
    }

    @Override
    public void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket param0) {
        this.enabledFeatures = FeatureFlags.REGISTRY.fromNames(param0.features());
    }

    @Override
    public void handleConfigurationFinished(ClientboundFinishConfigurationPacket param0) {
        this.connection.suspendInboundAfterProtocolChange();
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.connection
            .setListener(
                new ClientPacketListener(
                    this.minecraft,
                    this.connection,
                    new CommonListenerCookie(
                        this.localGameProfile,
                        this.telemetryManager,
                        this.receivedRegistries,
                        this.enabledFeatures,
                        this.serverBrand,
                        this.serverData,
                        this.postDisconnectScreen
                    )
                )
            );
        this.connection.resumeInboundAfterProtocolChange();
        this.connection.send(new ServerboundFinishConfigurationPacket());
    }

    @Override
    public void tick() {
        this.sendDeferredPackets();
    }

    @Override
    public void onDisconnect(Component param0) {
        super.onDisconnect(param0);
        this.minecraft.clearDownloadedResourcePacks();
    }
}
