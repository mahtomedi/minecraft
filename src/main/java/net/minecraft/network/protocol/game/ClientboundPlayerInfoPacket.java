package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.GameType;

public class ClientboundPlayerInfoPacket implements Packet<ClientGamePacketListener> {
    private final ClientboundPlayerInfoPacket.Action action;
    private final List<ClientboundPlayerInfoPacket.PlayerUpdate> entries;

    public ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action param0, ServerPlayer... param1) {
        this.action = param0;
        this.entries = Lists.newArrayListWithCapacity(param1.length);

        for(ServerPlayer var0 : param1) {
            this.entries.add(createPlayerUpdate(var0));
        }

    }

    public ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action param0, Collection<ServerPlayer> param1) {
        this.action = param0;
        this.entries = Lists.newArrayListWithCapacity(param1.size());

        for(ServerPlayer var0 : param1) {
            this.entries.add(createPlayerUpdate(var0));
        }

    }

    public ClientboundPlayerInfoPacket(FriendlyByteBuf param0) {
        this.action = param0.readEnum(ClientboundPlayerInfoPacket.Action.class);
        this.entries = param0.readList(this.action::read);
    }

    private static ClientboundPlayerInfoPacket.PlayerUpdate createPlayerUpdate(ServerPlayer param0) {
        ProfilePublicKey var0 = param0.getProfilePublicKey();
        ProfilePublicKey.Data var1 = var0 != null ? var0.data() : null;
        return new ClientboundPlayerInfoPacket.PlayerUpdate(
            param0.getGameProfile(), param0.latency, param0.gameMode.getGameModeForPlayer(), param0.getTabListDisplayName(), var1
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.action);
        param0.writeCollection(this.entries, this.action::write);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerInfo(this);
    }

    public List<ClientboundPlayerInfoPacket.PlayerUpdate> getEntries() {
        return this.entries;
    }

    public ClientboundPlayerInfoPacket.Action getAction() {
        return this.action;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("action", this.action).add("entries", this.entries).toString();
    }

    public static enum Action {
        ADD_PLAYER {
            @Override
            protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf param0) {
                GameProfile var0 = param0.readGameProfile();
                GameType var1 = GameType.byId(param0.readVarInt());
                int var2 = param0.readVarInt();
                Component var3 = param0.readNullable(FriendlyByteBuf::readComponent);
                ProfilePublicKey.Data var4 = param0.readNullable(ProfilePublicKey.Data::new);
                return new ClientboundPlayerInfoPacket.PlayerUpdate(var0, var2, var1, var3, var4);
            }

            @Override
            protected void write(FriendlyByteBuf param0, ClientboundPlayerInfoPacket.PlayerUpdate param1) {
                param0.writeGameProfile(param1.getProfile());
                param0.writeVarInt(param1.getGameMode().getId());
                param0.writeVarInt(param1.getLatency());
                param0.writeNullable(param1.getDisplayName(), FriendlyByteBuf::writeComponent);
                param0.writeNullable(param1.getProfilePublicKey(), (param0x, param1x) -> param1x.write(param0x));
            }
        },
        UPDATE_GAME_MODE {
            @Override
            protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf param0) {
                GameProfile var0 = new GameProfile(param0.readUUID(), null);
                GameType var1 = GameType.byId(param0.readVarInt());
                return new ClientboundPlayerInfoPacket.PlayerUpdate(var0, 0, var1, null, null);
            }

            @Override
            protected void write(FriendlyByteBuf param0, ClientboundPlayerInfoPacket.PlayerUpdate param1) {
                param0.writeUUID(param1.getProfile().getId());
                param0.writeVarInt(param1.getGameMode().getId());
            }
        },
        UPDATE_LATENCY {
            @Override
            protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf param0) {
                GameProfile var0 = new GameProfile(param0.readUUID(), null);
                int var1 = param0.readVarInt();
                return new ClientboundPlayerInfoPacket.PlayerUpdate(var0, var1, null, null, null);
            }

            @Override
            protected void write(FriendlyByteBuf param0, ClientboundPlayerInfoPacket.PlayerUpdate param1) {
                param0.writeUUID(param1.getProfile().getId());
                param0.writeVarInt(param1.getLatency());
            }
        },
        UPDATE_DISPLAY_NAME {
            @Override
            protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf param0) {
                GameProfile var0 = new GameProfile(param0.readUUID(), null);
                Component var1 = param0.readNullable(FriendlyByteBuf::readComponent);
                return new ClientboundPlayerInfoPacket.PlayerUpdate(var0, 0, null, var1, null);
            }

            @Override
            protected void write(FriendlyByteBuf param0, ClientboundPlayerInfoPacket.PlayerUpdate param1) {
                param0.writeUUID(param1.getProfile().getId());
                param0.writeNullable(param1.getDisplayName(), FriendlyByteBuf::writeComponent);
            }
        },
        REMOVE_PLAYER {
            @Override
            protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf param0) {
                GameProfile var0 = new GameProfile(param0.readUUID(), null);
                return new ClientboundPlayerInfoPacket.PlayerUpdate(var0, 0, null, null, null);
            }

            @Override
            protected void write(FriendlyByteBuf param0, ClientboundPlayerInfoPacket.PlayerUpdate param1) {
                param0.writeUUID(param1.getProfile().getId());
            }
        };

        protected abstract ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf var1);

        protected abstract void write(FriendlyByteBuf var1, ClientboundPlayerInfoPacket.PlayerUpdate var2);
    }

    public static class PlayerUpdate {
        private final int latency;
        private final GameType gameMode;
        private final GameProfile profile;
        @Nullable
        private final Component displayName;
        @Nullable
        private final ProfilePublicKey.Data profilePublicKey;

        public PlayerUpdate(GameProfile param0, int param1, @Nullable GameType param2, @Nullable Component param3, @Nullable ProfilePublicKey.Data param4) {
            this.profile = param0;
            this.latency = param1;
            this.gameMode = param2;
            this.displayName = param3;
            this.profilePublicKey = param4;
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

        @Nullable
        public ProfilePublicKey.Data getProfilePublicKey() {
            return this.profilePublicKey;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("latency", this.latency)
                .add("gameMode", this.gameMode)
                .add("profile", this.profile)
                .add("displayName", this.displayName == null ? null : Component.Serializer.toJson(this.displayName))
                .add("profilePublicKey", this.profilePublicKey)
                .toString();
        }
    }
}
