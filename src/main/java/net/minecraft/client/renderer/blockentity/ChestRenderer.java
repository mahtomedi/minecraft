package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Calendar;
import net.minecraft.client.model.geom.ModelPart;
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
public class ChestRenderer<T extends BlockEntity & LidBlockEntity> extends BlockEntityRenderer<T> {
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

    public ChestRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
        Calendar var0 = Calendar.getInstance();
        if (var0.get(2) + 1 == 12 && var0.get(5) >= 24 && var0.get(5) <= 26) {
            this.xmasTextures = true;
        }

        this.bottom = new ModelPart(64, 64, 0, 19);
        this.bottom.addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F, 0.0F);
        this.lid = new ModelPart(64, 64, 0, 0);
        this.lid.addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F, 0.0F);
        this.lid.y = 9.0F;
        this.lid.z = 1.0F;
        this.lock = new ModelPart(64, 64, 0, 0);
        this.lock.addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
        this.lock.y = 8.0F;
        this.doubleLeftBottom = new ModelPart(64, 64, 0, 19);
        this.doubleLeftBottom.addBox(1.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F, 0.0F);
        this.doubleLeftLid = new ModelPart(64, 64, 0, 0);
        this.doubleLeftLid.addBox(1.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F, 0.0F);
        this.doubleLeftLid.y = 9.0F;
        this.doubleLeftLid.z = 1.0F;
        this.doubleLeftLock = new ModelPart(64, 64, 0, 0);
        this.doubleLeftLock.addBox(15.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F, 0.0F);
        this.doubleLeftLock.y = 8.0F;
        this.doubleRightBottom = new ModelPart(64, 64, 0, 19);
        this.doubleRightBottom.addBox(0.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F, 0.0F);
        this.doubleRightLid = new ModelPart(64, 64, 0, 0);
        this.doubleRightLid.addBox(0.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F, 0.0F);
        this.doubleRightLid.y = 9.0F;
        this.doubleRightLid.z = 1.0F;
        this.doubleRightLock = new ModelPart(64, 64, 0, 0);
        this.doubleRightLock.addBox(0.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F, 0.0F);
        this.doubleRightLock.y = 8.0F;
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
                    this.render(param2, var13, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom, var10, var11, param5);
                } else {
                    this.render(param2, var13, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom, var10, var11, param5);
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
