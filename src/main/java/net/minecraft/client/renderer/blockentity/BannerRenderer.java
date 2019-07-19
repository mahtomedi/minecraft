package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nullable;
import net.minecraft.client.model.BannerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.banner.BannerTextures;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BannerRenderer extends BlockEntityRenderer<BannerBlockEntity> {
    private final BannerModel bannerModel = new BannerModel();

    public void render(BannerBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        float var0 = 0.6666667F;
        boolean var1 = param0.getLevel() == null;
        GlStateManager.pushMatrix();
        ModelPart var2 = this.bannerModel.getPole();
        long var3;
        if (var1) {
            var3 = 0L;
            GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.5F, (float)param3 + 0.5F);
            var2.visible = true;
        } else {
            var3 = param0.getLevel().getGameTime();
            BlockState var5 = param0.getBlockState();
            if (var5.getBlock() instanceof BannerBlock) {
                GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.5F, (float)param3 + 0.5F);
                GlStateManager.rotatef((float)(-var5.getValue(BannerBlock.ROTATION) * 360) / 16.0F, 0.0F, 1.0F, 0.0F);
                var2.visible = true;
            } else {
                GlStateManager.translatef((float)param1 + 0.5F, (float)param2 - 0.16666667F, (float)param3 + 0.5F);
                GlStateManager.rotatef(-var5.getValue(WallBannerBlock.FACING).toYRot(), 0.0F, 1.0F, 0.0F);
                GlStateManager.translatef(0.0F, -0.3125F, -0.4375F);
                var2.visible = false;
            }
        }

        BlockPos var6 = param0.getBlockPos();
        float var7 = (float)((long)(var6.getX() * 7 + var6.getY() * 9 + var6.getZ() * 13) + var3) + param4;
        this.bannerModel.getFlag().xRot = (-0.0125F + 0.01F * Mth.cos(var7 * (float) Math.PI * 0.02F)) * (float) Math.PI;
        GlStateManager.enableRescaleNormal();
        ResourceLocation var8 = this.getTextureLocation(param0);
        if (var8 != null) {
            this.bindTexture(var8);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(0.6666667F, -0.6666667F, -0.6666667F);
            this.bannerModel.render();
            GlStateManager.popMatrix();
        }

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    @Nullable
    private ResourceLocation getTextureLocation(BannerBlockEntity param0) {
        return BannerTextures.BANNER_CACHE.getTextureLocation(param0.getTextureHashName(), param0.getPatterns(), param0.getColors());
    }
}
