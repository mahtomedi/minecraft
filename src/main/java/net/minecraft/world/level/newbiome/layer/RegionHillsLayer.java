package net.minecraft.world.level.newbiome.layer;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset1Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum RegionHillsLayer implements AreaTransformer2, DimensionOffset1Transformer {
    INSTANCE;

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Int2IntMap MUTATIONS = Util.make(new Int2IntOpenHashMap(), param0 -> {
        param0.put(1, 129);
        param0.put(2, 130);
        param0.put(3, 131);
        param0.put(4, 132);
        param0.put(5, 133);
        param0.put(6, 134);
        param0.put(12, 140);
        param0.put(21, 149);
        param0.put(23, 151);
        param0.put(27, 155);
        param0.put(28, 156);
        param0.put(29, 157);
        param0.put(30, 158);
        param0.put(32, 160);
        param0.put(33, 161);
        param0.put(34, 162);
        param0.put(35, 163);
        param0.put(36, 164);
        param0.put(37, 165);
        param0.put(38, 166);
        param0.put(39, 167);
    });

    @Override
    public int applyPixel(Context param0, Area param1, Area param2, int param3, int param4) {
        int var0 = param1.get(this.getParentX(param3 + 1), this.getParentY(param4 + 1));
        int var1 = param2.get(this.getParentX(param3 + 1), this.getParentY(param4 + 1));
        if (var0 > 255) {
            LOGGER.debug("old! {}", var0);
        }

        int var2 = (var1 - 2) % 29;
        if (!Layers.isShallowOcean(var0) && var1 >= 2 && var2 == 1) {
            return MUTATIONS.getOrDefault(var0, var0);
        } else {
            if (param0.nextRandom(3) == 0 || var2 == 0) {
                int var3 = var0;
                if (var0 == 2) {
                    var3 = 17;
                } else if (var0 == 4) {
                    var3 = 18;
                } else if (var0 == 27) {
                    var3 = 28;
                } else if (var0 == 29) {
                    var3 = 1;
                } else if (var0 == 5) {
                    var3 = 19;
                } else if (var0 == 32) {
                    var3 = 33;
                } else if (var0 == 30) {
                    var3 = 31;
                } else if (var0 == 1) {
                    var3 = param0.nextRandom(3) == 0 ? 18 : 4;
                } else if (var0 == 12) {
                    var3 = 13;
                } else if (var0 == 21) {
                    var3 = 22;
                } else if (var0 == 168) {
                    var3 = 169;
                } else if (var0 == 0) {
                    var3 = 24;
                } else if (var0 == 45) {
                    var3 = 48;
                } else if (var0 == 46) {
                    var3 = 49;
                } else if (var0 == 10) {
                    var3 = 50;
                } else if (var0 == 3) {
                    var3 = 34;
                } else if (var0 == 35) {
                    var3 = 36;
                } else if (Layers.isSame(var0, 38)) {
                    var3 = 37;
                } else if ((var0 == 24 || var0 == 48 || var0 == 49 || var0 == 50) && param0.nextRandom(3) == 0) {
                    var3 = param0.nextRandom(2) == 0 ? 1 : 4;
                }

                if (var2 == 0 && var3 != var0) {
                    var3 = MUTATIONS.getOrDefault(var3, var0);
                }

                if (var3 != var0) {
                    int var4 = 0;
                    if (Layers.isSame(param1.get(this.getParentX(param3 + 1), this.getParentY(param4 + 0)), var0)) {
                        ++var4;
                    }

                    if (Layers.isSame(param1.get(this.getParentX(param3 + 2), this.getParentY(param4 + 1)), var0)) {
                        ++var4;
                    }

                    if (Layers.isSame(param1.get(this.getParentX(param3 + 0), this.getParentY(param4 + 1)), var0)) {
                        ++var4;
                    }

                    if (Layers.isSame(param1.get(this.getParentX(param3 + 1), this.getParentY(param4 + 2)), var0)) {
                        ++var4;
                    }

                    if (var4 >= 3) {
                        return var3;
                    }
                }
            }

            return var0;
        }
    }
}
