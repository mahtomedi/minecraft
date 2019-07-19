package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvokerRenderer<T extends SpellcasterIllager> extends IllagerRenderer<T> {
    private static final ResourceLocation EVOKER_ILLAGER = new ResourceLocation("textures/entity/illager/evoker.png");

    public EvokerRenderer(EntityRenderDispatcher param0) {
        super(param0, new IllagerModel<>(0.0F, 0.0F, 64, 64), 0.5F);
        this.addLayer(new ItemInHandLayer<T, IllagerModel<T>>(this) {
            public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
                if (param0.isCastingSpell()) {
                    super.render(param0, param1, param2, param3, param4, param5, param6, param7);
                }

            }
        });
    }

    protected ResourceLocation getTextureLocation(T param0) {
        return EVOKER_ILLAGER;
    }
}
