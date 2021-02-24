package net.minecraft.core.particles;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleGroup {
    private final int limit;
    public static final ParticleGroup SPORE_BLOSSOM = new ParticleGroup(1000);

    public ParticleGroup(int param0) {
        this.limit = param0;
    }

    public int getLimit() {
        return this.limit;
    }
}
