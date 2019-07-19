package net.minecraft.world.phys;

import java.util.Objects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PosAndRot {
    private final Vec3 pos;
    private final float xRot;
    private final float yRot;

    public PosAndRot(Vec3 param0, float param1, float param2) {
        this.pos = param0;
        this.xRot = param1;
        this.yRot = param2;
    }

    public Vec3 pos() {
        return this.pos;
    }

    public float xRot() {
        return this.xRot;
    }

    public float yRot() {
        return this.yRot;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            PosAndRot var0 = (PosAndRot)param0;
            return Float.compare(var0.xRot, this.xRot) == 0 && Float.compare(var0.yRot, this.yRot) == 0 && Objects.equals(this.pos, var0.pos);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pos, this.xRot, this.yRot);
    }

    @Override
    public String toString() {
        return "PosAndRot[" + this.pos + " (" + this.xRot + ", " + this.yRot + ")]";
    }
}
