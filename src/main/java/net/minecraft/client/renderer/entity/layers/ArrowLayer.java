package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import java.util.Random;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.Cube;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArrowLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final EntityRenderDispatcher dispatcher;

    public ArrowLayer(LivingEntityRenderer<T, M> param0) {
        super(param0);
        this.dispatcher = param0.getDispatcher();
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        int var0 = param0.getArrowCount();
        if (var0 > 0) {
            Entity var1 = new Arrow(param0.level, param0.x, param0.y, param0.z);
            Random var2 = new Random((long)param0.getId());
            Lighting.turnOff();

            for(int var3 = 0; var3 < var0; ++var3) {
                GlStateManager.pushMatrix();
                ModelPart var4 = this.getParentModel().getRandomModelPart(var2);
                Cube var5 = var4.cubes.get(var2.nextInt(var4.cubes.size()));
                var4.translateTo(0.0625F);
                float var6 = var2.nextFloat();
                float var7 = var2.nextFloat();
                float var8 = var2.nextFloat();
                float var9 = Mth.lerp(var6, var5.minX, var5.maxX) / 16.0F;
                float var10 = Mth.lerp(var7, var5.minY, var5.maxY) / 16.0F;
                float var11 = Mth.lerp(var8, var5.minZ, var5.maxZ) / 16.0F;
                GlStateManager.translatef(var9, var10, var11);
                var6 = var6 * 2.0F - 1.0F;
                var7 = var7 * 2.0F - 1.0F;
                var8 = var8 * 2.0F - 1.0F;
                var6 *= -1.0F;
                var7 *= -1.0F;
                var8 *= -1.0F;
                float var12 = Mth.sqrt(var6 * var6 + var8 * var8);
                var1.yRot = (float)(Math.atan2((double)var6, (double)var8) * 180.0F / (float)Math.PI);
                var1.xRot = (float)(Math.atan2((double)var7, (double)var12) * 180.0F / (float)Math.PI);
                var1.yRotO = var1.yRot;
                var1.xRotO = var1.xRot;
                double var13 = 0.0;
                double var14 = 0.0;
                double var15 = 0.0;
                this.dispatcher.render(var1, 0.0, 0.0, 0.0, 0.0F, param3, false);
                GlStateManager.popMatrix();
            }

            Lighting.turnOn();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
