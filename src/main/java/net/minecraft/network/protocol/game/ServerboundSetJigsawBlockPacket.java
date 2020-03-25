package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundSetJigsawBlockPacket implements Packet<ServerGamePacketListener> {
    private BlockPos pos;
    private ResourceLocation name;
    private ResourceLocation target;
    private ResourceLocation pool;
    private String finalState;
    private JigsawBlockEntity.JointType joint;

    public ServerboundSetJigsawBlockPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundSetJigsawBlockPacket(
        BlockPos param0, ResourceLocation param1, ResourceLocation param2, ResourceLocation param3, String param4, JigsawBlockEntity.JointType param5
    ) {
        this.pos = param0;
        this.name = param1;
        this.target = param2;
        this.pool = param3;
        this.finalState = param4;
        this.joint = param5;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.pos = param0.readBlockPos();
        this.name = param0.readResourceLocation();
        this.target = param0.readResourceLocation();
        this.pool = param0.readResourceLocation();
        this.finalState = param0.readUtf(32767);
        this.joint = JigsawBlockEntity.JointType.byName(param0.readUtf(32767)).orElse(JigsawBlockEntity.JointType.ALIGNED);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeBlockPos(this.pos);
        param0.writeResourceLocation(this.name);
        param0.writeResourceLocation(this.target);
        param0.writeResourceLocation(this.pool);
        param0.writeUtf(this.finalState);
        param0.writeUtf(this.joint.getSerializedName());
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSetJigsawBlock(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public ResourceLocation getTarget() {
        return this.target;
    }

    public ResourceLocation getPool() {
        return this.pool;
    }

    public String getFinalState() {
        return this.finalState;
    }

    public JigsawBlockEntity.JointType getJoint() {
        return this.joint;
    }
}
