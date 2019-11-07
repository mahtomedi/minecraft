package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import net.minecraft.client.Minecraft;
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

@OnlyIn(Dist.CLIENT)
public class BannerRenderer extends BlockEntityRenderer<BannerBlockEntity> {
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

    public void render(BannerBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        if (param0.getPatterns() != null) {
            float var0 = 0.6666667F;
            boolean var1 = param0.getLevel() == null;
            param2.pushPose();
            long var2;
            if (var1) {
                var2 = 0L;
                param2.translate(0.5, 0.5, 0.5);
                this.pole.visible = !param0.onlyRenderPattern();
            } else {
                var2 = param0.getLevel().getGameTime();
                BlockState var4 = param0.getBlockState();
                if (var4.getBlock() instanceof BannerBlock) {
                    param2.translate(0.5, 0.5, 0.5);
                    float var5 = (float)(-var4.getValue(BannerBlock.ROTATION) * 360) / 16.0F;
                    param2.mulPose(Vector3f.YP.rotationDegrees(var5));
                    this.pole.visible = true;
                } else {
                    param2.translate(0.5, -0.16666667F, 0.5);
                    float var6 = -var4.getValue(WallBannerBlock.FACING).toYRot();
                    param2.mulPose(Vector3f.YP.rotationDegrees(var6));
                    param2.translate(0.0, -0.3125, -0.4375);
                    this.pole.visible = false;
                }
            }

            TextureAtlasSprite var7 = this.getSprite(ModelBakery.BANNER_BASE);
            param2.pushPose();
            param2.scale(0.6666667F, -0.6666667F, -0.6666667F);
            VertexConsumer var8 = param3.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
            this.pole.render(param2, var8, param4, param5, var7);
            this.bar.render(param2, var8, param4, param5, var7);
            if (param0.onlyRenderPattern()) {
                this.flag.xRot = 0.0F;
            } else {
                BlockPos var9 = param0.getBlockPos();
                float var10 = (float)((long)(var9.getX() * 7 + var9.getY() * 9 + var9.getZ() * 13) + var2) + param1;
                this.flag.xRot = (-0.0125F + 0.01F * Mth.cos(var10 * (float) Math.PI * 0.02F)) * (float) Math.PI;
            }

            this.flag.y = -32.0F;
            this.flag.render(param2, var8, param4, param5, var7);
            renderPatterns(param0, param2, param3, param4, param5, this.flag, true);
            param2.popPose();
            param2.popPose();
        }
    }

    public static void renderPatterns(
        BannerBlockEntity param0, PoseStack param1, MultiBufferSource param2, int param3, int param4, ModelPart param5, boolean param6
    ) {
        List<BannerPattern> var0 = param0.getPatterns();
        List<DyeColor> var1 = param0.getColors();
        TextureAtlas var2 = Minecraft.getInstance().getTextureAtlas();
        VertexConsumer var3 = param2.getBuffer(RenderType.entityNoOutline(TextureAtlas.LOCATION_BLOCKS));

        for(int var4 = 0; var4 < 17 && var4 < var0.size() && var4 < var1.size(); ++var4) {
            BannerPattern var5 = var0.get(var4);
            DyeColor var6 = var1.get(var4);
            float[] var7 = var6.getTextureDiffuseColors();
            param5.render(param1, var3, param3, param4, var2.getSprite(var5.location(param6)), var7[0], var7[1], var7[2]);
        }

    }
}
