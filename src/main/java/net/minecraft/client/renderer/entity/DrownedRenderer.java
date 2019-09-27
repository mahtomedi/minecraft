package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DrownedRenderer extends AbstractZombieRenderer<Drowned, DrownedModel<Drowned>> {
    private static final ResourceLocation DROWNED_LOCATION = new ResourceLocation("textures/entity/zombie/drowned.png");

    public DrownedRenderer(EntityRenderDispatcher param0) {
        super(param0, new DrownedModel<>(0.0F, 0.0F, 64, 64), new DrownedModel<>(0.5F, true), new DrownedModel<>(1.0F, true));
        this.addLayer(new DrownedOuterLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie param0) {
        return DROWNED_LOCATION;
    }

    protected void setupRotations(Drowned param0, PoseStack param1, float param2, float param3, float param4) {
        super.setupRotations(param0, param1, param2, param3, param4);
        float var0 = param0.getSwimAmount(param4);
        if (var0 > 0.0F) {
            param1.mulPose(Vector3f.XP.rotation(Mth.lerp(var0, param0.xRot, -10.0F - param0.xRot), true));
        }

    }
}
