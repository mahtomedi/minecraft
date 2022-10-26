package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpawnerRenderer implements BlockEntityRenderer<SpawnerBlockEntity> {
    private final EntityRenderDispatcher entityRenderer;

    public SpawnerRenderer(BlockEntityRendererProvider.Context param0) {
        this.entityRenderer = param0.getEntityRenderer();
    }

    public void render(SpawnerBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        param2.pushPose();
        param2.translate(0.5F, 0.0F, 0.5F);
        BaseSpawner var0 = param0.getSpawner();
        Entity var1 = var0.getOrCreateDisplayEntity(param0.getLevel(), param0.getLevel().getRandom(), param0.getBlockPos());
        if (var1 != null) {
            float var2 = 0.53125F;
            float var3 = Math.max(var1.getBbWidth(), var1.getBbHeight());
            if ((double)var3 > 1.0) {
                var2 /= var3;
            }

            param2.translate(0.0F, 0.4F, 0.0F);
            param2.mulPose(Axis.YP.rotationDegrees((float)Mth.lerp((double)param1, var0.getoSpin(), var0.getSpin()) * 10.0F));
            param2.translate(0.0F, -0.2F, 0.0F);
            param2.mulPose(Axis.XP.rotationDegrees(-30.0F));
            param2.scale(var2, var2, var2);
            this.entityRenderer.render(var1, 0.0, 0.0, 0.0, 0.0F, param1, param2, param3, param4);
        }

        param2.popPose();
    }
}
