package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WritableBookItem extends Item {
    public WritableBookItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        BlockState var2 = var0.getBlockState(var1);
        if (var2.getBlock() == Blocks.LECTERN) {
            return LecternBlock.tryPlaceBook(var0, var1, var2, param0.getItemInHand()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        param1.openItemGui(var0, param2);
        param1.awardStat(Stats.ITEM_USED.get(this));
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, var0);
    }

    public static boolean makeSureTagIsValid(@Nullable CompoundTag param0) {
        if (param0 == null) {
            return false;
        } else if (!param0.contains("pages", 9)) {
            return false;
        } else {
            ListTag var0 = param0.getList("pages", 8);

            for(int var1 = 0; var1 < var0.size(); ++var1) {
                String var2 = var0.getString(var1);
                if (var2.length() > 32767) {
                    return false;
                }
            }

            return true;
        }
    }
}
