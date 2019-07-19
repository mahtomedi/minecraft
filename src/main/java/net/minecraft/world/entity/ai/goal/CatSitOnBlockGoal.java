package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
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
        return this.cat.isTame() && !this.cat.isSitting() && super.canUse();
    }

    @Override
    public void start() {
        super.start();
        this.cat.getSitGoal().wantToSit(false);
    }

    @Override
    public void stop() {
        super.stop();
        this.cat.setSitting(false);
    }

    @Override
    public void tick() {
        super.tick();
        this.cat.getSitGoal().wantToSit(false);
        if (!this.isReachedTarget()) {
            this.cat.setSitting(false);
        } else if (!this.cat.isSitting()) {
            this.cat.setSitting(true);
        }

    }

    @Override
    protected boolean isValidTarget(LevelReader param0, BlockPos param1) {
        if (!param0.isEmptyBlock(param1.above())) {
            return false;
        } else {
            BlockState var0 = param0.getBlockState(param1);
            Block var1 = var0.getBlock();
            if (var1 == Blocks.CHEST) {
                return ChestBlockEntity.getOpenCount(param0, param1) < 1;
            } else if (var1 == Blocks.FURNACE && var0.getValue(FurnaceBlock.LIT)) {
                return true;
            } else {
                return var1.is(BlockTags.BEDS) && var0.getValue(BedBlock.PART) != BedPart.HEAD;
            }
        }
    }
}
