package net.minecraft.commands.arguments.coordinates;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface Coordinates {
    Vec3 getPosition(CommandSourceStack var1);

    Vec2 getRotation(CommandSourceStack var1);

    default BlockPos getBlockPos(CommandSourceStack param0) {
        return new BlockPos(this.getPosition(param0));
    }

    boolean isXRelative();

    boolean isYRelative();

    boolean isZRelative();
}
