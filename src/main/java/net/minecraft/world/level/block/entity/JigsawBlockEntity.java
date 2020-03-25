package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class JigsawBlockEntity extends BlockEntity {
    private ResourceLocation name = new ResourceLocation("empty");
    private ResourceLocation target = new ResourceLocation("empty");
    private ResourceLocation pool = new ResourceLocation("empty");
    private JigsawBlockEntity.JointType joint = JigsawBlockEntity.JointType.ROLLABLE;
    private String finalState = "minecraft:air";

    public JigsawBlockEntity(BlockEntityType<?> param0) {
        super(param0);
    }

    public JigsawBlockEntity() {
        this(BlockEntityType.JIGSAW);
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getName() {
        return this.name;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTarget() {
        return this.target;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getPool() {
        return this.pool;
    }

    @OnlyIn(Dist.CLIENT)
    public String getFinalState() {
        return this.finalState;
    }

    @OnlyIn(Dist.CLIENT)
    public JigsawBlockEntity.JointType getJoint() {
        return this.joint;
    }

    public void setName(ResourceLocation param0) {
        this.name = param0;
    }

    public void setTarget(ResourceLocation param0) {
        this.target = param0;
    }

    public void setPool(ResourceLocation param0) {
        this.pool = param0;
    }

    public void setFinalState(String param0) {
        this.finalState = param0;
    }

    public void setJoint(JigsawBlockEntity.JointType param0) {
        this.joint = param0;
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        param0.putString("name", this.name.toString());
        param0.putString("target", this.target.toString());
        param0.putString("pool", this.pool.toString());
        param0.putString("final_state", this.finalState);
        param0.putString("joint", this.joint.getSerializedName());
        return param0;
    }

    @Override
    public void load(BlockState param0, CompoundTag param1) {
        super.load(param0, param1);
        this.name = new ResourceLocation(param1.getString("name"));
        this.target = new ResourceLocation(param1.getString("target"));
        this.pool = new ResourceLocation(param1.getString("pool"));
        this.finalState = param1.getString("final_state");
        this.joint = JigsawBlockEntity.JointType.byName(param1.getString("joint"))
            .orElseGet(
                () -> JigsawBlock.getFrontFacing(param0).getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE
            );
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 12, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    public static enum JointType implements StringRepresentable {
        ROLLABLE("rollable"),
        ALIGNED("aligned");

        private final String name;

        private JointType(String param0) {
            this.name = param0;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static Optional<JigsawBlockEntity.JointType> byName(String param0) {
            return Arrays.stream(values()).filter(param1 -> param1.getSerializedName().equals(param0)).findFirst();
        }
    }
}
