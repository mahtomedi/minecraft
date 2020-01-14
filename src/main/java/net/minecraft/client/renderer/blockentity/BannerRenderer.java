package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
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
        List<Pair<BannerPattern, DyeColor>> var0 = param0.getPatterns();
        if (var0 != null) {
            float var1 = 0.6666667F;
            boolean var2 = param0.getLevel() == null;
            param2.pushPose();
            long var3;
            if (var2) {
                var3 = 0L;
                param2.translate(0.5, 0.5, 0.5);
                this.pole.visible = true;
            } else {
                var3 = param0.getLevel().getGameTime();
                BlockState var5 = param0.getBlockState();
                if (var5.getBlock() instanceof BannerBlock) {
                    param2.translate(0.5, 0.5, 0.5);
                    float var6 = (float)(-var5.getValue(BannerBlock.ROTATION) * 360) / 16.0F;
                    param2.mulPose(Vector3f.YP.rotationDegrees(var6));
                    this.pole.visible = true;
                } else {
                    param2.translate(0.5, -0.16666667F, 0.5);
                    float var7 = -var5.getValue(WallBannerBlock.FACING).toYRot();
                    param2.mulPose(Vector3f.YP.rotationDegrees(var7));
                    param2.translate(0.0, -0.3125, -0.4375);
                    this.pole.visible = false;
                }
            }

            param2.pushPose();
            param2.scale(0.6666667F, -0.6666667F, -0.6666667F);
            VertexConsumer var8 = ModelBakery.BANNER_BASE.buffer(param3, RenderType::entitySolid);
            this.pole.render(param2, var8, param4, param5);
            this.bar.render(param2, var8, param4, param5);
            BlockPos var9 = param0.getBlockPos();
            float var10 = ((float)Math.floorMod((long)(var9.getX() * 7 + var9.getY() * 9 + var9.getZ() * 13) + var3, 100L) + param1) / 100.0F;
            this.flag.xRot = (-0.0125F + 0.01F * Mth.cos((float) (Math.PI * 2) * var10)) * (float) Math.PI;
            this.flag.y = -32.0F;
            renderPatterns(param2, param3, param4, param5, this.flag, ModelBakery.BANNER_BASE, true, var0);
            param2.popPose();
            param2.popPose();
        }
    }

    public static void renderPatterns(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        int param3,
        ModelPart param4,
        Material param5,
        boolean param6,
        List<Pair<BannerPattern, DyeColor>> param7
    ) {
        param4.render(param0, param5.buffer(param1, RenderType::entitySolid), param2, param3);

        for(int var0 = 0; var0 < 17 && var0 < param7.size(); ++var0) {
            Pair<BannerPattern, DyeColor> var1 = param7.get(var0);
            float[] var2 = var1.getSecond().getTextureDiffuseColors();
            Material var3 = new Material(param6 ? Sheets.BANNER_SHEET : Sheets.SHIELD_SHEET, var1.getFirst().location(param6));
            param4.render(param0, var3.buffer(param1, RenderType::entityNoOutline), param2, param3, var2[0], var2[1], var2[2], 1.0F);
        }

    }
}
