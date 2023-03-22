package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

public interface SignApplicator {
    boolean tryApplyToSign(Level var1, SignBlockEntity var2, boolean var3, Player var4);

    default boolean canApplyToSign(SignText param0, Player param1) {
        return param0.hasMessage(param1);
    }
}
