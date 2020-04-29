package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WolfRenderer extends MobRenderer<Wolf, WolfModel<Wolf>> {
    private static final ResourceLocation WOLF_LOCATION = new ResourceLocation("textures/entity/wolf/wolf.png");
    private static final ResourceLocation WOLF_TAME_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_tame.png");
    private static final ResourceLocation WOLF_ANGRY_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_angry.png");

    public WolfRenderer(EntityRenderDispatcher param0) {
        super(param0, new WolfModel<>(), 0.5F);
        this.addLayer(new WolfCollarLayer(this));
    }

    protected float getBob(Wolf param0, float param1) {
        return param0.getTailAngle();
    }

    public void render(Wolf param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        if (param0.isWet()) {
            float var0 = Mth.clamp(param0.getBrightness() * param0.getWetShade(param2), 0.0F, 1.0F);
            this.model.setColor(var0, var0, var0);
        }

        super.render(param0, param1, param2, param3, param4, param5);
        if (param0.isWet()) {
            this.model.setColor(1.0F, 1.0F, 1.0F);
        }

    }

    public ResourceLocation getTextureLocation(Wolf param0) {
        if (param0.isTame()) {
            return WOLF_TAME_LOCATION;
        } else {
            return param0.isAngry() ? WOLF_ANGRY_LOCATION : WOLF_LOCATION;
        }
    }
}
