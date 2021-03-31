package net.minecraft.stats;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Stat<T> extends ObjectiveCriteria {
    private final StatFormatter formatter;
    private final T value;
    private final StatType<T> type;

    protected Stat(StatType<T> param0, T param1, StatFormatter param2) {
        super(buildName(param0, param1));
        this.type = param0;
        this.formatter = param2;
        this.value = param1;
    }

    public static <T> String buildName(StatType<T> param0, T param1) {
        return locationToKey(Registry.STAT_TYPE.getKey(param0)) + ":" + locationToKey(param0.getRegistry().getKey(param1));
    }

    private static <T> String locationToKey(@Nullable ResourceLocation param0) {
        return param0.toString().replace(':', '.');
    }

    public StatType<T> getType() {
        return this.type;
    }

    public T getValue() {
        return this.value;
    }

    public String format(int param0) {
        return this.formatter.format(param0);
    }

    @Override
    public boolean equals(Object param0) {
        return this == param0 || param0 instanceof Stat && Objects.equals(this.getName(), ((Stat)param0).getName());
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Override
    public String toString() {
        return "Stat{name=" + this.getName() + ", formatter=" + this.formatter + '}';
    }
}
