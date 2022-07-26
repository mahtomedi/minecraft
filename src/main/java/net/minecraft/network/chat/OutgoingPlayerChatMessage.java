package net.minecraft.network.chat;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.game.ClientboundPlayerChatHeaderPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public interface OutgoingPlayerChatMessage {
    Component serverContent();

    void sendToPlayer(ServerPlayer var1, boolean var2, ChatType.Bound var3);

    void sendHeadersToRemainingPlayers(PlayerList var1);

    static OutgoingPlayerChatMessage create(PlayerChatMessage param0) {
        return (OutgoingPlayerChatMessage)(param0.signer().isSystem()
            ? new OutgoingPlayerChatMessage.NotTracked(param0)
            : new OutgoingPlayerChatMessage.Tracked(param0));
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
        public void sendToPlayer(ServerPlayer param0, boolean param1, ChatType.Bound param2) {
            PlayerChatMessage var0 = this.message.filter(param1);
            if (!var0.isFullyFiltered()) {
                RegistryAccess var1 = param0.level.registryAccess();
                ChatType.BoundNetwork var2 = param2.toNetwork(var1);
                param0.connection.send(new ClientboundPlayerChatPacket(var0, var2));
                param0.connection.addPendingMessage(var0);
            }

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
        public void sendToPlayer(ServerPlayer param0, boolean param1, ChatType.Bound param2) {
            PlayerChatMessage var0 = this.message.filter(param1);
            if (!var0.isFullyFiltered()) {
                this.playersWithFullMessage.add(param0);
                RegistryAccess var1 = param0.level.registryAccess();
                ChatType.BoundNetwork var2 = param2.toNetwork(var1);
                param0.connection
                    .send(
                        new ClientboundPlayerChatPacket(var0, var2),
                        PacketSendListener.exceptionallySend(() -> new ClientboundPlayerChatHeaderPacket(this.message))
                    );
                param0.connection.addPendingMessage(var0);
            }

        }

        @Override
        public void sendHeadersToRemainingPlayers(PlayerList param0) {
            param0.broadcastMessageHeader(this.message, this.playersWithFullMessage);
        }
    }
}
