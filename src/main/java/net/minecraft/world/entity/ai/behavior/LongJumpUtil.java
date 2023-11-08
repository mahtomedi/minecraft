package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

public final class LongJumpUtil {
    public static Optional<Vec3> calculateJumpVectorForAngle(Mob param0, Vec3 param1, float param2, int param3, boolean param4) {
        Vec3 var0 = param0.position();
        Vec3 var1 = new Vec3(param1.x - var0.x, 0.0, param1.z - var0.z).normalize().scale(0.5);
        Vec3 var2 = param1.subtract(var1);
        Vec3 var3 = var2.subtract(var0);
        float var4 = (float)param3 * (float) Math.PI / 180.0F;
        double var5 = Math.atan2(var3.z, var3.x);
        double var6 = var3.subtract(0.0, var3.y, 0.0).lengthSqr();
        double var7 = Math.sqrt(var6);
        double var8 = var3.y;
        double var9 = 0.08;
        double var10 = Math.sin((double)(2.0F * var4));
        double var11 = Math.pow(Math.cos((double)var4), 2.0);
        double var12 = Math.sin((double)var4);
        double var13 = Math.cos((double)var4);
        double var14 = Math.sin(var5);
        double var15 = Math.cos(var5);
        double var16 = var6 * 0.08 / (var7 * var10 - 2.0 * var8 * var11);
        if (var16 < 0.0) {
            return Optional.empty();
        } else {
            double var17 = Math.sqrt(var16);
            if (var17 > (double)param2) {
                return Optional.empty();
            } else {
                double var18 = var17 * var13;
                double var19 = var17 * var12;
                if (param4) {
                    int var20 = Mth.ceil(var7 / var18) * 2;
                    double var21 = 0.0;
                    Vec3 var22 = null;
                    EntityDimensions var23 = param0.getDimensions(Pose.LONG_JUMPING);

                    for(int var24 = 0; var24 < var20 - 1; ++var24) {
                        var21 += var7 / (double)var20;
                        double var25 = var12 / var13 * var21 - Math.pow(var21, 2.0) * 0.08 / (2.0 * var16 * Math.pow(var13, 2.0));
                        double var26 = var21 * var15;
                        double var27 = var21 * var14;
                        Vec3 var28 = new Vec3(var0.x + var26, var0.y + var25, var0.z + var27);
                        if (var22 != null && !isClearTransition(param0, var23, var22, var28)) {
                            return Optional.empty();
                        }

                        var22 = var28;
                    }
                }

                return Optional.of(new Vec3(var18 * var15, var19, var18 * var14).scale(0.95F));
            }
        }
    }

    private static boolean isClearTransition(Mob param0, EntityDimensions param1, Vec3 param2, Vec3 param3) {
        Vec3 var0 = param3.subtract(param2);
        double var1 = (double)Math.min(param1.width, param1.height);
        int var2 = Mth.ceil(var0.length() / var1);
        Vec3 var3 = var0.normalize();
        Vec3 var4 = param2;

        for(int var5 = 0; var5 < var2; ++var5) {
            var4 = var5 == var2 - 1 ? param3 : var4.add(var3.scale(var1 * 0.9F));
            if (!param0.level().noCollision(param0, param1.makeBoundingBox(var4))) {
                return false;
            }
        }

        return true;
    }
}
