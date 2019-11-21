package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
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
    private final ModelPart flag = makeFlag();
    private final ModelPart pole = new ModelPart(64, 64, 44, 0);
    private final ModelPart bar;

    public BannerRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
        this.pole.addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F, 0.0F);
        this.bar = new ModelPart(64, 64, 0, 42);
        this.bar.addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F, 0.0F);
    }

    public static ModelPart makeFlag() {
        ModelPart var0 = new ModelPart(64, 64, 0, 0);
        var0.addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F, 0.0F);
        return var0;
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

            param2.pushPose();
            param2.scale(0.6666667F, -0.6666667F, -0.6666667F);
            VertexConsumer var7 = ModelBakery.BANNER_BASE.buffer(param3, RenderType::entitySolid);
            this.pole.render(param2, var7, param4, param5);
            this.bar.render(param2, var7, param4, param5);
            if (param0.onlyRenderPattern()) {
                this.flag.xRot = 0.0F;
            } else {
                BlockPos var8 = param0.getBlockPos();
                float var9 = (float)((long)(var8.getX() * 7 + var8.getY() * 9 + var8.getZ() * 13) + var2) + param1;
                this.flag.xRot = (-0.0125F + 0.01F * Mth.cos(var9 * (float) Math.PI * 0.02F)) * (float) Math.PI;
            }

            this.flag.y = -32.0F;
            renderPatterns(param0, param2, param3, param4, param5, this.flag, true);
            param2.popPose();
            param2.popPose();
        }
    }

    public static void renderPatterns(
        BannerBlockEntity param0, PoseStack param1, MultiBufferSource param2, int param3, int param4, ModelPart param5, boolean param6
    ) {
        param5.render(param1, ModelBakery.BANNER_BASE.buffer(param2, RenderType::entitySolid), param3, param4);
        List<BannerPattern> var0 = param0.getPatterns();
        List<DyeColor> var1 = param0.getColors();

        for(int var2 = 0; var2 < 17 && var2 < var0.size() && var2 < var1.size(); ++var2) {
            BannerPattern var3 = var0.get(var2);
            DyeColor var4 = var1.get(var2);
            float[] var5 = var4.getTextureDiffuseColors();
            Material var6 = new Material(param6 ? Sheets.BANNER_SHEET : Sheets.SHIELD_SHEET, var3.location(param6));
            param5.render(param1, var6.buffer(param2, RenderType::entityNoOutline), param3, param4, var5[0], var5[1], var5[2], 1.0F);
        }

    }
}
