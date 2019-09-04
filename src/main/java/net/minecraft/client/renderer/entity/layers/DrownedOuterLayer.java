package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DrownedOuterLayer<T extends Zombie> extends RenderLayer<T, DrownedModel<T>> {
    private static final ResourceLocation DROWNED_OUTER_LAYER_LOCATION = new ResourceLocation("textures/entity/zombie/drowned_outer_layer.png");
    private final DrownedModel<T> model = new DrownedModel<>(0.25F, 0.0F, 64, 64);

    public DrownedOuterLayer(RenderLayerParent<T, DrownedModel<T>> param0) {
        super(param0);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (!param0.isInvisible()) {
            this.getParentModel().copyPropertiesTo(this.model);
            this.model.prepareMobModel(param0, param1, param2, param3);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.bindTexture(DROWNED_OUTER_LAYER_LOCATION);
            this.model.render(param0, param1, param2, param4, param5, param6, param7);
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}
