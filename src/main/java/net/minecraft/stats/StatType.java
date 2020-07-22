package net.minecraft.stats;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StatType<T> implements Iterable<Stat<T>> {
    private final Registry<T> registry;
    private final Map<T, Stat<T>> map = new IdentityHashMap<>();
    @Nullable
    @OnlyIn(Dist.CLIENT)
    private Component displayName;

    public StatType(Registry<T> param0) {
        this.registry = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean contains(T param0) {
        return this.map.containsKey(param0);
    }

    public Stat<T> get(T param0, StatFormatter param1) {
        return this.map.computeIfAbsent(param0, param1x -> new Stat<>(this, param1x, param1));
    }

    public Registry<T> getRegistry() {
        return this.registry;
    }

    @Override
    public Iterator<Stat<T>> iterator() {
        return this.map.values().iterator();
    }

    public Stat<T> get(T param0) {
        return this.get(param0, StatFormatter.DEFAULT);
    }

    @OnlyIn(Dist.CLIENT)
    public String getTranslationKey() {
        return "stat_type." + Registry.STAT_TYPE.getKey(this).toString().replace(':', '.');
    }

    @OnlyIn(Dist.CLIENT)
    public Component getDisplayName() {
        if (this.displayName == null) {
            this.displayName = new TranslatableComponent(this.getTranslationKey());
        }

        return this.displayName;
    }
}
