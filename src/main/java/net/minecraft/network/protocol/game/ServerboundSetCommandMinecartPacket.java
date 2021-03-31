package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;

public class ServerboundSetCommandMinecartPacket implements Packet<ServerGamePacketListener> {
    private final int entity;
    private final String command;
    private final boolean trackOutput;

    public ServerboundSetCommandMinecartPacket(int param0, String param1, boolean param2) {
        this.entity = param0;
        this.command = param1;
        this.trackOutput = param2;
    }

    public ServerboundSetCommandMinecartPacket(FriendlyByteBuf param0) {
        this.entity = param0.readVarInt();
        this.command = param0.readUtf();
        this.trackOutput = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.entity);
        param0.writeUtf(this.command);
        param0.writeBoolean(this.trackOutput);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSetCommandMinecart(this);
    }

    @Nullable
    public BaseCommandBlock getCommandBlock(Level param0) {
        Entity var0 = param0.getEntity(this.entity);
        return var0 instanceof MinecartCommandBlock ? ((MinecartCommandBlock)var0).getCommandBlock() : null;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean isTrackOutput() {
        return this.trackOutput;
    }
}
