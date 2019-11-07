package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VindicatorRenderer extends IllagerRenderer<Vindicator> {
    private static final ResourceLocation VINDICATOR = new ResourceLocation("textures/entity/illager/vindicator.png");

    public VindicatorRenderer(EntityRenderDispatcher param0) {
        super(param0, new IllagerModel<>(0.0F, 0.0F, 64, 64), 0.5F);
        this.addLayer(
            new ItemInHandLayer<Vindicator, IllagerModel<Vindicator>>(this) {
                public void render(
                    PoseStack param0,
                    MultiBufferSource param1,
                    int param2,
                    Vindicator param3,
                    float param4,
                    float param5,
                    float param6,
                    float param7,
                    float param8,
                    float param9
                ) {
                    if (param3.isAggressive()) {
                        super.render(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
                    }
    
                }
            }
        );
    }

    public ResourceLocation getTextureLocation(Vindicator param0) {
        return VINDICATOR;
    }
}
