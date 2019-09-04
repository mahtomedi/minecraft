package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WolfCollarLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
    private static final ResourceLocation WOLF_COLLAR_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_collar.png");

    public WolfCollarLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> param0) {
        super(param0);
    }

    public void render(Wolf param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (param0.isTame() && !param0.isInvisible()) {
            this.bindTexture(WOLF_COLLAR_LOCATION);
            float[] var0 = param0.getCollarColor().getTextureDiffuseColors();
            RenderSystem.color3f(var0[0], var0[1], var0[2]);
            this.getParentModel().render(param0, param1, param2, param4, param5, param6, param7);
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}
