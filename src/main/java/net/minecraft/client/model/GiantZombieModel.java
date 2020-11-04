package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Giant;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GiantZombieModel extends AbstractZombieModel<Giant> {
    public GiantZombieModel(ModelPart param0) {
        super(param0);
    }

    public boolean isAggressive(Giant param0) {
        return false;
    }
}
