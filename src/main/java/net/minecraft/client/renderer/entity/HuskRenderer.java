package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HuskRenderer extends ZombieRenderer {
    private static final ResourceLocation HUSK_LOCATION = new ResourceLocation("textures/entity/zombie/husk.png");

    public HuskRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    protected void scale(Zombie param0, float param1) {
        float var0 = 1.0625F;
        RenderSystem.scalef(1.0625F, 1.0625F, 1.0625F);
        super.scale(param0, param1);
    }

    @Override
    protected ResourceLocation getTextureLocation(Zombie param0) {
        return HUSK_LOCATION;
    }
}
