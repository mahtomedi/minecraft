package net.minecraft.client.renderer.entity;

import java.util.Random;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
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

    public void render(EnderMan param0, double param1, double param2, double param3, float param4, float param5) {
        BlockState var0 = param0.getCarriedBlock();
        EndermanModel<EnderMan> var1 = this.getModel();
        var1.carrying = var0 != null;
        var1.creepy = param0.isCreepy();
        if (param0.isCreepy()) {
            double var2 = 0.02;
            param1 += this.random.nextGaussian() * 0.02;
            param3 += this.random.nextGaussian() * 0.02;
        }

        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected ResourceLocation getTextureLocation(EnderMan param0) {
        return ENDERMAN_LOCATION;
    }
}
