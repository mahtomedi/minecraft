package net.minecraft.client.multiplayer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.ServerboundPacketListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class ClientCommonPacketListenerImpl implements ClientCommonPacketListener {
    private static final Component GENERIC_DISCONNECT_MESSAGE = Component.translatable("disconnect.lost");
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final Minecraft minecraft;
    protected final Connection connection;
    @Nullable
    protected final ServerData serverData;
    @Nullable
    protected String serverBrand;
    protected final WorldSessionTelemetryManager telemetryManager;
    @Nullable
    protected final Screen postDisconnectScreen;
    private final List<ClientCommonPacketListenerImpl.DeferredPacket> deferredPackets = new ArrayList<>();

    protected ClientCommonPacketListenerImpl(Minecraft param0, Connection param1, CommonListenerCookie param2) {
        this.minecraft = param0;
        this.connection = param1;
        this.serverData = param2.serverData();
        this.serverBrand = param2.serverBrand();
        this.telemetryManager = param2.telemetryManager();
        this.postDisconnectScreen = param2.postDisconnectScreen();
    }

    @Override
    public void handleKeepAlive(ClientboundKeepAlivePacket param0) {
        this.sendWhen(new ServerboundKeepAlivePacket(param0.getId()), () -> !RenderSystem.isFrozenAtPollEvents(), Duration.ofMinutes(1L));
    }

    @Override
    public void handlePing(ClientboundPingPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        this.send(new ServerboundPongPacket(param0.getId()));
    }

    @Override
    public void handleCustomPayload(ClientboundCustomPayloadPacket param0) {
        CustomPacketPayload var0 = param0.payload();
        if (!(var0 instanceof DiscardedPayload)) {
            PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
            if (var0 instanceof BrandPayload var1) {
                this.serverBrand = var1.brand();
                this.telemetryManager.onServerBrandReceived(var1.brand());
            } else {
                this.handleCustomPayload(var0);
            }

        }
    }

    protected abstract void handleCustomPayload(CustomPacketPayload var1);

    protected abstract RegistryAccess.Frozen registryAccess();

    @Override
    public void handleResourcePack(ClientboundResourcePackPacket param0) {
        URL var0 = parseResourcePackUrl(param0.getUrl());
        if (var0 == null) {
            this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
        } else {
            String var1 = param0.getHash();
            boolean var2 = param0.isRequired();
            if (this.serverData != null && this.serverData.getResourcePackStatus() == ServerData.ServerPackStatus.ENABLED) {
                this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                this.packApplicationCallback(this.minecraft.getDownloadedPackSource().downloadAndSelectResourcePack(var0, var1, true));
            } else if (this.serverData != null
                && this.serverData.getResourcePackStatus() != ServerData.ServerPackStatus.PROMPT
                && (!var2 || this.serverData.getResourcePackStatus() != ServerData.ServerPackStatus.DISABLED)) {
                this.send(ServerboundResourcePackPacket.Action.DECLINED);
                if (var2) {
                    this.connection.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                }
            } else {
                this.minecraft.execute(() -> this.showServerPackPrompt(var0, var1, var2, param0.getPrompt()));
            }

        }
    }

    private void showServerPackPrompt(URL param0, String param1, boolean param2, @Nullable Component param3) {
        Screen var0 = this.minecraft.screen;
        this.minecraft
            .setScreen(
                new ConfirmScreen(
                    param4 -> {
                        this.minecraft.setScreen(var0);
                        if (param4) {
                            if (this.serverData != null) {
                                this.serverData.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
                            }
            
                            this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                            this.packApplicationCallback(this.minecraft.getDownloadedPackSource().downloadAndSelectResourcePack(param0, param1, true));
                        } else {
                            this.send(ServerboundResourcePackPacket.Action.DECLINED);
                            if (param2) {
                                this.connection.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                            } else if (this.serverData != null) {
                                this.serverData.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
                            }
                        }
            
                        if (this.serverData != null) {
                            ServerList.saveSingleServer(this.serverData);
                        }
            
                    },
                    param2 ? Component.translatable("multiplayer.requiredTexturePrompt.line1") : Component.translatable("multiplayer.texturePrompt.line1"),
                    preparePackPrompt(
                        param2
                            ? Component.translatable("multiplayer.requiredTexturePrompt.line2").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                            : Component.translatable("multiplayer.texturePrompt.line2"),
                        param3
                    ),
                    param2 ? CommonComponents.GUI_PROCEED : CommonComponents.GUI_YES,
                    (Component)(param2 ? Component.translatable("menu.disconnect") : CommonComponents.GUI_NO)
                )
            );
    }

    private static Component preparePackPrompt(Component param0, @Nullable Component param1) {
        return (Component)(param1 == null ? param0 : Component.translatable("multiplayer.texturePrompt.serverPrompt", param0, param1));
    }

    @Nullable
    private static URL parseResourcePackUrl(String param0) {
        try {
            URL var0 = new URL(param0);
            String var1 = var0.getProtocol();
            return !"http".equals(var1) && !"https".equals(var1) ? null : var0;
        } catch (MalformedURLException var3) {
            return null;
        }
    }

    private void packApplicationCallback(CompletableFuture<?> param0) {
        param0.thenRun(() -> this.send(ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED)).exceptionally(param0x -> {
            this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
            return null;
        });
    }

    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket param0) {
        PacketUtils.ensureRunningOnSameThread(param0, this, this.minecraft);
        param0.getTags().forEach(this::updateTagsForRegistry);
    }

    private <T> void updateTagsForRegistry(ResourceKey<? extends Registry<? extends T>> param0x, TagNetworkSerialization.NetworkPayload param1) {
        if (!param1.isEmpty()) {
            Registry<T> var0 = this.registryAccess().<T>registry(param0x).orElseThrow(() -> new IllegalStateException("Unknown registry " + param0x));
            Map<TagKey<T>, List<Holder<T>>> var2 = new HashMap<>();
            TagNetworkSerialization.deserializeTagsFromNetwork(param0x, var0, param1, var2::put);
            var0.bindTags(var2);
        }
    }

    private void send(ServerboundResourcePackPacket.Action param0) {
        this.connection.send(new ServerboundResourcePackPacket(param0));
    }

    @Override
    public void handleDisconnect(ClientboundDisconnectPacket param0) {
        this.connection.disconnect(param0.getReason());
    }

    protected void sendDeferredPackets() {
        Iterator<ClientCommonPacketListenerImpl.DeferredPacket> var0 = this.deferredPackets.iterator();

        while(var0.hasNext()) {
            ClientCommonPacketListenerImpl.DeferredPacket var1 = var0.next();
            if (var1.sendCondition().getAsBoolean()) {
                this.send(var1.packet);
                var0.remove();
            } else if (var1.expirationTime() <= Util.getMillis()) {
                var0.remove();
            }
        }

    }

    public void send(Packet<?> param0) {
        this.connection.send(param0);
    }

    @Override
    public void onDisconnect(Component param0) {
        this.telemetryManager.onDisconnect();
        this.minecraft.disconnect(this.createDisconnectScreen(param0));
        LOGGER.warn("Client disconnected with reason: {}", param0.getString());
    }

    protected Screen createDisconnectScreen(Component param0) {
        Screen var0 = Objects.requireNonNullElseGet(this.postDisconnectScreen, () -> new JoinMultiplayerScreen(new TitleScreen()));
        return (Screen)(this.serverData != null && this.serverData.isRealm()
            ? new DisconnectedRealmsScreen(var0, GENERIC_DISCONNECT_MESSAGE, param0)
            : new DisconnectedScreen(var0, GENERIC_DISCONNECT_MESSAGE, param0));
    }

    @Nullable
    public String serverBrand() {
        return this.serverBrand;
    }

    private void sendWhen(Packet<? extends ServerboundPacketListener> param0, BooleanSupplier param1, Duration param2) {
        if (param1.getAsBoolean()) {
            this.send(param0);
        } else {
            this.deferredPackets.add(new ClientCommonPacketListenerImpl.DeferredPacket(param0, param1, Util.getMillis() + param2.toMillis()));
        }

    }

    @OnlyIn(Dist.CLIENT)
    static record DeferredPacket(Packet<? extends ServerboundPacketListener> packet, BooleanSupplier sendCondition, long expirationTime) {
    }
}
