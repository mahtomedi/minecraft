package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.SquidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SquidRenderer extends MobRenderer<Squid, SquidModel<Squid>> {
    private static final ResourceLocation SQUID_LOCATION = new ResourceLocation("textures/entity/squid.png");

    public SquidRenderer(EntityRenderDispatcher param0) {
        super(param0, new SquidModel<>(), 0.7F);
    }

    protected ResourceLocation getTextureLocation(Squid param0) {
        return SQUID_LOCATION;
    }

    protected void setupRotations(Squid param0, float param1, float param2, float param3) {
        float var0 = Mth.lerp(param3, param0.xBodyRotO, param0.xBodyRot);
        float var1 = Mth.lerp(param3, param0.zBodyRotO, param0.zBodyRot);
        GlStateManager.translatef(0.0F, 0.5F, 0.0F);
        GlStateManager.rotatef(180.0F - param2, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(var0, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(var1, 0.0F, 1.0F, 0.0F);
        GlStateManager.translatef(0.0F, -1.2F, 0.0F);
    }

    protected float getBob(Squid param0, float param1) {
        return Mth.lerp(param1, param0.oldTentacleAngle, param0.tentacleAngle);
    }
}
