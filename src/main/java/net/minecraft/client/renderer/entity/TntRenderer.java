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
    public TntRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.shadowRadius = 0.5F;
    }

    public void render(PrimedTnt param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7) {
        param6.pushPose();
        param6.translate(0.0, 0.5, 0.0);
        if ((float)param0.getLife() - param5 + 1.0F < 10.0F) {
            float var0 = 1.0F - ((float)param0.getLife() - param5 + 1.0F) / 10.0F;
            var0 = Mth.clamp(var0, 0.0F, 1.0F);
            var0 *= var0;
            var0 *= var0;
            float var1 = 1.0F + var0 * 0.3F;
            param6.scale(var1, var1, var1);
        }

        int var2 = param0.getLightColor();
        param6.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
        param6.translate(-0.5, -0.5, 0.5);
        TntMinecartRenderer.renderWhiteSolidBlock(Blocks.TNT.defaultBlockState(), param6, param7, var2, param0.getLife() / 5 % 2 == 0);
        param6.popPose();
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public ResourceLocation getTextureLocation(PrimedTnt param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
