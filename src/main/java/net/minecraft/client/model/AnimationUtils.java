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
        ModelPart var0 = param3 ? param0 : param1;
        ModelPart var1 = param3 ? param1 : param0;
        var0.yRot = (param3 ? -0.3F : 0.3F) + param2.yRot;
        var1.yRot = (param3 ? 0.6F : -0.6F) + param2.yRot;
        var0.xRot = (float) (-Math.PI / 2) + param2.xRot + 0.1F;
        var1.xRot = -1.5F + param2.xRot;
    }

    public static void animateCrossbowCharge(ModelPart param0, ModelPart param1, LivingEntity param2, boolean param3) {
        ModelPart var0 = param3 ? param0 : param1;
        ModelPart var1 = param3 ? param1 : param0;
        var0.yRot = param3 ? -0.8F : 0.8F;
        var0.xRot = -0.97079635F;
        var1.xRot = var0.xRot;
        float var2 = (float)CrossbowItem.getChargeDuration(param2.getUseItem());
        float var3 = Mth.clamp((float)param2.getTicksUsingItem(), 0.0F, var2);
        float var4 = var3 / var2;
        var1.yRot = Mth.lerp(var4, 0.4F, 0.85F) * (float)(param3 ? 1 : -1);
        var1.xRot = Mth.lerp(var4, var1.xRot, (float) (-Math.PI / 2));
    }
}
