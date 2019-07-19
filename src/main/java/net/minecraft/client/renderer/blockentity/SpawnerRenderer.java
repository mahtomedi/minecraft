package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpawnerRenderer extends BlockEntityRenderer<SpawnerBlockEntity> {
    public void render(SpawnerBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float)param1 + 0.5F, (float)param2, (float)param3 + 0.5F);
        render(param0.getSpawner(), param1, param2, param3, param4);
        GlStateManager.popMatrix();
    }

    public static void render(BaseSpawner param0, double param1, double param2, double param3, float param4) {
        Entity var0 = param0.getOrCreateDisplayEntity();
        if (var0 != null) {
            float var1 = 0.53125F;
            float var2 = Math.max(var0.getBbWidth(), var0.getBbHeight());
            if ((double)var2 > 1.0) {
                var1 /= var2;
            }

            GlStateManager.translatef(0.0F, 0.4F, 0.0F);
            GlStateManager.rotatef((float)Mth.lerp((double)param4, param0.getoSpin(), param0.getSpin()) * 10.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translatef(0.0F, -0.2F, 0.0F);
            GlStateManager.rotatef(-30.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.scalef(var1, var1, var1);
            var0.moveTo(param1, param2, param3, 0.0F, 0.0F);
            Minecraft.getInstance().getEntityRenderDispatcher().render(var0, 0.0, 0.0, 0.0, 0.0F, param4, false);
        }

    }
}
