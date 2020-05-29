package net.minecraft.server.packs.repository;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.server.packs.Pack;

public class PackRepository<T extends UnopenedPack> implements AutoCloseable {
    private final Set<RepositorySource> sources;
    private Map<String, T> available = ImmutableMap.of();
    private List<T> selected = ImmutableList.of();
    private final UnopenedPack.UnopenedPackConstructor<T> constructor;

    public PackRepository(UnopenedPack.UnopenedPackConstructor<T> param0, RepositorySource... param1) {
        this.constructor = param0;
        this.sources = ImmutableSet.copyOf(param1);
    }

    public void reload() {
        List<String> var0 = this.selected.stream().map(UnopenedPack::getId).collect(ImmutableList.toImmutableList());
        this.close();
        this.available = this.discoverAvailable();
        this.selected = this.rebuildSelected(var0);
    }

    private Map<String, T> discoverAvailable() {
        Map<String, T> var0 = Maps.newTreeMap();

        for(RepositorySource var1 : this.sources) {
            var1.loadPacks(var0, this.constructor);
        }

        return ImmutableMap.copyOf(var0);
    }

    public void setSelected(Collection<String> param0) {
        this.selected = this.rebuildSelected(param0);
    }

    private List<T> rebuildSelected(Collection<String> param0) {
        List<T> var0 = this.getAvailablePacks(param0).collect(Collectors.toList());

        for(T var1 : this.available.values()) {
            if (var1.isRequired() && !var0.contains(var1)) {
                var1.getDefaultPosition().insert(var0, var1, Functions.identity(), false);
            }
        }

        return ImmutableList.copyOf(var0);
    }

    private Stream<T> getAvailablePacks(Collection<String> param0) {
        return param0.stream().map(this.available::get).filter(Objects::nonNull);
    }

    public Collection<String> getAvailableIds() {
        return this.available.keySet();
    }

    public Collection<T> getAvailablePacks() {
        return this.available.values();
    }

    public Collection<String> getSelectedIds() {
        return this.selected.stream().map(UnopenedPack::getId).collect(ImmutableSet.toImmutableSet());
    }

    public Collection<T> getSelectedPacks() {
        return this.selected;
    }

    @Nullable
    public T getPack(String param0) {
        return this.available.get(param0);
    }

    @Override
    public void close() {
        this.available.values().forEach(UnopenedPack::close);
    }

    public boolean isAvailable(String param0) {
        return this.available.containsKey(param0);
    }

    public List<Pack> openAllSelected() {
        return this.selected.stream().map(UnopenedPack::open).collect(ImmutableList.toImmutableList());
    }
}
