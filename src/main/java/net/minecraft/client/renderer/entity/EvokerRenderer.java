package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvokerRenderer<T extends SpellcasterIllager> extends IllagerRenderer<T> {
    private static final ResourceLocation EVOKER_ILLAGER = new ResourceLocation("textures/entity/illager/evoker.png");

    public EvokerRenderer(EntityRendererProvider.Context param0) {
        super(param0, new IllagerModel<>(param0.bakeLayer(ModelLayers.EVOKER)), 0.5F);
        this.addLayer(
            new ItemInHandLayer<T, IllagerModel<T>>(this) {
                public void render(
                    PoseStack param0,
                    MultiBufferSource param1,
                    int param2,
                    T param3,
                    float param4,
                    float param5,
                    float param6,
                    float param7,
                    float param8,
                    float param9
                ) {
                    if (param3.isCastingSpell()) {
                        super.render(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
                    }
    
                }
            }
        );
        this.addLayer(new CarriedBlockLayer<>(this, -0.17500001F, 0.25F, 0.5F));
    }

    public ResourceLocation getTextureLocation(T param0) {
        return EVOKER_ILLAGER;
    }
}
