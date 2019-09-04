package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.BookModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantTableRenderer extends BlockEntityRenderer<EnchantmentTableBlockEntity> {
    private static final ResourceLocation BOOK_LOCATION = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private final BookModel bookModel = new BookModel();

    public void render(EnchantmentTableBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)param1 + 0.5F, (float)param2 + 0.75F, (float)param3 + 0.5F);
        float var0 = (float)param0.time + param4;
        RenderSystem.translatef(0.0F, 0.1F + Mth.sin(var0 * 0.1F) * 0.01F, 0.0F);
        float var1 = param0.rot - param0.oRot;

        while(var1 >= (float) Math.PI) {
            var1 -= (float) (Math.PI * 2);
        }

        while(var1 < (float) -Math.PI) {
            var1 += (float) (Math.PI * 2);
        }

        float var2 = param0.oRot + var1 * param4;
        RenderSystem.rotatef(-var2 * (180.0F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef(80.0F, 0.0F, 0.0F, 1.0F);
        this.bindTexture(BOOK_LOCATION);
        float var3 = Mth.lerp(param4, param0.oFlip, param0.flip) + 0.25F;
        float var4 = Mth.lerp(param4, param0.oFlip, param0.flip) + 0.75F;
        var3 = (var3 - (float)Mth.fastFloor((double)var3)) * 1.6F - 0.3F;
        var4 = (var4 - (float)Mth.fastFloor((double)var4)) * 1.6F - 0.3F;
        if (var3 < 0.0F) {
            var3 = 0.0F;
        }

        if (var4 < 0.0F) {
            var4 = 0.0F;
        }

        if (var3 > 1.0F) {
            var3 = 1.0F;
        }

        if (var4 > 1.0F) {
            var4 = 1.0F;
        }

        float var5 = Mth.lerp(param4, param0.oOpen, param0.open);
        RenderSystem.enableCull();
        this.bookModel.render(var0, var3, var4, var5, 0.0F, 0.0625F);
        RenderSystem.popMatrix();
    }
}
