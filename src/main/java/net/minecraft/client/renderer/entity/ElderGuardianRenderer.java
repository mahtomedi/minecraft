package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ElderGuardianRenderer extends GuardianRenderer {
    private static final ResourceLocation GUARDIAN_ELDER_LOCATION = new ResourceLocation("textures/entity/guardian_elder.png");

    public ElderGuardianRenderer(EntityRenderDispatcher param0) {
        super(param0, 1.2F);
    }

    protected void scale(Guardian param0, float param1) {
        GlStateManager.scalef(ElderGuardian.ELDER_SIZE_SCALE, ElderGuardian.ELDER_SIZE_SCALE, ElderGuardian.ELDER_SIZE_SCALE);
    }

    @Override
    protected ResourceLocation getTextureLocation(Guardian param0) {
        return GUARDIAN_ELDER_LOCATION;
    }
}
