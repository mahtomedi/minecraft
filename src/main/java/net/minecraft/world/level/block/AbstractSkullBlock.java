package net.minecraft.world.level.block;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractSkullBlock extends BaseEntityBlock {
    private final SkullBlock.Type type;

    public AbstractSkullBlock(SkullBlock.Type param0, Block.Properties param1) {
        super(param1);
        this.type = param0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean hasCustomBreakingProgress(BlockState param0) {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new SkullBlockEntity();
    }

    @OnlyIn(Dist.CLIENT)
    public SkullBlock.Type getType() {
        return this.type;
    }
}
