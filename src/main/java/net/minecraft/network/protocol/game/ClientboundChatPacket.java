package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundChatPacket implements Packet<ClientGamePacketListener> {
    private Component message;
    private ChatType type;
    private UUID sender;

    public ClientboundChatPacket() {
    }

    public ClientboundChatPacket(Component param0, ChatType param1, UUID param2) {
        this.message = param0;
        this.type = param1;
        this.sender = param2;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.message = param0.readComponent();
        this.type = ChatType.getForIndex(param0.readByte());
        this.sender = param0.readUUID();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeComponent(this.message);
        param0.writeByte(this.type.getIndex());
        param0.writeUUID(this.sender);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleChat(this);
    }

    @OnlyIn(Dist.CLIENT)
    public Component getMessage() {
        return this.message;
    }

    public boolean isSystem() {
        return this.type == ChatType.SYSTEM || this.type == ChatType.GAME_INFO;
    }

    public ChatType getType() {
        return this.type;
    }

    @OnlyIn(Dist.CLIENT)
    public UUID getSender() {
        return this.sender;
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}
