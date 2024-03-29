package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface Fallable {
    default void onLand(Level param0, BlockPos param1, BlockState param2, BlockState param3, FallingBlockEntity param4) {
    }

    default void onBrokenAfterFall(Level param0, BlockPos param1, FallingBlockEntity param2) {
    }

    default DamageSource getFallDamageSource(Entity param0) {
        return param0.damageSources().fallingBlock(param0);
    }
}
