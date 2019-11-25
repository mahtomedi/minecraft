package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.model.geom.ModelPart;
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
public class BedRenderer extends BlockEntityRenderer<BedBlockEntity> {
    private final ModelPart headPiece;
    private final ModelPart footPiece;
    private final ModelPart[] legs = new ModelPart[4];

    public BedRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
        this.headPiece = new ModelPart(64, 64, 0, 0);
        this.headPiece.addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F, 0.0F);
        this.footPiece = new ModelPart(64, 64, 0, 22);
        this.footPiece.addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F, 0.0F);
        this.legs[0] = new ModelPart(64, 64, 50, 0);
        this.legs[1] = new ModelPart(64, 64, 50, 6);
        this.legs[2] = new ModelPart(64, 64, 50, 12);
        this.legs[3] = new ModelPart(64, 64, 50, 18);
        this.legs[0].addBox(0.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F);
        this.legs[1].addBox(0.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F);
        this.legs[2].addBox(-16.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F);
        this.legs[3].addBox(-16.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F);
        this.legs[0].xRot = (float) (Math.PI / 2);
        this.legs[1].xRot = (float) (Math.PI / 2);
        this.legs[2].xRot = (float) (Math.PI / 2);
        this.legs[3].xRot = (float) (Math.PI / 2);
        this.legs[0].zRot = 0.0F;
        this.legs[1].zRot = (float) (Math.PI / 2);
        this.legs[2].zRot = (float) (Math.PI * 3.0 / 2.0);
        this.legs[3].zRot = (float) Math.PI;
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
            this.renderPiece(param2, param3, var2.getValue(BedBlock.PART) == BedPart.HEAD, var2.getValue(BedBlock.FACING), var0, var4, param5, false);
        } else {
            this.renderPiece(param2, param3, true, Direction.SOUTH, var0, param4, param5, false);
            this.renderPiece(param2, param3, false, Direction.SOUTH, var0, param4, param5, true);
        }

    }

    private void renderPiece(
        PoseStack param0, MultiBufferSource param1, boolean param2, Direction param3, Material param4, int param5, int param6, boolean param7
    ) {
        this.headPiece.visible = param2;
        this.footPiece.visible = !param2;
        this.legs[0].visible = !param2;
        this.legs[1].visible = param2;
        this.legs[2].visible = !param2;
        this.legs[3].visible = param2;
        param0.pushPose();
        param0.translate(0.0, 0.5625, param7 ? -1.0 : 0.0);
        param0.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        param0.translate(0.5, 0.5, 0.5);
        param0.mulPose(Vector3f.ZP.rotationDegrees(180.0F + param3.toYRot()));
        param0.translate(-0.5, -0.5, -0.5);
        VertexConsumer var0 = param4.buffer(param1, RenderType::entitySolid);
        this.headPiece.render(param0, var0, param5, param6);
        this.footPiece.render(param0, var0, param5, param6);
        this.legs[0].render(param0, var0, param5, param6);
        this.legs[1].render(param0, var0, param5, param6);
        this.legs[2].render(param0, var0, param5, param6);
        this.legs[3].render(param0, var0, param5, param6);
        param0.popPose();
    }
}
