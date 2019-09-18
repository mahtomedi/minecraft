package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantTableRenderer extends BatchedBlockEntityRenderer<EnchantmentTableBlockEntity> {
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("entity/enchanting_table_book");
    private final BookModel bookModel = new BookModel();

    protected void renderToBuffer(
        EnchantmentTableBlockEntity param0,
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
        param7.pushPose();
        param7.translate(0.5, 0.75, 0.5);
        float var0 = (float)param0.time + param4;
        param7.translate(0.0, (double)(0.1F + Mth.sin(var0 * 0.1F) * 0.01F), 0.0);
        float var1 = param0.rot - param0.oRot;

        while(var1 >= (float) Math.PI) {
            var1 -= (float) (Math.PI * 2);
        }

        while(var1 < (float) -Math.PI) {
            var1 += (float) (Math.PI * 2);
        }

        float var2 = param0.oRot + var1 * param4;
        param7.multiplyPose(new Quaternion(Vector3f.YP, -var2, false));
        param7.multiplyPose(new Quaternion(Vector3f.ZP, 80.0F, true));
        float var3 = Mth.lerp(param4, param0.oFlip, param0.flip);
        float var4 = Mth.frac(var3 + 0.25F) * 1.6F - 0.3F;
        float var5 = Mth.frac(var3 + 0.75F) * 1.6F - 0.3F;
        float var6 = Mth.lerp(param4, param0.oOpen, param0.open);
        this.bookModel.setupAnim(var0, Mth.clamp(var4, 0.0F, 1.0F), Mth.clamp(var5, 0.0F, 1.0F), var6);
        this.bookModel.render(param7, 0.0625F, param8, param9, this.getSprite(BOOK_LOCATION));
        param7.popPose();
    }
}
