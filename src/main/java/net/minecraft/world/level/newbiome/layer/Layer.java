package net.minecraft.world.level.newbiome.layer;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.area.LazyArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Layer {
    private static final Logger LOGGER = LogManager.getLogger();
    private final LazyArea area;

    public Layer(AreaFactory<LazyArea> param0) {
        this.area = param0.make();
    }

    public Biome get(Registry<Biome> param0, int param1, int param2) {
        int var0 = this.area.get(param1, param2);
        ResourceKey<Biome> var1 = Biomes.byId(var0);
        if (var1 == null) {
            throw new IllegalStateException("Unknown biome id emitted by layers: " + var0);
        } else {
            Biome var2 = param0.get(var1);
            if (var2 == null) {
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Unknown biome id: " + var0));
                } else {
                    LOGGER.warn("Unknown biome id: {}", var0);
                    return param0.get(Biomes.byId(0));
                }
            } else {
                return var2;
            }
        }
    }
}
