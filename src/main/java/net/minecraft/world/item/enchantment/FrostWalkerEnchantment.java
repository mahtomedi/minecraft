package net.minecraft.world.item.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class FrostWalkerEnchantment extends Enchantment {
    public FrostWalkerEnchantment(Enchantment.Rarity param0, EquipmentSlot... param1) {
        super(param0, EnchantmentCategory.ARMOR_FEET, param1);
    }

    @Override
    public int getMinCost(int param0) {
        return param0 * 10;
    }

    @Override
    public int getMaxCost(int param0) {
        return this.getMinCost(param0) + 15;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    public static void onEntityMoved(LivingEntity param0, Level param1, BlockPos param2, int param3) {
        if (param0.onGround()) {
            BlockState var0 = Blocks.FROSTED_ICE.defaultBlockState();
            int var1 = Math.min(16, 2 + param3);
            BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

            for(BlockPos var3 : BlockPos.betweenClosed(param2.offset(-var1, -1, -var1), param2.offset(var1, -1, var1))) {
                if (var3.closerToCenterThan(param0.position(), (double)var1)) {
                    var2.set(var3.getX(), var3.getY() + 1, var3.getZ());
                    BlockState var4 = param1.getBlockState(var2);
                    if (var4.isAir()) {
                        BlockState var5 = param1.getBlockState(var3);
                        if (var5 == FrostedIceBlock.meltsInto() && var0.canSurvive(param1, var3) && param1.isUnobstructed(var0, var3, CollisionContext.empty())
                            )
                         {
                            param1.setBlockAndUpdate(var3, var0);
                            param1.scheduleTick(var3, Blocks.FROSTED_ICE, Mth.nextInt(param0.getRandom(), 60, 120));
                        }
                    }
                }
            }

        }
    }

    @Override
    public boolean checkCompatibility(Enchantment param0) {
        return super.checkCompatibility(param0) && param0 != Enchantments.DEPTH_STRIDER;
    }
}
