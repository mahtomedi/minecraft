package net.minecraft.client.animation;

import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class KeyframeAnimations {
    public static void animate(HierarchicalModel<?> param0, AnimationDefinition param1, long param2, float param3, Vector3f param4) {
        float var0 = getElapsedSeconds(param1, param2);

        for(Entry<String, List<AnimationChannel>> var1 : param1.boneAnimations().entrySet()) {
            Optional<ModelPart> var2 = param0.getAnyDescendantWithName(var1.getKey());
            List<AnimationChannel> var3 = var1.getValue();
            var2.ifPresent(param4x -> var3.forEach(param4xx -> {
                    Keyframe[] var0x = param4xx.keyframes();
                    int var1x = Math.max(0, Mth.binarySearch(0, var0x.length, param2x -> var0 <= var0x[param2x].timestamp()) - 1);
                    int var2x = Math.min(var0x.length - 1, var1x + 1);
                    Keyframe var3x = var0x[var1x];
                    Keyframe var4x = var0x[var2x];
                    float var5x = var0 - var3x.timestamp();
                    float var6;
                    if (var2x != var1x) {
                        var6 = Mth.clamp(var5x / (var4x.timestamp() - var3x.timestamp()), 0.0F, 1.0F);
                    } else {
                        var6 = 0.0F;
                    }

                    var4x.interpolation().apply(param4, var6, var0x, var1x, var2x, param3);
                    param4xx.target().apply(param4x, param4);
                }));
        }

    }

    private static float getElapsedSeconds(AnimationDefinition param0, long param1) {
        float var0 = (float)param1 / 1000.0F;
        return param0.looping() ? var0 % param0.lengthInSeconds() : var0;
    }

    public static Vector3f posVec(float param0, float param1, float param2) {
        return new Vector3f(param0, -param1, param2);
    }

    public static Vector3f degreeVec(float param0, float param1, float param2) {
        return new Vector3f(param0 * (float) (Math.PI / 180.0), param1 * (float) (Math.PI / 180.0), param2 * (float) (Math.PI / 180.0));
    }

    public static Vector3f scaleVec(double param0, double param1, double param2) {
        return new Vector3f((float)(param0 - 1.0), (float)(param1 - 1.0), (float)(param2 - 1.0));
    }
}
