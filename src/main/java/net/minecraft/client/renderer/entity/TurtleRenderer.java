package net.minecraft.client.renderer.entity;

import javax.annotation.Nullable;
import net.minecraft.client.model.TurtleModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TurtleRenderer extends MobRenderer<Turtle, TurtleModel<Turtle>> {
    private static final ResourceLocation TURTLE_LOCATION = new ResourceLocation("textures/entity/turtle/big_sea_turtle.png");

    public TurtleRenderer(EntityRenderDispatcher param0) {
        super(param0, new TurtleModel<>(0.0F), 0.7F);
    }

    public void render(Turtle param0, double param1, double param2, double param3, float param4, float param5) {
        if (param0.isBaby()) {
            this.shadowRadius *= 0.5F;
        }

        super.render(param0, param1, param2, param3, param4, param5);
    }

    @Nullable
    protected ResourceLocation getTextureLocation(Turtle param0) {
        return TURTLE_LOCATION;
    }
}
