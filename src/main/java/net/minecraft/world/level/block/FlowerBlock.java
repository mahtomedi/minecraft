package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerBlock extends BushBlock {
    protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);
    private final MobEffect suspiciousStewEffect;
    private final int effectDuration;

    public FlowerBlock(MobEffect param0, int param1, BlockBehaviour.Properties param2) {
        super(param2);
        this.suspiciousStewEffect = param0;
        if (param0.isInstantenous()) {
            this.effectDuration = param1;
        } else {
            this.effectDuration = param1 * 20;
        }

    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        Vec3 var0 = param0.getOffset(param1, param2);
        return SHAPE.move(var0.x, var0.y, var0.z);
    }

    @Override
    public BlockBehaviour.OffsetType getOffsetType() {
        return BlockBehaviour.OffsetType.XZ;
    }

    public MobEffect getSuspiciousStewEffect() {
        return this.suspiciousStewEffect;
    }

    public int getEffectDuration() {
        return this.effectDuration;
    }
}
