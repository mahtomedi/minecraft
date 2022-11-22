package net.minecraft.client.renderer.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RabbitRenderer extends MobRenderer<Rabbit, RabbitModel<Rabbit>> {
    private static final ResourceLocation RABBIT_BROWN_LOCATION = new ResourceLocation("textures/entity/rabbit/brown.png");
    private static final ResourceLocation RABBIT_WHITE_LOCATION = new ResourceLocation("textures/entity/rabbit/white.png");
    private static final ResourceLocation RABBIT_BLACK_LOCATION = new ResourceLocation("textures/entity/rabbit/black.png");
    private static final ResourceLocation RABBIT_GOLD_LOCATION = new ResourceLocation("textures/entity/rabbit/gold.png");
    private static final ResourceLocation RABBIT_SALT_LOCATION = new ResourceLocation("textures/entity/rabbit/salt.png");
    private static final ResourceLocation RABBIT_WHITE_SPLOTCHED_LOCATION = new ResourceLocation("textures/entity/rabbit/white_splotched.png");
    private static final ResourceLocation RABBIT_TOAST_LOCATION = new ResourceLocation("textures/entity/rabbit/toast.png");
    private static final ResourceLocation RABBIT_EVIL_LOCATION = new ResourceLocation("textures/entity/rabbit/caerbannog.png");

    public RabbitRenderer(EntityRendererProvider.Context param0) {
        super(param0, new RabbitModel<>(param0.bakeLayer(ModelLayers.RABBIT)), 0.3F);
    }

    public ResourceLocation getTextureLocation(Rabbit param0) {
        String var0 = ChatFormatting.stripFormatting(param0.getName().getString());
        if ("Toast".equals(var0)) {
            return RABBIT_TOAST_LOCATION;
        } else {
            return switch(param0.getVariant()) {
                case BROWN -> RABBIT_BROWN_LOCATION;
                case WHITE -> RABBIT_WHITE_LOCATION;
                case BLACK -> RABBIT_BLACK_LOCATION;
                case GOLD -> RABBIT_GOLD_LOCATION;
                case SALT -> RABBIT_SALT_LOCATION;
                case WHITE_SPLOTCHED -> RABBIT_WHITE_SPLOTCHED_LOCATION;
                case EVIL -> RABBIT_EVIL_LOCATION;
            };
        }
    }
}
