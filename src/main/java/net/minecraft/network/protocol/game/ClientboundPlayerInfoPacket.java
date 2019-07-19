package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundPlayerInfoPacket implements Packet<ClientGamePacketListener> {
    private ClientboundPlayerInfoPacket.Action action;
    private final List<ClientboundPlayerInfoPacket.PlayerUpdate> entries = Lists.newArrayList();

    public ClientboundPlayerInfoPacket() {
    }

    public ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action param0, ServerPlayer... param1) {
        this.action = param0;

        for(ServerPlayer var0 : param1) {
            this.entries
                .add(
                    new ClientboundPlayerInfoPacket.PlayerUpdate(
                        var0.getGameProfile(), var0.latency, var0.gameMode.getGameModeForPlayer(), var0.getTabListDisplayName()
                    )
                );
        }

    }

    public ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action param0, Iterable<ServerPlayer> param1) {
        this.action = param0;

        for(ServerPlayer var0 : param1) {
            this.entries
                .add(
                    new ClientboundPlayerInfoPacket.PlayerUpdate(
                        var0.getGameProfile(), var0.latency, var0.gameMode.getGameModeForPlayer(), var0.getTabListDisplayName()
                    )
                );
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.action = param0.readEnum(ClientboundPlayerInfoPacket.Action.class);
        int var0 = param0.readVarInt();

        for(int var1 = 0; var1 < var0; ++var1) {
            GameProfile var2 = null;
            int var3 = 0;
            GameType var4 = null;
            Component var5 = null;
            switch(this.action) {
                case ADD_PLAYER:
                    var2 = new GameProfile(param0.readUUID(), param0.readUtf(16));
                    int var6 = param0.readVarInt();
                    int var7 = 0;

                    for(; var7 < var6; ++var7) {
                        String var8 = param0.readUtf(32767);
                        String var9 = param0.readUtf(32767);
                        if (param0.readBoolean()) {
                            var2.getProperties().put(var8, new Property(var8, var9, param0.readUtf(32767)));
                        } else {
                            var2.getProperties().put(var8, new Property(var8, var9));
                        }
                    }

                    var4 = GameType.byId(param0.readVarInt());
                    var3 = param0.readVarInt();
                    if (param0.readBoolean()) {
                        var5 = param0.readComponent();
                    }
                    break;
                case UPDATE_GAME_MODE:
                    var2 = new GameProfile(param0.readUUID(), null);
                    var4 = GameType.byId(param0.readVarInt());
                    break;
                case UPDATE_LATENCY:
                    var2 = new GameProfile(param0.readUUID(), null);
                    var3 = param0.readVarInt();
                    break;
                case UPDATE_DISPLAY_NAME:
                    var2 = new GameProfile(param0.readUUID(), null);
                    if (param0.readBoolean()) {
                        var5 = param0.readComponent();
                    }
                    break;
                case REMOVE_PLAYER:
                    var2 = new GameProfile(param0.readUUID(), null);
            }

            this.entries.add(new ClientboundPlayerInfoPacket.PlayerUpdate(var2, var3, var4, var5));
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeEnum(this.action);
        param0.writeVarInt(this.entries.size());

        for(ClientboundPlayerInfoPacket.PlayerUpdate var0 : this.entries) {
            switch(this.action) {
                case ADD_PLAYER:
                    param0.writeUUID(var0.getProfile().getId());
                    param0.writeUtf(var0.getProfile().getName());
                    param0.writeVarInt(var0.getProfile().getProperties().size());

                    for(Property var1 : var0.getProfile().getProperties().values()) {
                        param0.writeUtf(var1.getName());
                        param0.writeUtf(var1.getValue());
                        if (var1.hasSignature()) {
                            param0.writeBoolean(true);
                            param0.writeUtf(var1.getSignature());
                        } else {
                            param0.writeBoolean(false);
                        }
                    }

                    param0.writeVarInt(var0.getGameMode().getId());
                    param0.writeVarInt(var0.getLatency());
                    if (var0.getDisplayName() == null) {
                        param0.writeBoolean(false);
                    } else {
                        param0.writeBoolean(true);
                        param0.writeComponent(var0.getDisplayName());
                    }
                    break;
                case UPDATE_GAME_MODE:
                    param0.writeUUID(var0.getProfile().getId());
                    param0.writeVarInt(var0.getGameMode().getId());
                    break;
                case UPDATE_LATENCY:
                    param0.writeUUID(var0.getProfile().getId());
                    param0.writeVarInt(var0.getLatency());
                    break;
                case UPDATE_DISPLAY_NAME:
                    param0.writeUUID(var0.getProfile().getId());
                    if (var0.getDisplayName() == null) {
                        param0.writeBoolean(false);
                    } else {
                        param0.writeBoolean(true);
                        param0.writeComponent(var0.getDisplayName());
                    }
                    break;
                case REMOVE_PLAYER:
                    param0.writeUUID(var0.getProfile().getId());
            }
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerInfo(this);
    }

    @OnlyIn(Dist.CLIENT)
    public List<ClientboundPlayerInfoPacket.PlayerUpdate> getEntries() {
        return this.entries;
    }

    @OnlyIn(Dist.CLIENT)
    public ClientboundPlayerInfoPacket.Action getAction() {
        return this.action;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("action", this.action).add("entries", this.entries).toString();
    }

    public static enum Action {
        ADD_PLAYER,
        UPDATE_GAME_MODE,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME,
        REMOVE_PLAYER;
    }

    public class PlayerUpdate {
        private final int latency;
        private final GameType gameMode;
        private final GameProfile profile;
        private final Component displayName;

        public PlayerUpdate(GameProfile param1, int param2, @Nullable GameType param3, @Nullable Component param4) {
            this.profile = param1;
            this.latency = param2;
            this.gameMode = param3;
            this.displayName = param4;
        }

        public GameProfile getProfile() {
            return this.profile;
        }

        public int getLatency() {
            return this.latency;
        }

        public GameType getGameMode() {
            return this.gameMode;
        }

        @Nullable
        public Component getDisplayName() {
            return this.displayName;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("latency", this.latency)
                .add("gameMode", this.gameMode)
                .add("profile", this.profile)
                .add("displayName", this.displayName == null ? null : Component.Serializer.toJson(this.displayName))
                .toString();
        }
    }
}
