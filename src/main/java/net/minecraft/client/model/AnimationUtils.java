package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnimationUtils {
    public static void animateCrossbowHold(ModelPart param0, ModelPart param1, ModelPart param2, boolean param3) {
        param0.yRot = (param3 ? -0.3F : 0.3F) + param2.yRot;
        param1.yRot = (param3 ? 0.6F : -0.6F) + param2.yRot;
        param0.xRot = (float) (-Math.PI / 2) + param2.xRot + 0.1F;
        param1.xRot = -1.5F + param2.xRot;
    }

    public static void animateCrossbowCharge(ModelPart param0, ModelPart param1, LivingEntity param2, boolean param3) {
        param0.yRot = param3 ? -0.8F : 0.8F;
        param0.xRot = -0.97079635F;
        param1.xRot = param0.xRot;
        float var0 = (float)CrossbowItem.getChargeDuration(param2.getUseItem());
        float var1 = Mth.clamp((float)param2.getTicksUsingItem(), 0.0F, var0);
        float var2 = var1 / var0;
        param1.yRot = Mth.lerp(var2, 0.4F, 0.85F) * (float)(param3 ? 1 : -1);
        param1.xRot = Mth.lerp(var2, param1.xRot, (float) (-Math.PI / 2));
    }
}
