package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DropExperienceBlock extends Block {
    private final IntProvider xpRange;

    public DropExperienceBlock(BlockBehaviour.Properties param0) {
        this(param0, ConstantInt.of(0));
    }

    public DropExperienceBlock(BlockBehaviour.Properties param0, IntProvider param1) {
        super(param0);
        this.xpRange = param1;
    }

    @Override
    public void spawnAfterBreak(BlockState param0, ServerLevel param1, BlockPos param2, ItemStack param3, boolean param4) {
        super.spawnAfterBreak(param0, param1, param2, param3, param4);
        if (param4) {
            this.tryDropExperience(param1, param2, param3, this.xpRange);
        }

    }
}
