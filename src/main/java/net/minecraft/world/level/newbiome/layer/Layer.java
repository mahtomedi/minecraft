package net.minecraft.world.level.newbiome.layer;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
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

    private Biome getBiome(int param0) {
        Biome var0 = BuiltinRegistries.BIOME.byId(param0);
        if (var0 == null) {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Unknown biome id: " + param0));
            } else {
                LOGGER.warn("Unknown biome id: ", param0);
                return Biomes.DEFAULT;
            }
        } else {
            return var0;
        }
    }

    public Biome get(int param0, int param1) {
        return this.getBiome(this.area.get(param0, param1));
    }
}
