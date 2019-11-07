package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class StuckInBodyLayer<T extends LivingEntity, M extends PlayerModel<T>> extends RenderLayer<T, M> {
    public StuckInBodyLayer(LivingEntityRenderer<T, M> param0) {
        super(param0);
    }

    protected abstract int numStuck(T var1);

    protected abstract void renderStuckItem(PoseStack var1, MultiBufferSource var2, int var3, Entity var4, float var5, float var6, float var7, float var8);

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        int var0 = this.numStuck(param3);
        Random var1 = new Random((long)param3.getId());
        if (var0 > 0) {
            for(int var2 = 0; var2 < var0; ++var2) {
                param0.pushPose();
                ModelPart var3 = this.getParentModel().getRandomModelPart(var1);
                ModelPart.Cube var4 = var3.getRandomCube(var1);
                var3.translateAndRotate(param0);
                float var5 = var1.nextFloat();
                float var6 = var1.nextFloat();
                float var7 = var1.nextFloat();
                float var8 = Mth.lerp(var5, var4.minX, var4.maxX) / 16.0F;
                float var9 = Mth.lerp(var6, var4.minY, var4.maxY) / 16.0F;
                float var10 = Mth.lerp(var7, var4.minZ, var4.maxZ) / 16.0F;
                param0.translate((double)var8, (double)var9, (double)var10);
                var5 = -1.0F * (var5 * 2.0F - 1.0F);
                var6 = -1.0F * (var6 * 2.0F - 1.0F);
                var7 = -1.0F * (var7 * 2.0F - 1.0F);
                this.renderStuckItem(param0, param1, param2, param3, var5, var6, var7, param6);
                param0.popPose();
            }

        }
    }
}
