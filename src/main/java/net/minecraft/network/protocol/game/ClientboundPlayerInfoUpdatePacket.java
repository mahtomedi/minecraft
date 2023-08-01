package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class ClientboundPlayerInfoUpdatePacket implements Packet<ClientGamePacketListener> {
    private final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions;
    private final List<ClientboundPlayerInfoUpdatePacket.Entry> entries;

    public ClientboundPlayerInfoUpdatePacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> param0, Collection<ServerPlayer> param1) {
        this.actions = param0;
        this.entries = param1.stream().map(ClientboundPlayerInfoUpdatePacket.Entry::new).toList();
    }

    public ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action param0, ServerPlayer param1) {
        this.actions = EnumSet.of(param0);
        this.entries = List.of(new ClientboundPlayerInfoUpdatePacket.Entry(param1));
    }

    public static ClientboundPlayerInfoUpdatePacket createPlayerInitializing(Collection<ServerPlayer> param0) {
        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> var0 = EnumSet.of(
            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
            ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
        );
        return new ClientboundPlayerInfoUpdatePacket(var0, param0);
    }

    public ClientboundPlayerInfoUpdatePacket(FriendlyByteBuf param0) {
        this.actions = param0.readEnumSet(ClientboundPlayerInfoUpdatePacket.Action.class);
        this.entries = param0.readList(param0x -> {
            ClientboundPlayerInfoUpdatePacket.EntryBuilder var0 = new ClientboundPlayerInfoUpdatePacket.EntryBuilder(param0x.readUUID());

            for(ClientboundPlayerInfoUpdatePacket.Action var1x : this.actions) {
                var1x.reader.read(var0, param0x);
            }

            return var0.build();
        });
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnumSet(this.actions, ClientboundPlayerInfoUpdatePacket.Action.class);
        param0.writeCollection(this.entries, (param0x, param1) -> {
            param0x.writeUUID(param1.profileId());

            for(ClientboundPlayerInfoUpdatePacket.Action var0 : this.actions) {
                var0.writer.write(param0x, param1);
            }

        });
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerInfoUpdate(this);
    }

    public EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions() {
        return this.actions;
    }

    public List<ClientboundPlayerInfoUpdatePacket.Entry> entries() {
        return this.entries;
    }

    public List<ClientboundPlayerInfoUpdatePacket.Entry> newEntries() {
        return this.actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER) ? this.entries : List.of();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("actions", this.actions).add("entries", this.entries).toString();
    }

    public static enum Action {
        ADD_PLAYER((param0, param1) -> {
            GameProfile var0 = new GameProfile(param0.profileId, param1.readUtf(16));
            var0.getProperties().putAll(param1.readGameProfileProperties());
            param0.profile = var0;
        }, (param0, param1) -> {
            GameProfile var0 = Objects.requireNonNull(param1.profile());
            param0.writeUtf(var0.getName(), 16);
            param0.writeGameProfileProperties(var0.getProperties());
        }),
        INITIALIZE_CHAT(
            (param0, param1) -> param0.chatSession = param1.readNullable(RemoteChatSession.Data::read),
            (param0, param1) -> param0.writeNullable(param1.chatSession, RemoteChatSession.Data::write)
        ),
        UPDATE_GAME_MODE(
            (param0, param1) -> param0.gameMode = GameType.byId(param1.readVarInt()), (param0, param1) -> param0.writeVarInt(param1.gameMode().getId())
        ),
        UPDATE_LISTED((param0, param1) -> param0.listed = param1.readBoolean(), (param0, param1) -> param0.writeBoolean(param1.listed())),
        UPDATE_LATENCY((param0, param1) -> param0.latency = param1.readVarInt(), (param0, param1) -> param0.writeVarInt(param1.latency())),
        UPDATE_DISPLAY_NAME(
            (param0, param1) -> param0.displayName = param1.readNullable(FriendlyByteBuf::readComponent),
            (param0, param1) -> param0.writeNullable(param1.displayName(), FriendlyByteBuf::writeComponent)
        );

        final ClientboundPlayerInfoUpdatePacket.Action.Reader reader;
        final ClientboundPlayerInfoUpdatePacket.Action.Writer writer;

        private Action(ClientboundPlayerInfoUpdatePacket.Action.Reader param0, ClientboundPlayerInfoUpdatePacket.Action.Writer param1) {
            this.reader = param0;
            this.writer = param1;
        }

        public interface Reader {
            void read(ClientboundPlayerInfoUpdatePacket.EntryBuilder var1, FriendlyByteBuf var2);
        }

        public interface Writer {
            void write(FriendlyByteBuf var1, ClientboundPlayerInfoUpdatePacket.Entry var2);
        }
    }

    public static record Entry(
        UUID profileId,
        @Nullable GameProfile profile,
        boolean listed,
        int latency,
        GameType gameMode,
        @Nullable Component displayName,
        @Nullable RemoteChatSession.Data chatSession
    ) {
        Entry(ServerPlayer param0) {
            this(
                param0.getUUID(),
                param0.getGameProfile(),
                true,
                param0.connection.latency(),
                param0.gameMode.getGameModeForPlayer(),
                param0.getTabListDisplayName(),
                Optionull.map(param0.getChatSession(), RemoteChatSession::asData)
            );
        }
    }

    static class EntryBuilder {
        final UUID profileId;
        @Nullable
        GameProfile profile;
        boolean listed;
        int latency;
        GameType gameMode = GameType.DEFAULT_MODE;
        @Nullable
        Component displayName;
        @Nullable
        RemoteChatSession.Data chatSession;

        EntryBuilder(UUID param0) {
            this.profileId = param0;
        }

        ClientboundPlayerInfoUpdatePacket.Entry build() {
            return new ClientboundPlayerInfoUpdatePacket.Entry(
                this.profileId, this.profile, this.listed, this.latency, this.gameMode, this.displayName, this.chatSession
            );
        }
    }
}
