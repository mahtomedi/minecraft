package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;

public class ServerboundSetJigsawBlockPacket implements Packet<ServerGamePacketListener> {
    private final BlockPos pos;
    private final ResourceLocation name;
    private final ResourceLocation target;
    private final ResourceLocation pool;
    private final String finalState;
    private final JigsawBlockEntity.JointType joint;

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

    public ServerboundSetJigsawBlockPacket(FriendlyByteBuf param0) {
        this.pos = param0.readBlockPos();
        this.name = param0.readResourceLocation();
        this.target = param0.readResourceLocation();
        this.pool = param0.readResourceLocation();
        this.finalState = param0.readUtf();
        this.joint = JigsawBlockEntity.JointType.byName(param0.readUtf()).orElse(JigsawBlockEntity.JointType.ALIGNED);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
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
