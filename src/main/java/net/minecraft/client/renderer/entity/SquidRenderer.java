package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SquidRenderer extends MobRenderer<Squid, SquidModel<Squid>> {
    private static final ResourceLocation SQUID_LOCATION = new ResourceLocation("textures/entity/squid.png");

    public SquidRenderer(EntityRendererProvider.Context param0) {
        super(param0, new SquidModel<>(param0.bakeLayer(ModelLayers.SQUID)), 0.7F);
    }

    public ResourceLocation getTextureLocation(Squid param0) {
        return SQUID_LOCATION;
    }

    protected void setupRotations(Squid param0, PoseStack param1, float param2, float param3, float param4) {
        float var0 = Mth.lerp(param4, param0.xBodyRotO, param0.xBodyRot);
        float var1 = Mth.lerp(param4, param0.zBodyRotO, param0.zBodyRot);
        param1.translate(0.0, 0.5, 0.0);
        param1.mulPose(Vector3f.YP.rotationDegrees(180.0F - param3));
        param1.mulPose(Vector3f.XP.rotationDegrees(var0));
        param1.mulPose(Vector3f.YP.rotationDegrees(var1));
        param1.translate(0.0, -1.2F, 0.0);
    }

    protected float getBob(Squid param0, float param1) {
        return Mth.lerp(param1, param0.oldTentacleAngle, param0.tentacleAngle);
    }
}
