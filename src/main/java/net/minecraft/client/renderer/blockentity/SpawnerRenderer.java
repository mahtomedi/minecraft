package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
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
        Level var0 = param0.getLevel();
        if (var0 != null) {
            BaseSpawner var1 = param0.getSpawner();
            Entity var2 = var1.getOrCreateDisplayEntity(var0, param0.getBlockPos());
            if (var2 != null) {
                renderEntityInSpawner(param1, param2, param3, param4, var2, this.entityRenderer, var1.getoSpin(), var1.getSpin());
            }

        }
    }

    public static void renderEntityInSpawner(
        float param0, PoseStack param1, MultiBufferSource param2, int param3, Entity param4, EntityRenderDispatcher param5, double param6, double param7
    ) {
        param1.pushPose();
        param1.translate(0.5F, 0.0F, 0.5F);
        float var0 = 0.53125F;
        float var1 = Math.max(param4.getBbWidth(), param4.getBbHeight());
        if ((double)var1 > 1.0) {
            var0 /= var1;
        }

        param1.translate(0.0F, 0.4F, 0.0F);
        param1.mulPose(Axis.YP.rotationDegrees((float)Mth.lerp((double)param0, param6, param7) * 10.0F));
        param1.translate(0.0F, -0.2F, 0.0F);
        param1.mulPose(Axis.XP.rotationDegrees(-30.0F));
        param1.scale(var0, var0, var0);
        param5.render(param4, 0.0, 0.0, 0.0, 0.0F, param0, param1, param2, param3);
        param1.popPose();
    }
}
