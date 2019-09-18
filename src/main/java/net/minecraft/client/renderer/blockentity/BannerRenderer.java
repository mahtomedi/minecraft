package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BannerRenderer extends BatchedBlockEntityRenderer<BannerBlockEntity> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ModelPart flag = new ModelPart(64, 64, 0, 0);
    private final ModelPart pole;
    private final ModelPart bar;

    public BannerRenderer() {
        this.flag.addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F, 0.0F);
        this.pole = new ModelPart(64, 64, 44, 0);
        this.pole.addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F, 0.0F);
        this.bar = new ModelPart(64, 64, 0, 42);
        this.bar.addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F, 0.0F);
    }

    protected void renderToBuffer(
        BannerBlockEntity param0,
        double param1,
        double param2,
        double param3,
        float param4,
        int param5,
        RenderType param6,
        BufferBuilder param7,
        int param8,
        int param9
    ) {
        float var0 = 0.6666667F;
        boolean var1 = param0.getLevel() == null;
        param7.pushPose();
        long var2;
        if (var1) {
            var2 = 0L;
            param7.translate(0.5, 0.5, param3 + 0.5);
            this.pole.visible = !param0.onlyRenderPattern();
        } else {
            var2 = param0.getLevel().getGameTime();
            BlockState var4 = param0.getBlockState();
            if (var4.getBlock() instanceof BannerBlock) {
                param7.translate(0.5, 0.5, 0.5);
                param7.multiplyPose(new Quaternion(Vector3f.YP, (float)(-var4.getValue(BannerBlock.ROTATION) * 360) / 16.0F, true));
                this.pole.visible = true;
            } else {
                param7.translate(0.5, -0.16666667F, 0.5);
                param7.multiplyPose(new Quaternion(Vector3f.YP, -var4.getValue(WallBannerBlock.FACING).toYRot(), true));
                param7.translate(0.0, -0.3125, -0.4375);
                this.pole.visible = false;
            }
        }

        TextureAtlasSprite var5 = this.getSprite(ModelBakery.BANNER_BASE);
        param7.pushPose();
        param7.scale(0.6666667F, -0.6666667F, -0.6666667F);
        float var6 = 0.0625F;
        this.pole.render(param7, 0.0625F, param8, param9, var5);
        this.bar.render(param7, 0.0625F, param8, param9, var5);
        if (param0.onlyRenderPattern()) {
            this.flag.xRot = 0.0F;
        } else {
            BlockPos var7 = param0.getBlockPos();
            float var8 = (float)((long)(var7.getX() * 7 + var7.getY() * 9 + var7.getZ() * 13) + var2) + param4;
            this.flag.xRot = (-0.0125F + 0.01F * Mth.cos(var8 * (float) Math.PI * 0.02F)) * (float) Math.PI;
        }

        this.flag.y = -32.0F;
        this.flag.render(param7, 0.0625F, param8, param9, var5);
        List<BannerPattern> var9 = param0.getPatterns();
        List<DyeColor> var10 = param0.getColors();
        if (var9 == null) {
            LOGGER.error("patterns are null");
        } else if (var10 == null) {
            LOGGER.error("colors are null");
        } else {
            for(int var11 = 0; var11 < 17 && var11 < var9.size() && var11 < var10.size(); ++var11) {
                BannerPattern var12 = var9.get(var11);
                DyeColor var13 = var10.get(var11);
                float[] var14 = var13.getTextureDiffuseColors();
                this.flag.render(param7, 0.0625F, param8, param9, this.getSprite(var12.location()), var14[0], var14[1], var14[2]);
            }
        }

        param7.popPose();
        param7.popPose();
    }
}
