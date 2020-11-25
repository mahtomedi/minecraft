package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Calendar;
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
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChestRenderer<T extends BlockEntity & LidBlockEntity> implements BlockEntityRenderer<T> {
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final ModelPart doubleLeftLid;
    private final ModelPart doubleLeftBottom;
    private final ModelPart doubleLeftLock;
    private final ModelPart doubleRightLid;
    private final ModelPart doubleRightBottom;
    private final ModelPart doubleRightLock;
    private boolean xmasTextures;

    public ChestRenderer(BlockEntityRendererProvider.Context param0) {
        Calendar var0 = Calendar.getInstance();
        if (var0.get(2) + 1 == 12 && var0.get(5) >= 24 && var0.get(5) <= 26) {
            this.xmasTextures = true;
        }

        ModelPart var1 = param0.bakeLayer(ModelLayers.CHEST);
        this.bottom = var1.getChild("bottom");
        this.lid = var1.getChild("lid");
        this.lock = var1.getChild("lock");
        ModelPart var2 = param0.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT);
        this.doubleLeftBottom = var2.getChild("bottom");
        this.doubleLeftLid = var2.getChild("lid");
        this.doubleLeftLock = var2.getChild("lock");
        ModelPart var3 = param0.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT);
        this.doubleRightBottom = var3.getChild("bottom");
        this.doubleRightLid = var3.getChild("lid");
        this.doubleRightLock = var3.getChild("lock");
    }

    public static LayerDefinition createSingleBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F), PartPose.ZERO);
        var1.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        var1.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(var0, 64, 64);
    }

    public static LayerDefinition createDoubleBodyRightLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), PartPose.ZERO);
        var1.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        var1.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(15.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(var0, 64, 64);
    }

    public static LayerDefinition createDoubleBodyLeftLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), PartPose.ZERO);
        var1.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        var1.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(var0, 64, 64);
    }

    @Override
    public void render(T param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        Level var0 = param0.getLevel();
        boolean var1 = var0 != null;
        BlockState var2 = var1 ? param0.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        ChestType var3 = var2.hasProperty(ChestBlock.TYPE) ? var2.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        Block var4 = var2.getBlock();
        if (var4 instanceof AbstractChestBlock) {
            AbstractChestBlock<?> var5 = (AbstractChestBlock)var4;
            boolean var6 = var3 != ChestType.SINGLE;
            param2.pushPose();
            float var7 = var2.getValue(ChestBlock.FACING).toYRot();
            param2.translate(0.5, 0.5, 0.5);
            param2.mulPose(Vector3f.YP.rotationDegrees(-var7));
            param2.translate(-0.5, -0.5, -0.5);
            DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> var8;
            if (var1) {
                var8 = var5.combine(var2, var0, param0.getBlockPos(), true);
            } else {
                var8 = DoubleBlockCombiner.Combiner::acceptNone;
            }

            float var10 = var8.<Float2FloatFunction>apply(ChestBlock.opennessCombiner(param0)).get(param1);
            var10 = 1.0F - var10;
            var10 = 1.0F - var10 * var10 * var10;
            int var11 = var8.<Int2IntFunction>apply(new BrightnessCombiner<>()).applyAsInt(param4);
            Material var12 = Sheets.chooseMaterial(param0, var3, this.xmasTextures);
            VertexConsumer var13 = var12.buffer(param3, RenderType::entityCutout);
            if (var6) {
                if (var3 == ChestType.LEFT) {
                    this.render(param2, var13, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom, var10, var11, param5);
                } else {
                    this.render(param2, var13, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom, var10, var11, param5);
                }
            } else {
                this.render(param2, var13, this.lid, this.lock, this.bottom, var10, var11, param5);
            }

            param2.popPose();
        }
    }

    private void render(PoseStack param0, VertexConsumer param1, ModelPart param2, ModelPart param3, ModelPart param4, float param5, int param6, int param7) {
        param2.xRot = -(param5 * (float) (Math.PI / 2));
        param3.xRot = param2.xRot;
        param2.render(param0, param1, param6, param7);
        param3.render(param0, param1, param6, param7);
        param4.render(param0, param1, param6, param7);
    }
}
