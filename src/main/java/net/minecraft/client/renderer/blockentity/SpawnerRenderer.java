package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpawnerRenderer implements BlockEntityRenderer<SpawnerBlockEntity> {
    public SpawnerRenderer(BlockEntityRendererProvider.Context param0) {
    }

    public void render(SpawnerBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        param2.pushPose();
        param2.translate(0.5, 0.0, 0.5);
        BaseSpawner var0 = param0.getSpawner();
        Entity var1 = var0.getOrCreateDisplayEntity(param0.getLevel());
        if (var1 != null) {
            float var2 = 0.53125F;
            float var3 = Math.max(var1.getBbWidth(), var1.getBbHeight());
            if ((double)var3 > 1.0) {
                var2 /= var3;
            }

            param2.translate(0.0, 0.4F, 0.0);
            param2.mulPose(Vector3f.YP.rotationDegrees((float)Mth.lerp((double)param1, var0.getoSpin(), var0.getSpin()) * 10.0F));
            param2.translate(0.0, -0.2F, 0.0);
            param2.mulPose(Vector3f.XP.rotationDegrees(-30.0F));
            param2.scale(var2, var2, var2);
            Minecraft.getInstance().getEntityRenderDispatcher().render(var1, 0.0, 0.0, 0.0, 0.0F, param1, param2, param3, param4);
        }

        param2.popPose();
    }
}
