package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ElderGuardianRenderer extends GuardianRenderer {
    public static final ResourceLocation GUARDIAN_ELDER_LOCATION = new ResourceLocation("textures/entity/guardian_elder.png");

    public ElderGuardianRenderer(EntityRenderDispatcher param0) {
        super(param0, 1.2F);
    }

    protected void scale(Guardian param0, PoseStack param1, float param2) {
        param1.scale(ElderGuardian.ELDER_SIZE_SCALE, ElderGuardian.ELDER_SIZE_SCALE, ElderGuardian.ELDER_SIZE_SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(Guardian param0) {
        return GUARDIAN_ELDER_LOCATION;
    }
}
