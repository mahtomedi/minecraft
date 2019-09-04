package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatCollarLayer extends RenderLayer<Cat, CatModel<Cat>> {
    private static final ResourceLocation CAT_COLLAR_LOCATION = new ResourceLocation("textures/entity/cat/cat_collar.png");
    private final CatModel<Cat> catModel = new CatModel<>(0.01F);

    public CatCollarLayer(RenderLayerParent<Cat, CatModel<Cat>> param0) {
        super(param0);
    }

    public void render(Cat param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (param0.isTame() && !param0.isInvisible()) {
            this.bindTexture(CAT_COLLAR_LOCATION);
            float[] var0 = param0.getCollarColor().getTextureDiffuseColors();
            RenderSystem.color3f(var0[0], var0[1], var0[2]);
            this.getParentModel().copyPropertiesTo(this.catModel);
            this.catModel.prepareMobModel(param0, param1, param2, param3);
            this.catModel.render(param0, param1, param2, param4, param5, param6, param7);
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}
