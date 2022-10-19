package net.minecraft.network.chat;

import net.minecraft.server.level.ServerPlayer;

public interface OutgoingChatMessage {
    Component content();

    void sendToPlayer(ServerPlayer var1, boolean var2, ChatType.Bound var3);

    static OutgoingChatMessage create(PlayerChatMessage param0) {
        return (OutgoingChatMessage)(param0.isSystem() ? new OutgoingChatMessage.Disguised(param0.decoratedContent()) : new OutgoingChatMessage.Player(param0));
    }

    public static record Disguised(Component content) implements OutgoingChatMessage {
        @Override
        public void sendToPlayer(ServerPlayer param0, boolean param1, ChatType.Bound param2) {
            param0.connection.sendDisguisedChatMessage(this.content, param2);
        }
    }

    public static record Player(PlayerChatMessage message) implements OutgoingChatMessage {
        @Override
        public Component content() {
            return this.message.decoratedContent();
        }

        @Override
        public void sendToPlayer(ServerPlayer param0, boolean param1, ChatType.Bound param2) {
            PlayerChatMessage var0 = this.message.filter(param1);
            if (!var0.isFullyFiltered()) {
                param0.connection.sendPlayerChatMessage(var0, param2);
            }

        }
    }
}
