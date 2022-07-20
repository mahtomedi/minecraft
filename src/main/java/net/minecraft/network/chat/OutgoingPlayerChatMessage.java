package net.minecraft.network.chat;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.players.PlayerList;

public interface OutgoingPlayerChatMessage {
    Component serverContent();

    ClientboundPlayerChatPacket packetForPlayer(ServerPlayer var1, ChatType.Bound var2);

    void sendHeadersToRemainingPlayers(PlayerList var1);

    static OutgoingPlayerChatMessage create(PlayerChatMessage param0) {
        return (OutgoingPlayerChatMessage)(param0.signer().isSystem()
            ? new OutgoingPlayerChatMessage.NotTracked(param0)
            : new OutgoingPlayerChatMessage.Tracked(param0));
    }

    static FilteredText<OutgoingPlayerChatMessage> createFromFiltered(FilteredText<PlayerChatMessage> param0) {
        OutgoingPlayerChatMessage var0 = create(param0.raw());
        return param0.rebuildIfNeeded(var0, OutgoingPlayerChatMessage.NotTracked::new);
    }

    public static class NotTracked implements OutgoingPlayerChatMessage {
        private final PlayerChatMessage message;

        public NotTracked(PlayerChatMessage param0) {
            this.message = param0;
        }

        @Override
        public Component serverContent() {
            return this.message.serverContent();
        }

        @Override
        public ClientboundPlayerChatPacket packetForPlayer(ServerPlayer param0, ChatType.Bound param1) {
            RegistryAccess var0 = param0.level.registryAccess();
            ChatType.BoundNetwork var1 = param1.toNetwork(var0);
            return new ClientboundPlayerChatPacket(this.message, var1);
        }

        @Override
        public void sendHeadersToRemainingPlayers(PlayerList param0) {
        }
    }

    public static class Tracked implements OutgoingPlayerChatMessage {
        private final PlayerChatMessage message;
        private final Set<ServerPlayer> playersWithFullMessage = Sets.newIdentityHashSet();

        public Tracked(PlayerChatMessage param0) {
            this.message = param0;
        }

        @Override
        public Component serverContent() {
            return this.message.serverContent();
        }

        @Override
        public ClientboundPlayerChatPacket packetForPlayer(ServerPlayer param0, ChatType.Bound param1) {
            this.playersWithFullMessage.add(param0);
            RegistryAccess var0 = param0.level.registryAccess();
            ChatType.BoundNetwork var1 = param1.toNetwork(var0);
            return new ClientboundPlayerChatPacket(this.message, var1);
        }

        @Override
        public void sendHeadersToRemainingPlayers(PlayerList param0) {
            param0.broadcastMessageHeader(this.message, this.playersWithFullMessage);
        }
    }
}
