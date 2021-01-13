package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantTableRenderer extends BlockEntityRenderer<EnchantmentTableBlockEntity> {
    public static final Material BOOK_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/enchanting_table_book"));
    private final BookModel bookModel = new BookModel();

    public EnchantTableRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(EnchantmentTableBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        param2.pushPose();
        param2.translate(0.5, 0.75, 0.5);
        float var0 = (float)param0.time + param1;
        param2.translate(0.0, (double)(0.1F + Mth.sin(var0 * 0.1F) * 0.01F), 0.0);
        float var1 = param0.rot - param0.oRot;

        while(var1 >= (float) Math.PI) {
            var1 -= (float) (Math.PI * 2);
        }

        while(var1 < (float) -Math.PI) {
            var1 += (float) (Math.PI * 2);
        }

        float var2 = param0.oRot + var1 * param1;
        param2.mulPose(Vector3f.YP.rotation(-var2));
        param2.mulPose(Vector3f.ZP.rotationDegrees(80.0F));
        float var3 = Mth.lerp(param1, param0.oFlip, param0.flip);
        float var4 = Mth.frac(var3 + 0.25F) * 1.6F - 0.3F;
        float var5 = Mth.frac(var3 + 0.75F) * 1.6F - 0.3F;
        float var6 = Mth.lerp(param1, param0.oOpen, param0.open);
        this.bookModel.setupAnim(var0, Mth.clamp(var4, 0.0F, 1.0F), Mth.clamp(var5, 0.0F, 1.0F), var6);
        VertexConsumer var7 = BOOK_LOCATION.buffer(param3, RenderType::entitySolid);
        this.bookModel.render(param2, var7, param4, param5, 1.0F, 1.0F, 1.0F, 1.0F);
        param2.popPose();
    }
}
