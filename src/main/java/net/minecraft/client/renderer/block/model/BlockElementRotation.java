package net.minecraft.client.renderer.block.model;

import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockElementRotation {
    public final Vector3f origin;
    public final Direction.Axis axis;
    public final float angle;
    public final boolean rescale;

    public BlockElementRotation(Vector3f param0, Direction.Axis param1, float param2, boolean param3) {
        this.origin = param0;
        this.axis = param1;
        this.angle = param2;
        this.rescale = param3;
    }
}
