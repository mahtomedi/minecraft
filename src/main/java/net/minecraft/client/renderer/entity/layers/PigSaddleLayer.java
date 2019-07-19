package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PigSaddleLayer extends RenderLayer<Pig, PigModel<Pig>> {
    private static final ResourceLocation SADDLE_LOCATION = new ResourceLocation("textures/entity/pig/pig_saddle.png");
    private final PigModel<Pig> model = new PigModel<>(0.5F);

    public PigSaddleLayer(RenderLayerParent<Pig, PigModel<Pig>> param0) {
        super(param0);
    }

    public void render(Pig param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (param0.hasSaddle()) {
            this.bindTexture(SADDLE_LOCATION);
            this.getParentModel().copyPropertiesTo(this.model);
            this.model.render(param0, param1, param2, param4, param5, param6, param7);
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
