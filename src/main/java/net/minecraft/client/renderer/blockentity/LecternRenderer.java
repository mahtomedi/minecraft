package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LecternRenderer implements BlockEntityRenderer<LecternBlockEntity> {
    private final BookModel bookModel;

    public LecternRenderer(BlockEntityRendererProvider.Context param0) {
        this.bookModel = new BookModel(param0.bakeLayer(ModelLayers.BOOK));
    }

    public void render(LecternBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        BlockState var0 = param0.getBlockState();
        if (var0.getValue(LecternBlock.HAS_BOOK)) {
            param2.pushPose();
            param2.translate(0.5F, 1.0625F, 0.5F);
            float var1 = var0.getValue(LecternBlock.FACING).getClockWise().toYRot();
            param2.mulPose(Axis.YP.rotationDegrees(-var1));
            param2.mulPose(Axis.ZP.rotationDegrees(67.5F));
            param2.translate(0.0F, -0.125F, 0.0F);
            this.bookModel.setupAnim(0.0F, 0.1F, 0.9F, 1.2F);
            VertexConsumer var2 = EnchantTableRenderer.BOOK_LOCATION.buffer(param3, RenderType::entitySolid);
            this.bookModel.render(param2, var2, param4, param5, 1.0F, 1.0F, 1.0F, 1.0F);
            param2.popPose();
        }
    }
}
