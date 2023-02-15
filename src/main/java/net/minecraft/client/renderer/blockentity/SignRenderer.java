package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SignRenderer implements BlockEntityRenderer<SignBlockEntity> {
    private static final String STICK = "stick";
    private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private final Map<WoodType, SignRenderer.SignModel> signModels;
    private final Font font;

    public SignRenderer(BlockEntityRendererProvider.Context param0) {
        this.signModels = WoodType.values()
            .collect(
                ImmutableMap.toImmutableMap(param0x -> param0x, param1 -> new SignRenderer.SignModel(param0.bakeLayer(ModelLayers.createSignModelName(param1))))
            );
        this.font = param0.getFont();
    }

    public void render(SignBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        BlockState var0 = param0.getBlockState();
        param2.pushPose();
        float var1 = 0.6666667F;
        WoodType var2 = SignBlock.getWoodType(var0.getBlock());
        SignRenderer.SignModel var3 = this.signModels.get(var2);
        if (var0.getBlock() instanceof StandingSignBlock) {
            param2.translate(0.5F, 0.5F, 0.5F);
            float var4 = -RotationSegment.convertToDegrees(var0.getValue(StandingSignBlock.ROTATION));
            param2.mulPose(Axis.YP.rotationDegrees(var4));
            var3.stick.visible = true;
        } else {
            param2.translate(0.5F, 0.5F, 0.5F);
            float var5 = -var0.getValue(WallSignBlock.FACING).toYRot();
            param2.mulPose(Axis.YP.rotationDegrees(var5));
            param2.translate(0.0F, -0.3125F, -0.4375F);
            var3.stick.visible = false;
        }

        this.renderSign(param2, param3, param4, param5, 0.6666667F, var2, var3);
        this.renderSignText(param0, param2, param3, param4, 0.6666667F);
    }

    void renderSign(PoseStack param0, MultiBufferSource param1, int param2, int param3, float param4, WoodType param5, Model param6) {
        param0.pushPose();
        param0.scale(param4, -param4, -param4);
        Material var0 = this.getSignMaterial(param5);
        VertexConsumer var1 = var0.buffer(param1, param6::renderType);
        this.renderSignModel(param0, param2, param3, param6, var1);
        param0.popPose();
    }

    void renderSignModel(PoseStack param0, int param1, int param2, Model param3, VertexConsumer param4) {
        SignRenderer.SignModel var0 = (SignRenderer.SignModel)param3;
        var0.root.render(param0, param4, param1, param2);
    }

    Material getSignMaterial(WoodType param0) {
        return Sheets.getSignMaterial(param0);
    }

    void renderSignText(SignBlockEntity param0, PoseStack param1, MultiBufferSource param2, int param3, float param4) {
        float var0 = 0.015625F * param4;
        Vec3 var1 = this.getTextOffset(param4);
        param1.translate(var1.x, var1.y, var1.z);
        param1.scale(var0, -var0, var0);
        int var2 = getDarkColor(param0);
        int var3 = 4 * param0.getTextLineHeight() / 2;
        FormattedCharSequence[] var4 = param0.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), param1x -> {
            List<FormattedCharSequence> var0x = this.font.split(param1x, param0.getMaxTextLineWidth());
            return var0x.isEmpty() ? FormattedCharSequence.EMPTY : var0x.get(0);
        });
        int var5;
        boolean var6;
        int var7;
        if (param0.hasGlowingText()) {
            var5 = param0.getColor().getTextColor();
            var6 = isOutlineVisible(param0, var5);
            var7 = 15728880;
        } else {
            var5 = var2;
            var6 = false;
            var7 = param3;
        }

        for(int var11 = 0; var11 < 4; ++var11) {
            FormattedCharSequence var12 = var4[var11];
            float var13 = (float)(-this.font.width(var12) / 2);
            if (var6) {
                this.font
                    .drawInBatch8xOutline(var12, var13, (float)(var11 * param0.getTextLineHeight() - var3), var5, var2, param1.last().pose(), param2, var7);
            } else {
                this.font
                    .drawInBatch(
                        var12,
                        var13,
                        (float)(var11 * param0.getTextLineHeight() - var3),
                        var5,
                        false,
                        param1.last().pose(),
                        param2,
                        Font.DisplayMode.NORMAL,
                        0,
                        var7
                    );
            }
        }

        param1.popPose();
    }

    Vec3 getTextOffset(float param0) {
        return new Vec3(0.0, (double)(0.5F * param0), (double)(0.07F * param0));
    }

    static boolean isOutlineVisible(SignBlockEntity param0, int param1) {
        if (param1 == DyeColor.BLACK.getTextColor()) {
            return true;
        } else {
            Minecraft var0 = Minecraft.getInstance();
            LocalPlayer var1 = var0.player;
            if (var1 != null && var0.options.getCameraType().isFirstPerson() && var1.isScoping()) {
                return true;
            } else {
                Entity var2 = var0.getCameraEntity();
                return var2 != null && var2.distanceToSqr(Vec3.atCenterOf(param0.getBlockPos())) < (double)OUTLINE_RENDER_DISTANCE;
            }
        }
    }

    static int getDarkColor(SignBlockEntity param0) {
        int var0 = param0.getColor().getTextColor();
        if (var0 == DyeColor.BLACK.getTextColor() && param0.hasGlowingText()) {
            return -988212;
        } else {
            double var1 = 0.4;
            int var2 = (int)((double)FastColor.ARGB32.red(var0) * 0.4);
            int var3 = (int)((double)FastColor.ARGB32.green(var0) * 0.4);
            int var4 = (int)((double)FastColor.ARGB32.blue(var0) * 0.4);
            return FastColor.ARGB32.color(0, var2, var3, var4);
        }
    }

    public static SignRenderer.SignModel createSignModel(EntityModelSet param0, WoodType param1) {
        return new SignRenderer.SignModel(param0.bakeLayer(ModelLayers.createSignModelName(param1)));
    }

    public static LayerDefinition createSignLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F), PartPose.ZERO);
        var1.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.ZERO);
        return LayerDefinition.create(var0, 64, 32);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class SignModel extends Model {
        public final ModelPart root;
        public final ModelPart stick;

        public SignModel(ModelPart param0) {
            super(RenderType::entityCutoutNoCull);
            this.root = param0;
            this.stick = param0.getChild("stick");
        }

        @Override
        public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
            this.root.render(param0, param1, param2, param3, param4, param5, param6, param7);
        }
    }
}
