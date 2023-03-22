package com.mojang.blaze3d.vertex;

import com.google.common.primitives.Floats;
import it.unimi.dsi.fastutil.ints.IntArrays;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public interface VertexSorting {
    VertexSorting DISTANCE_TO_ORIGIN = byDistance(0.0F, 0.0F, 0.0F);
    VertexSorting ORTHOGRAPHIC_Z = byDistance(param0 -> -param0.z());

    static VertexSorting byDistance(float param0, float param1, float param2) {
        return byDistance(new Vector3f(param0, param1, param2));
    }

    static VertexSorting byDistance(Vector3f param0) {
        return byDistance(param0::distanceSquared);
    }

    static VertexSorting byDistance(VertexSorting.DistanceFunction param0) {
        return param1 -> {
            float[] var0x = new float[param1.length];
            int[] var1 = new int[param1.length];

            for(int var2 = 0; var2 < param1.length; var1[var2] = var2++) {
                var0x[var2] = param0.apply(param1[var2]);
            }

            IntArrays.mergeSort(var1, (param1x, param2) -> Floats.compare(var0x[param2], var0x[param1x]));
            return var1;
        };
    }

    int[] sort(Vector3f[] var1);

    @OnlyIn(Dist.CLIENT)
    public interface DistanceFunction {
        float apply(Vector3f var1);
    }
}
