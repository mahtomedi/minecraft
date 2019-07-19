package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.BookModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LecternRenderer extends BlockEntityRenderer<LecternBlockEntity> {
    private static final ResourceLocation BOOK_LOCATION = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private final BookModel bookModel = new BookModel();

    public void render(LecternBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        BlockState var0 = param0.getBlockState();
        if (var0.getValue(LecternBlock.HAS_BOOK)) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 1.0F + 0.0625F, (float)param3 + 0.5F);
            float var1 = var0.getValue(LecternBlock.FACING).getClockWise().toYRot();
            GlStateManager.rotatef(-var1, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotatef(67.5F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translatef(0.0F, -0.125F, 0.0F);
            this.bindTexture(BOOK_LOCATION);
            GlStateManager.enableCull();
            this.bookModel.render(0.0F, 0.1F, 0.9F, 1.2F, 0.0F, 0.0625F);
            GlStateManager.popMatrix();
        }
    }
}
