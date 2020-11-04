package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
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
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BedRenderer implements BlockEntityRenderer<BedBlockEntity> {
    private final ModelPart headRoot;
    private final ModelPart footRoot;

    public BedRenderer(BlockEntityRendererProvider.Context param0) {
        this.headRoot = param0.getLayer(ModelLayers.BED_HEAD);
        this.footRoot = param0.getLayer(ModelLayers.BED_FOOT);
    }

    public static LayerDefinition createHeadLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), PartPose.ZERO);
        var1.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(50, 6).addBox(0.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F),
            PartPose.rotation((float) (Math.PI / 2), 0.0F, (float) (Math.PI / 2))
        );
        var1.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(50, 18).addBox(-16.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F),
            PartPose.rotation((float) (Math.PI / 2), 0.0F, (float) Math.PI)
        );
        return LayerDefinition.create(var0, 64, 64);
    }

    public static LayerDefinition createFootLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 22).addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), PartPose.ZERO);
        var1.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(50, 0).addBox(0.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F),
            PartPose.rotation((float) (Math.PI / 2), 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(50, 12).addBox(-16.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F),
            PartPose.rotation((float) (Math.PI / 2), 0.0F, (float) (Math.PI * 3.0 / 2.0))
        );
        return LayerDefinition.create(var0, 64, 64);
    }

    public void render(BedBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        Material var0 = Sheets.BED_TEXTURES[param0.getColor().getId()];
        Level var1 = param0.getLevel();
        if (var1 != null) {
            BlockState var2 = param0.getBlockState();
            DoubleBlockCombiner.NeighborCombineResult<? extends BedBlockEntity> var3 = DoubleBlockCombiner.combineWithNeigbour(
                BlockEntityType.BED,
                BedBlock::getBlockType,
                BedBlock::getConnectedDirection,
                ChestBlock.FACING,
                var2,
                var1,
                param0.getBlockPos(),
                (param0x, param1x) -> false
            );
            int var4 = var3.<Int2IntFunction>apply(new BrightnessCombiner<>()).get(param4);
            this.renderPiece(
                param2,
                param3,
                var2.getValue(BedBlock.PART) == BedPart.HEAD ? this.headRoot : this.footRoot,
                var2.getValue(BedBlock.FACING),
                var0,
                var4,
                param5,
                false
            );
        } else {
            this.renderPiece(param2, param3, this.headRoot, Direction.SOUTH, var0, param4, param5, false);
            this.renderPiece(param2, param3, this.footRoot, Direction.SOUTH, var0, param4, param5, true);
        }

    }

    private void renderPiece(
        PoseStack param0, MultiBufferSource param1, ModelPart param2, Direction param3, Material param4, int param5, int param6, boolean param7
    ) {
        param0.pushPose();
        param0.translate(0.0, 0.5625, param7 ? -1.0 : 0.0);
        param0.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        param0.translate(0.5, 0.5, 0.5);
        param0.mulPose(Vector3f.ZP.rotationDegrees(180.0F + param3.toYRot()));
        param0.translate(-0.5, -0.5, -0.5);
        VertexConsumer var0 = param4.buffer(param1, RenderType::entitySolid);
        param2.render(param0, var0, param5, param6);
        param0.popPose();
    }
}
