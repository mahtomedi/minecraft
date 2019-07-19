package net.minecraft.world.level.newbiome.layer;

import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
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

    public Biome[] getArea(int param0, int param1, int param2, int param3) {
        Biome[] var0 = new Biome[param2 * param3];

        for(int var1 = 0; var1 < param3; ++var1) {
            for(int var2 = 0; var2 < param2; ++var2) {
                int var3 = this.area.get(param0 + var2, param1 + var1);
                Biome var4 = this.getBiome(var3);
                var0[var2 + var1 * param2] = var4;
            }
        }

        return var0;
    }

    private Biome getBiome(int param0) {
        Biome var0 = Registry.BIOME.byId(param0);
        if (var0 == null) {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                throw new IllegalStateException("Unknown biome id: " + param0);
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
