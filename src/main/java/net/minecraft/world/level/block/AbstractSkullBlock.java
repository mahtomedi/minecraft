package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractSkullBlock extends BaseEntityBlock implements Wearable {
    private final SkullBlock.Type type;

    public AbstractSkullBlock(SkullBlock.Type param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.type = param0;
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new SkullBlockEntity();
    }

    @OnlyIn(Dist.CLIENT)
    public SkullBlock.Type getType() {
        return this.type;
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
