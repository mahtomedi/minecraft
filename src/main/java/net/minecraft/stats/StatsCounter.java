package net.minecraft.stats;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StatsCounter {
    protected final Object2IntMap<Stat<?>> stats = Object2IntMaps.synchronize(new Object2IntOpenHashMap<>());

    public StatsCounter() {
        this.stats.defaultReturnValue(0);
    }

    public void increment(Player param0, Stat<?> param1, int param2) {
        this.setValue(param0, param1, this.getValue(param1) + param2);
    }

    public void setValue(Player param0, Stat<?> param1, int param2) {
        this.stats.put(param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    public <T> int getValue(StatType<T> param0, T param1) {
        return param0.contains(param1) ? this.getValue(param0.get(param1)) : 0;
    }

    public int getValue(Stat<?> param0) {
        return this.stats.getInt(param0);
    }
}
