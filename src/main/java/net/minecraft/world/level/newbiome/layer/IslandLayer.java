package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer0;

public enum IslandLayer implements AreaTransformer0 {
    INSTANCE;

    @Override
    public int applyPixel(Context param0, int param1, int param2) {
        if (param1 == 0 && param2 == 0) {
            return 1;
        } else {
            return param0.nextRandom(10) == 0 ? 1 : 0;
        }
    }
}
