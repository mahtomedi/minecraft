package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.BlazeModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlazeRenderer extends MobRenderer<Blaze, BlazeModel<Blaze>> {
    private static final ResourceLocation BLAZE_LOCATION = new ResourceLocation("textures/entity/blaze.png");

    public BlazeRenderer(EntityRenderDispatcher param0) {
        super(param0, new BlazeModel<>(), 0.5F);
    }

    protected int getBlockLightLevel(Blaze param0, BlockPos param1) {
        return 15;
    }

    public ResourceLocation getTextureLocation(Blaze param0) {
        return BLAZE_LOCATION;
    }
}
