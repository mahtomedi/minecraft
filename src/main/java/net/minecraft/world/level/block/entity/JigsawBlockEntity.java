package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class JigsawBlockEntity extends BlockEntity {
    private ResourceLocation attachementType = new ResourceLocation("empty");
    private ResourceLocation targetPool = new ResourceLocation("empty");
    private String finalState = "minecraft:air";

    public JigsawBlockEntity(BlockEntityType<?> param0) {
        super(param0);
    }

    public JigsawBlockEntity() {
        this(BlockEntityType.JIGSAW);
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getAttachementType() {
        return this.attachementType;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTargetPool() {
        return this.targetPool;
    }

    @OnlyIn(Dist.CLIENT)
    public String getFinalState() {
        return this.finalState;
    }

    public void setAttachementType(ResourceLocation param0) {
        this.attachementType = param0;
    }

    public void setTargetPool(ResourceLocation param0) {
        this.targetPool = param0;
    }

    public void setFinalState(String param0) {
        this.finalState = param0;
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        param0.putString("attachement_type", this.attachementType.toString());
        param0.putString("target_pool", this.targetPool.toString());
        param0.putString("final_state", this.finalState);
        return param0;
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.attachementType = new ResourceLocation(param0.getString("attachement_type"));
        this.targetPool = new ResourceLocation(param0.getString("target_pool"));
        this.finalState = param0.getString("final_state");
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
}
