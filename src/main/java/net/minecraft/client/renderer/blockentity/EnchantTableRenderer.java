package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantTableRenderer extends BlockEntityRenderer<EnchantmentTableBlockEntity> {
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("entity/enchanting_table_book");
    private final BookModel bookModel = new BookModel();

    public EnchantTableRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(
        EnchantmentTableBlockEntity param0, double param1, double param2, double param3, float param4, PoseStack param5, MultiBufferSource param6, int param7
    ) {
        param5.pushPose();
        param5.translate(0.5, 0.75, 0.5);
        float var0 = (float)param0.time + param4;
        param5.translate(0.0, (double)(0.1F + Mth.sin(var0 * 0.1F) * 0.01F), 0.0);
        float var1 = param0.rot - param0.oRot;

        while(var1 >= (float) Math.PI) {
            var1 -= (float) (Math.PI * 2);
        }

        while(var1 < (float) -Math.PI) {
            var1 += (float) (Math.PI * 2);
        }

        float var2 = param0.oRot + var1 * param4;
        param5.mulPose(Vector3f.YP.rotation(-var2, false));
        param5.mulPose(Vector3f.ZP.rotation(80.0F, true));
        float var3 = Mth.lerp(param4, param0.oFlip, param0.flip);
        float var4 = Mth.frac(var3 + 0.25F) * 1.6F - 0.3F;
        float var5 = Mth.frac(var3 + 0.75F) * 1.6F - 0.3F;
        float var6 = Mth.lerp(param4, param0.oOpen, param0.open);
        this.bookModel.setupAnim(var0, Mth.clamp(var4, 0.0F, 1.0F), Mth.clamp(var5, 0.0F, 1.0F), var6);
        VertexConsumer var7 = param6.getBuffer(RenderType.SOLID);
        this.bookModel.render(param5, var7, 0.0625F, param7, this.getSprite(BOOK_LOCATION));
        param5.popPose();
    }
}
