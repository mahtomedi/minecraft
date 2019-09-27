package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LecternRenderer extends BlockEntityRenderer<LecternBlockEntity> {
    private final BookModel bookModel = new BookModel();

    public LecternRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(
        LecternBlockEntity param0, double param1, double param2, double param3, float param4, PoseStack param5, MultiBufferSource param6, int param7
    ) {
        BlockState var0 = param0.getBlockState();
        if (var0.getValue(LecternBlock.HAS_BOOK)) {
            param5.pushPose();
            param5.translate(0.5, 1.0625, 0.5);
            float var1 = var0.getValue(LecternBlock.FACING).getClockWise().toYRot();
            param5.mulPose(Vector3f.YP.rotation(-var1, true));
            param5.mulPose(Vector3f.ZP.rotation(67.5F, true));
            param5.translate(0.0, -0.125, 0.0);
            this.bookModel.setupAnim(0.0F, 0.1F, 0.9F, 1.2F);
            VertexConsumer var2 = param6.getBuffer(RenderType.SOLID);
            this.bookModel.render(param5, var2, 0.0625F, param7, this.getSprite(EnchantTableRenderer.BOOK_LOCATION));
            param5.popPose();
        }
    }
}
