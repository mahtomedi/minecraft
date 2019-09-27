package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
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
        BannerBlockEntity param0, double param1, double param2, double param3, float param4, PoseStack param5, MultiBufferSource param6, int param7
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
                param5.mulPose(Vector3f.YP.rotation((float)(-var4.getValue(BannerBlock.ROTATION) * 360) / 16.0F, true));
                this.pole.visible = true;
            } else {
                param5.translate(0.5, -0.16666667F, 0.5);
                param5.mulPose(Vector3f.YP.rotation(-var4.getValue(WallBannerBlock.FACING).toYRot(), true));
                param5.translate(0.0, -0.3125, -0.4375);
                this.pole.visible = false;
            }
        }

        TextureAtlasSprite var5 = this.getSprite(ModelBakery.BANNER_BASE);
        param5.pushPose();
        param5.scale(0.6666667F, -0.6666667F, -0.6666667F);
        float var6 = 0.0625F;
        VertexConsumer var7 = param6.getBuffer(RenderType.SOLID);
        this.pole.render(param5, var7, 0.0625F, param7, var5);
        this.bar.render(param5, var7, 0.0625F, param7, var5);
        if (param0.onlyRenderPattern()) {
            this.flag.xRot = 0.0F;
        } else {
            BlockPos var8 = param0.getBlockPos();
            float var9 = (float)((long)(var8.getX() * 7 + var8.getY() * 9 + var8.getZ() * 13) + var2) + param4;
            this.flag.xRot = (-0.0125F + 0.01F * Mth.cos(var9 * (float) Math.PI * 0.02F)) * (float) Math.PI;
        }

        this.flag.y = -32.0F;
        this.flag.render(param5, var7, 0.0625F, param7, var5);
        List<BannerPattern> var10 = param0.getPatterns();
        List<DyeColor> var11 = param0.getColors();
        VertexConsumer var12 = param6.getBuffer(RenderType.TRANSLUCENT_NO_CRUMBLING);
        if (var10 == null) {
            LOGGER.error("patterns are null");
        } else if (var11 == null) {
            LOGGER.error("colors are null");
        } else {
            for(int var13 = 0; var13 < 17 && var13 < var10.size() && var13 < var11.size(); ++var13) {
                BannerPattern var14 = var10.get(var13);
                DyeColor var15 = var11.get(var13);
                float[] var16 = var15.getTextureDiffuseColors();
                this.flag.render(param5, var12, 0.0625F, param7, this.getSprite(var14.location()), var16[0], var16[1], var16[2]);
            }
        }

        param5.popPose();
        param5.popPose();
    }
}
