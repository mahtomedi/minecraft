package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public class WaterLilyBlockItem extends BlockItem {
    public WaterLilyBlockItem(Block param0, Item.Properties param1) {
        super(param0, param1);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        BlockHitResult var0 = getPlayerPOVHitResult(param0, param1, ClipContext.Fluid.SOURCE_ONLY);
        BlockHitResult var1 = var0.withPosition(var0.getBlockPos().above());
        InteractionResult var2 = super.useOn(new UseOnContext(param1, param2, var1));
        return new InteractionResultHolder<>(var2, param1.getItemInHand(param2));
    }
}
