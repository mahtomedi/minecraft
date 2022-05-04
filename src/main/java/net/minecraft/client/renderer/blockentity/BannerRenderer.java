package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import java.util.List;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
public class BannerRenderer implements BlockEntityRenderer<BannerBlockEntity> {
    private static final int BANNER_WIDTH = 20;
    private static final int BANNER_HEIGHT = 40;
    private static final int MAX_PATTERNS = 16;
    public static final String FLAG = "flag";
    private static final String POLE = "pole";
    private static final String BAR = "bar";
    private final ModelPart flag;
    private final ModelPart pole;
    private final ModelPart bar;

    public BannerRenderer(BlockEntityRendererProvider.Context param0) {
        ModelPart var0 = param0.bakeLayer(ModelLayers.BANNER);
        this.flag = var0.getChild("flag");
        this.pole = var0.getChild("pole");
        this.bar = var0.getChild("bar");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F), PartPose.ZERO);
        var1.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(44, 0).addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F), PartPose.ZERO);
        var1.addOrReplaceChild("bar", CubeListBuilder.create().texOffs(0, 42).addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F), PartPose.ZERO);
        return LayerDefinition.create(var0, 64, 64);
    }

    public void render(BannerBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        List<Pair<Holder<BannerPattern>, DyeColor>> var0 = param0.getPatterns();
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

    public static void renderPatterns(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        int param3,
        ModelPart param4,
        Material param5,
        boolean param6,
        List<Pair<Holder<BannerPattern>, DyeColor>> param7
    ) {
        renderPatterns(param0, param1, param2, param3, param4, param5, param6, param7, false);
    }

    public static void renderPatterns(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        int param3,
        ModelPart param4,
        Material param5,
        boolean param6,
        List<Pair<Holder<BannerPattern>, DyeColor>> param7,
        boolean param8
    ) {
        param4.render(param0, param5.buffer(param1, RenderType::entitySolid, param8), param2, param3);

        for(int var0 = 0; var0 < 17 && var0 < param7.size(); ++var0) {
            Pair<Holder<BannerPattern>, DyeColor> var1 = param7.get(var0);
            float[] var2 = var1.getSecond().getTextureDiffuseColors();
            var1.getFirst()
                .unwrapKey()
                .map(param1x -> param6 ? Sheets.getBannerMaterial(param1x) : Sheets.getShieldMaterial(param1x))
                .ifPresent(
                    param6x -> param4.render(param0, param6x.buffer(param1, RenderType::entityNoOutline), param2, param3, var2[0], var2[1], var2[2], 1.0F)
                );
        }

    }
}
