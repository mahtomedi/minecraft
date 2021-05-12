package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class ClientboundPlayerInfoPacket implements Packet<ClientGamePacketListener> {
    private final ClientboundPlayerInfoPacket.Action action;
    private final List<ClientboundPlayerInfoPacket.PlayerUpdate> entries;

    public ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action param0, ServerPlayer... param1) {
        this.action = param0;
        this.entries = Lists.newArrayListWithCapacity(param1.length);

        for(ServerPlayer var0 : param1) {
            this.entries
                .add(
                    new ClientboundPlayerInfoPacket.PlayerUpdate(
                        var0.getGameProfile(), var0.latency, var0.gameMode.getGameModeForPlayer(), var0.getTabListDisplayName()
                    )
                );
        }

    }

    public ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action param0, Collection<ServerPlayer> param1) {
        this.action = param0;
        this.entries = Lists.newArrayListWithCapacity(param1.size());

        for(ServerPlayer var0 : param1) {
            this.entries
                .add(
                    new ClientboundPlayerInfoPacket.PlayerUpdate(
                        var0.getGameProfile(), var0.latency, var0.gameMode.getGameModeForPlayer(), var0.getTabListDisplayName()
                    )
                );
        }

    }

    public ClientboundPlayerInfoPacket(FriendlyByteBuf param0) {
        this.action = param0.readEnum(ClientboundPlayerInfoPacket.Action.class);
        this.entries = param0.readList(this.action::read);
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

    @Nullable
    static Component readDisplayName(FriendlyByteBuf param0) {
        return param0.readBoolean() ? param0.readComponent() : null;
    }

    static void writeDisplayName(FriendlyByteBuf param0, @Nullable Component param1) {
        if (param1 == null) {
            param0.writeBoolean(false);
        } else {
            param0.writeBoolean(true);
            param0.writeComponent(param1);
        }

    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("action", this.action).add("entries", this.entries).toString();
    }

    public static enum Action {
        ADD_PLAYER {
            @Override
            protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf param0) {
                GameProfile var0 = new GameProfile(param0.readUUID(), param0.readUtf(16));
                PropertyMap var1 = var0.getProperties();
                param0.readWithCount(param1 -> {
                    String var0x = param1.readUtf();
                    String var1x = param1.readUtf();
                    if (param1.readBoolean()) {
                        String var2x = param1.readUtf();
                        var1.put(var0x, new Property(var0x, var1x, var2x));
                    } else {
                        var1.put(var0x, new Property(var0x, var1x));
                    }

                });
                GameType var2 = GameType.byId(param0.readVarInt());
                int var3 = param0.readVarInt();
                Component var4 = ClientboundPlayerInfoPacket.readDisplayName(param0);
                return new ClientboundPlayerInfoPacket.PlayerUpdate(var0, var3, var2, var4);
            }

            @Override
            protected void write(FriendlyByteBuf param0, ClientboundPlayerInfoPacket.PlayerUpdate param1) {
                param0.writeUUID(param1.getProfile().getId());
                param0.writeUtf(param1.getProfile().getName());
                param0.writeCollection(param1.getProfile().getProperties().values(), (param0x, param1x) -> {
                    param0x.writeUtf(param1x.getName());
                    param0x.writeUtf(param1x.getValue());
                    if (param1x.hasSignature()) {
                        param0x.writeBoolean(true);
                        param0x.writeUtf(param1x.getSignature());
                    } else {
                        param0x.writeBoolean(false);
                    }

                });
                param0.writeVarInt(param1.getGameMode().getId());
                param0.writeVarInt(param1.getLatency());
                ClientboundPlayerInfoPacket.writeDisplayName(param0, param1.getDisplayName());
            }
        },
        UPDATE_GAME_MODE {
            @Override
            protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf param0) {
                GameProfile var0 = new GameProfile(param0.readUUID(), null);
                GameType var1 = GameType.byId(param0.readVarInt());
                return new ClientboundPlayerInfoPacket.PlayerUpdate(var0, 0, var1, null);
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
                return new ClientboundPlayerInfoPacket.PlayerUpdate(var0, var1, null, null);
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
                Component var1 = ClientboundPlayerInfoPacket.readDisplayName(param0);
                return new ClientboundPlayerInfoPacket.PlayerUpdate(var0, 0, null, var1);
            }

            @Override
            protected void write(FriendlyByteBuf param0, ClientboundPlayerInfoPacket.PlayerUpdate param1) {
                param0.writeUUID(param1.getProfile().getId());
                ClientboundPlayerInfoPacket.writeDisplayName(param0, param1.getDisplayName());
            }
        },
        REMOVE_PLAYER {
            @Override
            protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf param0) {
                GameProfile var0 = new GameProfile(param0.readUUID(), null);
                return new ClientboundPlayerInfoPacket.PlayerUpdate(var0, 0, null, null);
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

        public PlayerUpdate(GameProfile param0, int param1, @Nullable GameType param2, @Nullable Component param3) {
            this.profile = param0;
            this.latency = param1;
            this.gameMode = param2;
            this.displayName = param3;
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
