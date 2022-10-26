package net.minecraft.client.animation;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public record AnimationChannel(AnimationChannel.Target target, Keyframe... keyframes) {
    @OnlyIn(Dist.CLIENT)
    public interface Interpolation {
        Vector3f apply(Vector3f var1, float var2, Keyframe[] var3, int var4, int var5, float var6);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Interpolations {
        public static final AnimationChannel.Interpolation LINEAR = (param0, param1, param2, param3, param4, param5) -> {
            Vector3f var0 = param2[param3].target();
            Vector3f var1 = param2[param4].target();
            return var0.lerp(var1, param1, param0).mul(param5);
        };
        public static final AnimationChannel.Interpolation CATMULLROM = (param0, param1, param2, param3, param4, param5) -> {
            Vector3f var0 = param2[Math.max(0, param3 - 1)].target();
            Vector3f var1 = param2[param3].target();
            Vector3f var2 = param2[param4].target();
            Vector3f var3 = param2[Math.min(param2.length - 1, param4 + 1)].target();
            param0.set(
                Mth.catmullrom(param1, var0.x(), var1.x(), var2.x(), var3.x()) * param5,
                Mth.catmullrom(param1, var0.y(), var1.y(), var2.y(), var3.y()) * param5,
                Mth.catmullrom(param1, var0.z(), var1.z(), var2.z(), var3.z()) * param5
            );
            return param0;
        };
    }

    @OnlyIn(Dist.CLIENT)
    public interface Target {
        void apply(ModelPart var1, Vector3f var2);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Targets {
        public static final AnimationChannel.Target POSITION = ModelPart::offsetPos;
        public static final AnimationChannel.Target ROTATION = ModelPart::offsetRotation;
        public static final AnimationChannel.Target SCALE = ModelPart::offsetScale;
    }
}
