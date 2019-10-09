package net.minecraft.client.model;

import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieModel<T extends Zombie> extends AbstractZombieModel<T> {
    public ZombieModel(Function<ResourceLocation, RenderType> param0, float param1, boolean param2) {
        this(param0, param1, 0.0F, 64, param2 ? 32 : 64);
    }

    protected ZombieModel(Function<ResourceLocation, RenderType> param0, float param1, float param2, int param3, int param4) {
        super(param0, param1, param2, param3, param4);
    }

    public boolean isAggressive(T param0) {
        return param0.isAggressive();
    }
}
