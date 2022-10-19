package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.Map;
import net.minecraft.client.model.Model;
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
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HangingSignRenderer extends SignRenderer {
    private static final String PLANK = "plank";
    private static final String V_CHAINS = "vChains";
    public static final String NORMAL_CHAINS = "normalChains";
    public static final String CHAIN_L_1 = "chainL1";
    public static final String CHAIN_L_2 = "chainL2";
    public static final String CHAIN_R_1 = "chainR1";
    public static final String CHAIN_R_2 = "chainR2";
    public static final String BOARD = "board";
    private final Map<WoodType, HangingSignRenderer.HangingSignModel> hangingSignModels;

    public HangingSignRenderer(BlockEntityRendererProvider.Context param0) {
        super(param0);
        this.hangingSignModels = WoodType.values()
            .collect(
                ImmutableMap.toImmutableMap(
                    param0x -> param0x, param1 -> new HangingSignRenderer.HangingSignModel(param0.bakeLayer(ModelLayers.createHangingSignModelName(param1)))
                )
            );
    }

    @Override
    public void render(SignBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        BlockState var0 = param0.getBlockState();
        param2.pushPose();
        WoodType var1 = SignBlock.getWoodType(var0.getBlock());
        HangingSignRenderer.HangingSignModel var2 = this.hangingSignModels.get(var1);
        boolean var3 = !(var0.getBlock() instanceof CeilingHangingSignBlock);
        boolean var4 = var0.hasProperty(BlockStateProperties.ATTACHED) && var0.getValue(BlockStateProperties.ATTACHED);
        param2.translate(0.5, 0.9375, 0.5);
        if (var4) {
            float var5 = -RotationSegment.convertToDegrees(var0.getValue(CeilingHangingSignBlock.ROTATION));
            param2.mulPose(Vector3f.YP.rotationDegrees(var5));
        } else {
            param2.mulPose(Vector3f.YP.rotationDegrees(this.getSignAngle(var0, var3)));
        }

        param2.translate(0.0, -0.3125, 0.0);
        var2.evaluateVisibleParts(var0);
        float var6 = 1.0F;
        this.renderSign(param2, param3, param4, param5, 1.0F, var1, var2);
        this.renderSignText(param0, param2, param3, param4, 1.0F);
    }

    private float getSignAngle(BlockState param0, boolean param1) {
        return param1 ? -param0.getValue(WallSignBlock.FACING).toYRot() : -((float)(param0.getValue(CeilingHangingSignBlock.ROTATION) * 360) / 16.0F);
    }

    @Override
    Material getSignMaterial(WoodType param0) {
        return Sheets.getHangingSignMaterial(param0);
    }

    @Override
    void renderSignModel(PoseStack param0, int param1, int param2, Model param3, VertexConsumer param4) {
        HangingSignRenderer.HangingSignModel var0 = (HangingSignRenderer.HangingSignModel)param3;
        var0.root.render(param0, param4, param1, param2);
    }

    @Override
    Vec3 getTextOffset(float param0) {
        return new Vec3(0.0, (double)(-0.32F * param0), (double)(0.063F * param0));
    }

    public static LayerDefinition createHangingSignLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("board", CubeListBuilder.create().texOffs(0, 12).addBox(-7.0F, 0.0F, -1.0F, 14.0F, 10.0F, 2.0F), PartPose.ZERO);
        var1.addOrReplaceChild("plank", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -6.0F, -2.0F, 16.0F, 2.0F, 4.0F), PartPose.ZERO);
        PartDefinition var2 = var1.addOrReplaceChild("normalChains", CubeListBuilder.create(), PartPose.ZERO);
        var2.addOrReplaceChild(
            "chainL1",
            CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
            PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, (float) (-Math.PI / 4), 0.0F)
        );
        var2.addOrReplaceChild(
            "chainL2",
            CubeListBuilder.create().texOffs(6, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
            PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F)
        );
        var2.addOrReplaceChild(
            "chainR1",
            CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
            PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, (float) (-Math.PI / 4), 0.0F)
        );
        var2.addOrReplaceChild(
            "chainR2",
            CubeListBuilder.create().texOffs(6, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
            PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F)
        );
        var1.addOrReplaceChild("vChains", CubeListBuilder.create().texOffs(14, 6).addBox(-6.0F, -6.0F, 0.0F, 12.0F, 6.0F, 0.0F), PartPose.ZERO);
        return LayerDefinition.create(var0, 64, 32);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class HangingSignModel extends Model {
        public final ModelPart root;
        public final ModelPart plank;
        public final ModelPart vChains;
        public final ModelPart normalChains;

        public HangingSignModel(ModelPart param0) {
            super(RenderType::entityCutoutNoCull);
            this.root = param0;
            this.plank = param0.getChild("plank");
            this.normalChains = param0.getChild("normalChains");
            this.vChains = param0.getChild("vChains");
        }

        public void evaluateVisibleParts(BlockState param0) {
            boolean var0 = !(param0.getBlock() instanceof CeilingHangingSignBlock);
            this.plank.visible = var0;
            this.vChains.visible = false;
            this.normalChains.visible = true;
            if (!var0) {
                boolean var1 = param0.getValue(BlockStateProperties.ATTACHED);
                this.normalChains.visible = !var1;
                this.vChains.visible = var1;
            }

        }

        @Override
        public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6, float param7) {
            this.root.render(param0, param1, param2, param3, param4, param5, param6, param7);
        }
    }
}
