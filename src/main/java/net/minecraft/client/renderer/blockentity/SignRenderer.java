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
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SignRenderer implements BlockEntityRenderer<SignBlockEntity> {
    private static final String STICK = "stick";
    private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private static final float RENDER_SCALE = 0.6666667F;
    private static final Vec3 TEXT_OFFSET = new Vec3(0.0, 0.33333334F, 0.046666667F);
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
        SignBlock var1 = (SignBlock)var0.getBlock();
        WoodType var2 = SignBlock.getWoodType(var1);
        SignRenderer.SignModel var3 = this.signModels.get(var2);
        var3.stick.visible = var0.getBlock() instanceof StandingSignBlock;
        this.renderSignWithText(param0, param2, param3, param4, param5, var0, var1, var2, var3);
    }

    public float getSignModelRenderScale() {
        return 0.6666667F;
    }

    public float getSignTextRenderScale() {
        return 0.6666667F;
    }

    void renderSignWithText(
        SignBlockEntity param0,
        PoseStack param1,
        MultiBufferSource param2,
        int param3,
        int param4,
        BlockState param5,
        SignBlock param6,
        WoodType param7,
        Model param8
    ) {
        param1.pushPose();
        this.translateSign(param1, -param6.getYRotationDegrees(param5), param5);
        this.renderSign(param1, param2, param3, param4, param7, param8);
        this.renderSignText(param0.getBlockPos(), param0.getFrontText(), param1, param2, param3, param0.getTextLineHeight(), param0.getMaxTextLineWidth(), true);
        this.renderSignText(param0.getBlockPos(), param0.getBackText(), param1, param2, param3, param0.getTextLineHeight(), param0.getMaxTextLineWidth(), false);
        param1.popPose();
    }

    void translateSign(PoseStack param0, float param1, BlockState param2) {
        param0.translate(0.5F, 0.75F * this.getSignModelRenderScale(), 0.5F);
        param0.mulPose(Axis.YP.rotationDegrees(param1));
        if (!(param2.getBlock() instanceof StandingSignBlock)) {
            param0.translate(0.0F, -0.3125F, -0.4375F);
        }

    }

    void renderSign(PoseStack param0, MultiBufferSource param1, int param2, int param3, WoodType param4, Model param5) {
        param0.pushPose();
        float var0 = this.getSignModelRenderScale();
        param0.scale(var0, -var0, -var0);
        Material var1 = this.getSignMaterial(param4);
        VertexConsumer var2 = var1.buffer(param1, param5::renderType);
        this.renderSignModel(param0, param2, param3, param5, var2);
        param0.popPose();
    }

    void renderSignModel(PoseStack param0, int param1, int param2, Model param3, VertexConsumer param4) {
        SignRenderer.SignModel var0 = (SignRenderer.SignModel)param3;
        var0.root.render(param0, param4, param1, param2);
    }

    Material getSignMaterial(WoodType param0) {
        return Sheets.getSignMaterial(param0);
    }

    void renderSignText(BlockPos param0, SignText param1, PoseStack param2, MultiBufferSource param3, int param4, int param5, int param6, boolean param7) {
        param2.pushPose();
        this.translateSignText(param2, param7, this.getTextOffset());
        int var0 = getDarkColor(param1);
        int var1 = 4 * param5 / 2;
        FormattedCharSequence[] var2 = param1.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), param1x -> {
            List<FormattedCharSequence> var0x = this.font.split(param1x, param6);
            return var0x.isEmpty() ? FormattedCharSequence.EMPTY : var0x.get(0);
        });
        int var3;
        boolean var4;
        int var5;
        if (param1.hasGlowingText()) {
            var3 = param1.getColor().getTextColor();
            var4 = isOutlineVisible(param0, var3);
            var5 = 15728880;
        } else {
            var3 = var0;
            var4 = false;
            var5 = param4;
        }

        for(int var9 = 0; var9 < 4; ++var9) {
            FormattedCharSequence var10 = var2[var9];
            float var11 = (float)(-this.font.width(var10) / 2);
            if (var4) {
                this.font.drawInBatch8xOutline(var10, var11, (float)(var9 * param5 - var1), var3, var0, param2.last().pose(), param3, var5);
            } else {
                this.font
                    .drawInBatch(
                        var10, var11, (float)(var9 * param5 - var1), var3, false, param2.last().pose(), param3, Font.DisplayMode.POLYGON_OFFSET, 0, var5
                    );
            }
        }

        param2.popPose();
    }

    private void translateSignText(PoseStack param0, boolean param1, Vec3 param2) {
        if (!param1) {
            param0.mulPose(Axis.YP.rotationDegrees(180.0F));
        }

        float var0 = 0.015625F * this.getSignTextRenderScale();
        param0.translate(param2.x, param2.y, param2.z);
        param0.scale(var0, -var0, var0);
    }

    Vec3 getTextOffset() {
        return TEXT_OFFSET;
    }

    static boolean isOutlineVisible(BlockPos param0, int param1) {
        if (param1 == DyeColor.BLACK.getTextColor()) {
            return true;
        } else {
            Minecraft var0 = Minecraft.getInstance();
            LocalPlayer var1 = var0.player;
            if (var1 != null && var0.options.getCameraType().isFirstPerson() && var1.isScoping()) {
                return true;
            } else {
                Entity var2 = var0.getCameraEntity();
                return var2 != null && var2.distanceToSqr(Vec3.atCenterOf(param0)) < (double)OUTLINE_RENDER_DISTANCE;
            }
        }
    }

    static int getDarkColor(SignText param0) {
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
