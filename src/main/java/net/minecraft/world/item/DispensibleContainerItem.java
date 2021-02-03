package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface DispensibleContainerItem {
    default void checkExtraContent(@Nullable Player param0, Level param1, ItemStack param2, BlockPos param3) {
    }

    boolean emptyContents(@Nullable Player var1, Level var2, BlockPos var3, @Nullable BlockHitResult var4);
}
