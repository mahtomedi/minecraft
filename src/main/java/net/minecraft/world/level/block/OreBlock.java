package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class OreBlock extends Block {
    public OreBlock(Block.Properties param0) {
        super(param0);
    }

    protected int xpOnDrop(Random param0) {
        if (this == Blocks.COAL_ORE) {
            return Mth.nextInt(param0, 0, 2);
        } else if (this == Blocks.DIAMOND_ORE) {
            return Mth.nextInt(param0, 3, 7);
        } else if (this == Blocks.EMERALD_ORE) {
            return Mth.nextInt(param0, 3, 7);
        } else if (this == Blocks.LAPIS_ORE) {
            return Mth.nextInt(param0, 2, 5);
        } else {
            return this == Blocks.NETHER_QUARTZ_ORE ? Mth.nextInt(param0, 2, 5) : 0;
        }
    }

    @Override
    public void spawnAfterBreak(BlockState param0, Level param1, BlockPos param2, ItemStack param3) {
        super.spawnAfterBreak(param0, param1, param2, param3);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, param3) == 0) {
            int var0 = this.xpOnDrop(param1.random);
            if (var0 > 0) {
                this.popExperience(param1, param2, var0);
            }
        }

    }
}
