package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndermanRenderer extends MobRenderer<EnderMan, EndermanModel<EnderMan>> {
    private static final ResourceLocation ENDERMAN_LOCATION = new ResourceLocation("textures/entity/enderman/enderman.png");
    private final Random random = new Random();

    public EndermanRenderer(EntityRenderDispatcher param0) {
        super(param0, new EndermanModel<>(0.0F), 0.5F);
        this.addLayer(new EnderEyesLayer<>(this));
        this.addLayer(new CarriedBlockLayer(this));
    }

    public void render(EnderMan param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7) {
        BlockState var0 = param0.getCarriedBlock();
        EndermanModel<EnderMan> var1 = this.getModel();
        var1.carrying = var0 != null;
        var1.creepy = param0.isCreepy();
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public Vec3 getRenderOffset(EnderMan param0, double param1, double param2, double param3, float param4) {
        if (param0.isCreepy()) {
            double var0 = 0.02;
            return new Vec3(this.random.nextGaussian() * 0.02, 0.0, this.random.nextGaussian() * 0.02);
        } else {
            return super.getRenderOffset(param0, param1, param2, param3, param4);
        }
    }

    public ResourceLocation getTextureLocation(EnderMan param0) {
        return ENDERMAN_LOCATION;
    }
}
