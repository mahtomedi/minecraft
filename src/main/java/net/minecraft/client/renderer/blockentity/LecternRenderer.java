package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LecternRenderer extends BatchedBlockEntityRenderer<LecternBlockEntity> {
    private final BookModel bookModel = new BookModel();

    protected void renderToBuffer(
        LecternBlockEntity param0,
        double param1,
        double param2,
        double param3,
        float param4,
        int param5,
        RenderType param6,
        BufferBuilder param7,
        int param8,
        int param9
    ) {
        BlockState var0 = param0.getBlockState();
        if (var0.getValue(LecternBlock.HAS_BOOK)) {
            param7.pushPose();
            param7.translate(0.5, 1.0625, 0.5);
            float var1 = var0.getValue(LecternBlock.FACING).getClockWise().toYRot();
            param7.multiplyPose(new Quaternion(Vector3f.YP, -var1, true));
            param7.multiplyPose(new Quaternion(Vector3f.ZP, 67.5F, true));
            param7.translate(0.0, -0.125, 0.0);
            this.bookModel.setupAnim(0.0F, 0.1F, 0.9F, 1.2F);
            this.bookModel.render(param7, 0.0625F, param8, param9, this.getSprite(EnchantTableRenderer.BOOK_LOCATION));
            param7.popPose();
        }
    }
}
