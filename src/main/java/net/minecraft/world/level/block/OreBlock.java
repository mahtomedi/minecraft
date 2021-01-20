package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.IntRange;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class OreBlock extends Block {
    private final IntRange xpRange;

    public OreBlock(BlockBehaviour.Properties param0) {
        this(param0, IntRange.of(0, 0));
    }

    public OreBlock(BlockBehaviour.Properties param0, IntRange param1) {
        super(param0);
        this.xpRange = param1;
    }

    @Override
    public void spawnAfterBreak(BlockState param0, ServerLevel param1, BlockPos param2, ItemStack param3) {
        super.spawnAfterBreak(param0, param1, param2, param3);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, param3) == 0) {
            int var0 = this.xpRange.randomValue(param1.random);
            if (var0 > 0) {
                this.popExperience(param1, param2, var0);
            }
        }

    }
}
