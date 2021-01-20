package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TntRenderer extends EntityRenderer<PrimedTnt> {
    public TntRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.shadowRadius = 0.5F;
    }

    public void render(PrimedTnt param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        param3.translate(0.0, 0.5, 0.0);
        int var0 = param0.getFuse();
        if ((float)var0 - param2 + 1.0F < 10.0F) {
            float var1 = 1.0F - ((float)var0 - param2 + 1.0F) / 10.0F;
            var1 = Mth.clamp(var1, 0.0F, 1.0F);
            var1 *= var1;
            var1 *= var1;
            float var2 = 1.0F + var1 * 0.3F;
            param3.scale(var2, var2, var2);
        }

        param3.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
        param3.translate(-0.5, -0.5, 0.5);
        param3.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        TntMinecartRenderer.renderWhiteSolidBlock(Blocks.TNT.defaultBlockState(), param3, param4, param5, var0 / 5 % 2 == 0);
        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public ResourceLocation getTextureLocation(PrimedTnt param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
