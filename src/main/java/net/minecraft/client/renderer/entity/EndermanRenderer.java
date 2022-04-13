package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndermanRenderer extends MobRenderer<EnderMan, EndermanModel<EnderMan>> {
    private static final ResourceLocation ENDERMAN_LOCATION = new ResourceLocation("textures/entity/enderman/enderman.png");
    private final RandomSource random = RandomSource.create();

    public EndermanRenderer(EntityRendererProvider.Context param0) {
        super(param0, new EndermanModel<>(param0.bakeLayer(ModelLayers.ENDERMAN)), 0.5F);
        this.addLayer(new EnderEyesLayer<>(this));
        this.addLayer(new CarriedBlockLayer(this, param0.getBlockRenderDispatcher()));
    }

    public void render(EnderMan param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        BlockState var0 = param0.getCarriedBlock();
        EndermanModel<EnderMan> var1 = this.getModel();
        var1.carrying = var0 != null;
        var1.creepy = param0.isCreepy();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public Vec3 getRenderOffset(EnderMan param0, float param1) {
        if (param0.isCreepy()) {
            double var0 = 0.02;
            return new Vec3(this.random.nextGaussian() * 0.02, 0.0, this.random.nextGaussian() * 0.02);
        } else {
            return super.getRenderOffset(param0, param1);
        }
    }

    public ResourceLocation getTextureLocation(EnderMan param0) {
        return ENDERMAN_LOCATION;
    }
}
