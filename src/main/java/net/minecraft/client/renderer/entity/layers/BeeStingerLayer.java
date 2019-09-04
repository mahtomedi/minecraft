package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeStingerLayer<T extends LivingEntity, M extends EntityModel<T>> extends StuckInBodyLayer<T, M> {
    private static final ResourceLocation BEE_STINGER_LOCATION = new ResourceLocation("textures/entity/bee/bee_stinger.png");

    public BeeStingerLayer(LivingEntityRenderer<T, M> param0) {
        super(param0);
    }

    @Override
    protected int numStuck(T param0) {
        return param0.getStingerCount();
    }

    @Override
    protected void preRenderStuckItem(T param0) {
        Lighting.turnOff();
        RenderSystem.pushMatrix();
        this.bindTexture(BEE_STINGER_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableLighting();
        RenderSystem.enableRescaleNormal();
    }

    @Override
    protected void postRenderStuckItem() {
        RenderSystem.disableRescaleNormal();
        RenderSystem.enableLighting();
        RenderSystem.popMatrix();
        Lighting.turnOn();
    }

    @Override
    protected void renderStuckItem(Entity param0, float param1, float param2, float param3, float param4) {
        RenderSystem.pushMatrix();
        float var0 = Mth.sqrt(param1 * param1 + param3 * param3);
        float var1 = (float)(Math.atan2((double)param1, (double)param3) * 180.0F / (float)Math.PI);
        float var2 = (float)(Math.atan2((double)param2, (double)var0) * 180.0F / (float)Math.PI);
        RenderSystem.translatef(0.0F, 0.0F, 0.0F);
        RenderSystem.rotatef(var1 - 90.0F, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef(var2, 0.0F, 0.0F, 1.0F);
        Tesselator var3 = Tesselator.getInstance();
        BufferBuilder var4 = var3.getBuilder();
        float var5 = 0.0F;
        float var6 = 0.125F;
        float var7 = 0.0F;
        float var8 = 0.0625F;
        float var9 = 0.03125F;
        RenderSystem.rotatef(45.0F, 1.0F, 0.0F, 0.0F);
        RenderSystem.scalef(0.03125F, 0.03125F, 0.03125F);
        RenderSystem.translatef(2.5F, 0.0F, 0.0F);

        for(int var10 = 0; var10 < 4; ++var10) {
            RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
            var4.begin(7, DefaultVertexFormat.POSITION_TEX);
            var4.vertex(-4.5, -1.0, 0.0).uv(0.0, 0.0).endVertex();
            var4.vertex(4.5, -1.0, 0.0).uv(0.125, 0.0).endVertex();
            var4.vertex(4.5, 1.0, 0.0).uv(0.125, 0.0625).endVertex();
            var4.vertex(-4.5, 1.0, 0.0).uv(0.0, 0.0625).endVertex();
            var3.end();
        }

        RenderSystem.popMatrix();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
