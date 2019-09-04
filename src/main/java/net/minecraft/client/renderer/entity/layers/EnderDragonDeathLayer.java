package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Random;
import net.minecraft.client.model.dragon.DragonModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnderDragonDeathLayer extends RenderLayer<EnderDragon, DragonModel> {
    public EnderDragonDeathLayer(RenderLayerParent<EnderDragon, DragonModel> param0) {
        super(param0);
    }

    public void render(EnderDragon param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (param0.dragonDeathTime > 0) {
            Tesselator var0 = Tesselator.getInstance();
            BufferBuilder var1 = var0.getBuilder();
            Lighting.turnOff();
            float var2 = ((float)param0.dragonDeathTime + param3) / 200.0F;
            float var3 = 0.0F;
            if (var2 > 0.8F) {
                var3 = (var2 - 0.8F) / 0.2F;
            }

            Random var4 = new Random(432L);
            RenderSystem.disableTexture();
            RenderSystem.shadeModel(7425);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableCull();
            RenderSystem.depthMask(false);
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0F, -1.0F, -2.0F);

            for(int var5 = 0; (float)var5 < (var2 + var2 * var2) / 2.0F * 60.0F; ++var5) {
                RenderSystem.rotatef(var4.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.rotatef(var4.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                RenderSystem.rotatef(var4.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.rotatef(var4.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                RenderSystem.rotatef(var4.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                RenderSystem.rotatef(var4.nextFloat() * 360.0F + var2 * 90.0F, 0.0F, 0.0F, 1.0F);
                float var6 = var4.nextFloat() * 20.0F + 5.0F + var3 * 10.0F;
                float var7 = var4.nextFloat() * 2.0F + 1.0F + var3 * 2.0F;
                var1.begin(6, DefaultVertexFormat.POSITION_COLOR);
                var1.vertex(0.0, 0.0, 0.0).color(255, 255, 255, (int)(255.0F * (1.0F - var3))).endVertex();
                var1.vertex(-0.866 * (double)var7, (double)var6, (double)(-0.5F * var7)).color(255, 0, 255, 0).endVertex();
                var1.vertex(0.866 * (double)var7, (double)var6, (double)(-0.5F * var7)).color(255, 0, 255, 0).endVertex();
                var1.vertex(0.0, (double)var6, (double)(1.0F * var7)).color(255, 0, 255, 0).endVertex();
                var1.vertex(-0.866 * (double)var7, (double)var6, (double)(-0.5F * var7)).color(255, 0, 255, 0).endVertex();
                var0.end();
            }

            RenderSystem.popMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            RenderSystem.disableBlend();
            RenderSystem.shadeModel(7424);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableTexture();
            RenderSystem.enableAlphaTest();
            Lighting.turnOn();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
