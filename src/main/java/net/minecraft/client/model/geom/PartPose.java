package net.minecraft.client.model.geom;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PartPose {
    public static final PartPose ZERO = offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    public final float x;
    public final float y;
    public final float z;
    public final float xRot;
    public final float yRot;
    public final float zRot;

    private PartPose(float param0, float param1, float param2, float param3, float param4, float param5) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.xRot = param3;
        this.yRot = param4;
        this.zRot = param5;
    }

    public static PartPose offset(float param0, float param1, float param2) {
        return offsetAndRotation(param0, param1, param2, 0.0F, 0.0F, 0.0F);
    }

    public static PartPose rotation(float param0, float param1, float param2) {
        return offsetAndRotation(0.0F, 0.0F, 0.0F, param0, param1, param2);
    }

    public static PartPose offsetAndRotation(float param0, float param1, float param2, float param3, float param4, float param5) {
        return new PartPose(param0, param1, param2, param3, param4, param5);
    }
}
