package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
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
public class BannerRenderer extends BlockEntityRenderer<BannerBlockEntity> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ModelPart flag = new ModelPart(64, 64, 0, 0);
    private final ModelPart pole;
    private final ModelPart bar;

    public BannerRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
        this.flag.addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F, 0.0F);
        this.pole = new ModelPart(64, 64, 44, 0);
        this.pole.addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F, 0.0F);
        this.bar = new ModelPart(64, 64, 0, 42);
        this.bar.addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F, 0.0F);
    }

    public void render(
        BannerBlockEntity param0, double param1, double param2, double param3, float param4, PoseStack param5, MultiBufferSource param6, int param7, int param8
    ) {
        float var0 = 0.6666667F;
        boolean var1 = param0.getLevel() == null;
        param5.pushPose();
        long var2;
        if (var1) {
            var2 = 0L;
            param5.translate(0.5, 0.5, param3 + 0.5);
            this.pole.visible = !param0.onlyRenderPattern();
        } else {
            var2 = param0.getLevel().getGameTime();
            BlockState var4 = param0.getBlockState();
            if (var4.getBlock() instanceof BannerBlock) {
                param5.translate(0.5, 0.5, 0.5);
                float var5 = (float)(-var4.getValue(BannerBlock.ROTATION) * 360) / 16.0F;
                param5.mulPose(Vector3f.YP.rotationDegrees(var5));
                this.pole.visible = true;
            } else {
                param5.translate(0.5, -0.16666667F, 0.5);
                float var6 = -var4.getValue(WallBannerBlock.FACING).toYRot();
                param5.mulPose(Vector3f.YP.rotationDegrees(var6));
                param5.translate(0.0, -0.3125, -0.4375);
                this.pole.visible = false;
            }
        }

        TextureAtlasSprite var7 = this.getSprite(ModelBakery.BANNER_BASE);
        param5.pushPose();
        param5.scale(0.6666667F, -0.6666667F, -0.6666667F);
        float var8 = 0.0625F;
        VertexConsumer var9 = param6.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        this.pole.render(param5, var9, 0.0625F, param7, param8, var7);
        this.bar.render(param5, var9, 0.0625F, param7, param8, var7);
        if (param0.onlyRenderPattern()) {
            this.flag.xRot = 0.0F;
        } else {
            BlockPos var10 = param0.getBlockPos();
            float var11 = (float)((long)(var10.getX() * 7 + var10.getY() * 9 + var10.getZ() * 13) + var2) + param4;
            this.flag.xRot = (-0.0125F + 0.01F * Mth.cos(var11 * (float) Math.PI * 0.02F)) * (float) Math.PI;
        }

        this.flag.y = -32.0F;
        this.flag.render(param5, var9, 0.0625F, param7, param8, var7);
        List<BannerPattern> var12 = param0.getPatterns();
        List<DyeColor> var13 = param0.getColors();
        VertexConsumer var14 = param6.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        if (var12 == null) {
            LOGGER.error("patterns are null");
        } else if (var13 == null) {
            LOGGER.error("colors are null");
        } else {
            for(int var15 = 0; var15 < 17 && var15 < var12.size() && var15 < var13.size(); ++var15) {
                BannerPattern var16 = var12.get(var15);
                DyeColor var17 = var13.get(var15);
                float[] var18 = var17.getTextureDiffuseColors();
                this.flag.render(param5, var14, 0.0625F, param7, param8, this.getSprite(var16.location()), var18[0], var18[1], var18[2]);
            }
        }

        param5.popPose();
        param5.popPose();
    }
}
