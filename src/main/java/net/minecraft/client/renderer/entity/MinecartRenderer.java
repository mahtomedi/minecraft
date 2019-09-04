package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MinecartRenderer<T extends AbstractMinecart> extends EntityRenderer<T> {
    private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
    protected final EntityModel<T> model = new MinecartModel<>();

    public MinecartRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.shadowRadius = 0.7F;
    }

    public void render(T param0, double param1, double param2, double param3, float param4, float param5) {
        RenderSystem.pushMatrix();
        this.bindTexture(param0);
        long var0 = (long)param0.getId() * 493286711L;
        var0 = var0 * var0 * 4392167121L + var0 * 98761L;
        float var1 = (((float)(var0 >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float var2 = (((float)(var0 >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float var3 = (((float)(var0 >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        RenderSystem.translatef(var1, var2, var3);
        double var4 = Mth.lerp((double)param5, param0.xOld, param0.x);
        double var5 = Mth.lerp((double)param5, param0.yOld, param0.y);
        double var6 = Mth.lerp((double)param5, param0.zOld, param0.z);
        double var7 = 0.3F;
        Vec3 var8 = param0.getPos(var4, var5, var6);
        float var9 = Mth.lerp(param5, param0.xRotO, param0.xRot);
        if (var8 != null) {
            Vec3 var10 = param0.getPosOffs(var4, var5, var6, 0.3F);
            Vec3 var11 = param0.getPosOffs(var4, var5, var6, -0.3F);
            if (var10 == null) {
                var10 = var8;
            }

            if (var11 == null) {
                var11 = var8;
            }

            param1 += var8.x - var4;
            param2 += (var10.y + var11.y) / 2.0 - var5;
            param3 += var8.z - var6;
            Vec3 var12 = var11.add(-var10.x, -var10.y, -var10.z);
            if (var12.length() != 0.0) {
                var12 = var12.normalize();
                param4 = (float)(Math.atan2(var12.z, var12.x) * 180.0 / Math.PI);
                var9 = (float)(Math.atan(var12.y) * 73.0);
            }
        }

        RenderSystem.translatef((float)param1, (float)param2 + 0.375F, (float)param3);
        RenderSystem.rotatef(180.0F - param4, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef(-var9, 0.0F, 0.0F, 1.0F);
        float var13 = (float)param0.getHurtTime() - param5;
        float var14 = param0.getDamage() - param5;
        if (var14 < 0.0F) {
            var14 = 0.0F;
        }

        if (var13 > 0.0F) {
            RenderSystem.rotatef(Mth.sin(var13) * var13 * var14 / 10.0F * (float)param0.getHurtDir(), 1.0F, 0.0F, 0.0F);
        }

        int var15 = param0.getDisplayOffset();
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
        }

        BlockState var16 = param0.getDisplayBlockState();
        if (var16.getRenderShape() != RenderShape.INVISIBLE) {
            RenderSystem.pushMatrix();
            this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
            float var17 = 0.75F;
            RenderSystem.scalef(0.75F, 0.75F, 0.75F);
            RenderSystem.translatef(-0.5F, (float)(var15 - 8) / 16.0F, 0.5F);
            this.renderMinecartContents(param0, param5, var16);
            RenderSystem.popMatrix();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.bindTexture(param0);
        }

        RenderSystem.scalef(-1.0F, -1.0F, 1.0F);
        this.model.render(param0, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        RenderSystem.popMatrix();
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }

        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected ResourceLocation getTextureLocation(T param0) {
        return MINECART_LOCATION;
    }

    protected void renderMinecartContents(T param0, float param1, BlockState param2) {
        RenderSystem.pushMatrix();
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(param2, param0.getBrightness());
        RenderSystem.popMatrix();
    }
}
