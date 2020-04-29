package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class CatSitOnBlockGoal extends MoveToBlockGoal {
    private final Cat cat;

    public CatSitOnBlockGoal(Cat param0, double param1) {
        super(param0, param1, 8);
        this.cat = param0;
    }

    @Override
    public boolean canUse() {
        return this.cat.isTame() && !this.cat.isOrderedToSit() && super.canUse();
    }

    @Override
    public void start() {
        super.start();
        this.cat.setInSittingPose(false);
    }

    @Override
    public void stop() {
        super.stop();
        this.cat.setInSittingPose(false);
    }

    @Override
    public void tick() {
        super.tick();
        this.cat.setInSittingPose(this.isReachedTarget());
    }

    @Override
    protected boolean isValidTarget(LevelReader param0, BlockPos param1) {
        if (!param0.isEmptyBlock(param1.above())) {
            return false;
        } else {
            BlockState var0 = param0.getBlockState(param1);
            if (var0.is(Blocks.CHEST)) {
                return ChestBlockEntity.getOpenCount(param0, param1) < 1;
            } else {
                return var0.is(Blocks.FURNACE) && var0.getValue(FurnaceBlock.LIT)
                    ? true
                    : var0.is(
                        BlockTags.BEDS, param0x -> param0x.<BedPart>getOptionalValue(BedBlock.PART).map(param0xx -> param0xx != BedPart.HEAD).orElse(true)
                    );
            }
        }
    }
}
