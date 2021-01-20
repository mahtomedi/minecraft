package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HorseModel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractHorseRenderer<T extends AbstractHorse, M extends HorseModel<T>> extends MobRenderer<T, M> {
    private final float scale;

    public AbstractHorseRenderer(EntityRendererProvider.Context param0, M param1, float param2) {
        super(param0, param1, 0.75F);
        this.scale = param2;
    }

    protected void scale(T param0, PoseStack param1, float param2) {
        param1.scale(this.scale, this.scale, this.scale);
        super.scale(param0, param1, param2);
    }
}
