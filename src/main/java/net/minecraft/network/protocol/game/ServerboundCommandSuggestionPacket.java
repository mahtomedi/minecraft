package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundCommandSuggestionPacket implements Packet<ServerGamePacketListener> {
    private final int id;
    private final String command;

    public ServerboundCommandSuggestionPacket(int param0, String param1) {
        this.id = param0;
        this.command = param1;
    }

    public ServerboundCommandSuggestionPacket(FriendlyByteBuf param0) {
        this.id = param0.readVarInt();
        this.command = param0.readUtf(32500);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.id);
        param0.writeUtf(this.command, 32500);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleCustomCommandSuggestions(this);
    }

    public int getId() {
        return this.id;
    }

    public String getCommand() {
        return this.command;
    }
}
