package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitchRenderer extends MobRenderer<Witch, WitchModel<Witch>> {
    private static final ResourceLocation WITCH_LOCATION = new ResourceLocation("textures/entity/witch.png");

    public WitchRenderer(EntityRenderDispatcher param0) {
        super(param0, new WitchModel<>(0.0F), 0.5F);
        this.addLayer(new WitchItemLayer<>(this));
    }

    public void render(Witch param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        this.model.setHoldingItem(!param0.getMainHandItem().isEmpty());
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public ResourceLocation getTextureLocation(Witch param0) {
        return WITCH_LOCATION;
    }

    protected void scale(Witch param0, PoseStack param1, float param2) {
        float var0 = 0.9375F;
        param1.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
