package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
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

    protected abstract void renderStuckItem(Entity var1, float var2, float var3, float var4, float var5);

    protected void preRenderStuckItem(T param0) {
        Lighting.turnOff();
    }

    protected void postRenderStuckItem() {
        Lighting.turnOn();
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        int var0 = this.numStuck(param0);
        Random var1 = new Random((long)param0.getId());
        if (var0 > 0) {
            this.preRenderStuckItem(param0);

            for(int var2 = 0; var2 < var0; ++var2) {
                RenderSystem.pushMatrix();
                ModelPart var3 = this.getParentModel().getRandomModelPart(var1);
                ModelPart.Cube var4 = var3.getRandomCube(var1);
                var3.translateTo(0.0625F);
                float var5 = var1.nextFloat();
                float var6 = var1.nextFloat();
                float var7 = var1.nextFloat();
                float var8 = Mth.lerp(var5, var4.minX, var4.maxX) / 16.0F;
                float var9 = Mth.lerp(var6, var4.minY, var4.maxY) / 16.0F;
                float var10 = Mth.lerp(var7, var4.minZ, var4.maxZ) / 16.0F;
                RenderSystem.translatef(var8, var9, var10);
                var5 = -1.0F * (var5 * 2.0F - 1.0F);
                var6 = -1.0F * (var6 * 2.0F - 1.0F);
                var7 = -1.0F * (var7 * 2.0F - 1.0F);
                this.renderStuckItem(param0, var5, var6, var7, param3);
                RenderSystem.popMatrix();
            }

            this.postRenderStuckItem();
        }
    }
}
