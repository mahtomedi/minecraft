package net.minecraft.client.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.monster.Giant;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GiantZombieModel extends AbstractZombieModel<Giant> {
    public GiantZombieModel() {
        this(0.0F, false);
    }

    public GiantZombieModel(float param0, boolean param1) {
        super(RenderType::entitySolid, param0, 0.0F, 64, param1 ? 32 : 64);
    }

    public boolean isAggressive(Giant param0) {
        return false;
    }
}
