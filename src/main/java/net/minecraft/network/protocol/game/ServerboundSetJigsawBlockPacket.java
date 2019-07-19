package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundSetJigsawBlockPacket implements Packet<ServerGamePacketListener> {
    private BlockPos pos;
    private ResourceLocation attachementType;
    private ResourceLocation targetPool;
    private String finalState;

    public ServerboundSetJigsawBlockPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundSetJigsawBlockPacket(BlockPos param0, ResourceLocation param1, ResourceLocation param2, String param3) {
        this.pos = param0;
        this.attachementType = param1;
        this.targetPool = param2;
        this.finalState = param3;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.pos = param0.readBlockPos();
        this.attachementType = param0.readResourceLocation();
        this.targetPool = param0.readResourceLocation();
        this.finalState = param0.readUtf(32767);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeBlockPos(this.pos);
        param0.writeResourceLocation(this.attachementType);
        param0.writeResourceLocation(this.targetPool);
        param0.writeUtf(this.finalState);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSetJigsawBlock(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public ResourceLocation getTargetPool() {
        return this.targetPool;
    }

    public ResourceLocation getAttachementType() {
        return this.attachementType;
    }

    public String getFinalState() {
        return this.finalState;
    }
}
