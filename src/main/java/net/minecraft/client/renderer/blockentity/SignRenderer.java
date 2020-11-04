package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Map;
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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SignRenderer implements BlockEntityRenderer<SignBlockEntity> {
    private final Map<WoodType, SignRenderer.SignModel> signModels;
    private final Font font;

    public SignRenderer(BlockEntityRendererProvider.Context param0) {
        this.signModels = WoodType.values()
            .collect(
                ImmutableMap.toImmutableMap(param0x -> param0x, param1 -> new SignRenderer.SignModel(param0.getLayer(ModelLayers.createSignModelName(param1))))
            );
        this.font = param0.getFont();
    }

    public void render(SignBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        BlockState var0 = param0.getBlockState();
        param2.pushPose();
        float var1 = 0.6666667F;
        WoodType var2 = getWoodType(var0.getBlock());
        SignRenderer.SignModel var3 = this.signModels.get(var2);
        if (var0.getBlock() instanceof StandingSignBlock) {
            param2.translate(0.5, 0.5, 0.5);
            float var4 = -((float)(var0.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);
            param2.mulPose(Vector3f.YP.rotationDegrees(var4));
            var3.stick.visible = true;
        } else {
            param2.translate(0.5, 0.5, 0.5);
            float var5 = -var0.getValue(WallSignBlock.FACING).toYRot();
            param2.mulPose(Vector3f.YP.rotationDegrees(var5));
            param2.translate(0.0, -0.3125, -0.4375);
            var3.stick.visible = false;
        }

        param2.pushPose();
        param2.scale(0.6666667F, -0.6666667F, -0.6666667F);
        Material var6 = Sheets.signTexture(var2);
        VertexConsumer var7 = var6.buffer(param3, var3::renderType);
        var3.root.render(param2, var7, param4, param5);
        param2.popPose();
        float var8 = 0.010416667F;
        param2.translate(0.0, 0.33333334F, 0.046666667F);
        param2.scale(0.010416667F, -0.010416667F, 0.010416667F);
        int var9 = param0.getColor().getTextColor();
        double var10 = 0.4;
        int var11 = (int)((double)NativeImage.getR(var9) * 0.4);
        int var12 = (int)((double)NativeImage.getG(var9) * 0.4);
        int var13 = (int)((double)NativeImage.getB(var9) * 0.4);
        int var14 = NativeImage.combine(0, var13, var12, var11);
        int var15 = 20;

        for(int var16 = 0; var16 < 4; ++var16) {
            FormattedCharSequence var17 = param0.getRenderMessage(var16, param0x -> {
                List<FormattedCharSequence> var0x = this.font.split(param0x, 90);
                return var0x.isEmpty() ? FormattedCharSequence.EMPTY : var0x.get(0);
            });
            if (var17 != null) {
                float var18 = (float)(-this.font.width(var17) / 2);
                this.font.drawInBatch(var17, var18, (float)(var16 * 10 - 20), var14, false, param2.last().pose(), param3, false, 0, param4);
            }
        }

        param2.popPose();
    }

    public static WoodType getWoodType(Block param0) {
        WoodType var0;
        if (param0 instanceof SignBlock) {
            var0 = ((SignBlock)param0).type();
        } else {
            var0 = WoodType.OAK;
        }

        return var0;
    }

    public static SignRenderer.SignModel createSignModel(EntityModelSet param0, WoodType param1) {
        return new SignRenderer.SignModel(param0.getLayer(ModelLayers.createSignModelName(param1)));
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
