package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundCommandSuggestionPacket implements Packet<ServerGamePacketListener> {
    private int id;
    private String command;

    public ServerboundCommandSuggestionPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundCommandSuggestionPacket(int param0, String param1) {
        this.id = param0;
        this.command = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.id = param0.readVarInt();
        this.command = param0.readUtf(32500);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
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
