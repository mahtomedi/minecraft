package net.minecraft.server.packs.repository;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class PackRepository<T extends UnopenedPack> implements AutoCloseable {
    private final Set<RepositorySource> sources = Sets.newHashSet();
    private final Map<String, T> available = Maps.newLinkedHashMap();
    private final List<T> selected = Lists.newLinkedList();
    private final UnopenedPack.UnopenedPackConstructor<T> constructor;

    public PackRepository(UnopenedPack.UnopenedPackConstructor<T> param0) {
        this.constructor = param0;
    }

    public void reload() {
        this.close();
        Set<String> var0 = this.selected.stream().map(UnopenedPack::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        this.available.clear();
        this.selected.clear();

        for(RepositorySource var1 : this.sources) {
            var1.loadPacks(this.available, this.constructor);
        }

        this.sortAvailable();
        this.selected.addAll(var0.stream().map(this.available::get).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new)));

        for(T var2 : this.available.values()) {
            if (var2.isRequired() && !this.selected.contains(var2)) {
                var2.getDefaultPosition().insert(this.selected, var2, Functions.identity(), false);
            }
        }

    }

    private void sortAvailable() {
        List<Entry<String, T>> var0 = Lists.newArrayList(this.available.entrySet());
        this.available.clear();
        var0.stream().sorted(Entry.comparingByKey()).forEachOrdered(param0 -> param0.getKey());
    }

    public void setSelected(Collection<T> param0) {
        this.selected.clear();
        this.selected.addAll(param0);

        for(T var0 : this.available.values()) {
            if (var0.isRequired() && !this.selected.contains(var0)) {
                var0.getDefaultPosition().insert(this.selected, var0, Functions.identity(), false);
            }
        }

    }

    public Collection<T> getAvailable() {
        return this.available.values();
    }

    public Collection<T> getUnselected() {
        Collection<T> var0 = Lists.newArrayList(this.available.values());
        var0.removeAll(this.selected);
        return var0;
    }

    public Collection<T> getSelected() {
        return this.selected;
    }

    @Nullable
    public T getPack(String param0) {
        return this.available.get(param0);
    }

    public void addSource(RepositorySource param0) {
        this.sources.add(param0);
    }

    @Override
    public void close() {
        this.available.values().forEach(UnopenedPack::close);
    }
}
