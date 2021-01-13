package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
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

    protected void scale(Zombie param0, PoseStack param1, float param2) {
        float var0 = 1.0625F;
        param1.scale(1.0625F, 1.0625F, 1.0625F);
        super.scale(param0, param1, param2);
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie param0) {
        return HUSK_LOCATION;
    }
}
