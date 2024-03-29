package net.minecraft.server.packs.repository;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.server.packs.PackResources;
import net.minecraft.world.flag.FeatureFlagSet;

public class PackRepository {
    private final Set<RepositorySource> sources;
    private Map<String, Pack> available = ImmutableMap.of();
    private List<Pack> selected = ImmutableList.of();

    public PackRepository(RepositorySource... param0) {
        this.sources = ImmutableSet.copyOf(param0);
    }

    public void reload() {
        List<String> var0 = this.selected.stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
        this.available = this.discoverAvailable();
        this.selected = this.rebuildSelected(var0);
    }

    private Map<String, Pack> discoverAvailable() {
        Map<String, Pack> var0 = Maps.newTreeMap();

        for(RepositorySource var1 : this.sources) {
            var1.loadPacks(param1 -> var0.put(param1.getId(), param1));
        }

        return ImmutableMap.copyOf(var0);
    }

    public void setSelected(Collection<String> param0) {
        this.selected = this.rebuildSelected(param0);
    }

    public boolean addPack(String param0) {
        Pack var0 = this.available.get(param0);
        if (var0 != null && !this.selected.contains(var0)) {
            List<Pack> var1 = Lists.newArrayList(this.selected);
            var1.add(var0);
            this.selected = var1;
            return true;
        } else {
            return false;
        }
    }

    public boolean removePack(String param0) {
        Pack var0 = this.available.get(param0);
        if (var0 != null && this.selected.contains(var0)) {
            List<Pack> var1 = Lists.newArrayList(this.selected);
            var1.remove(var0);
            this.selected = var1;
            return true;
        } else {
            return false;
        }
    }

    private List<Pack> rebuildSelected(Collection<String> param0) {
        List<Pack> var0 = this.getAvailablePacks(param0).collect(Collectors.toList());

        for(Pack var1 : this.available.values()) {
            if (var1.isRequired() && !var0.contains(var1)) {
                var1.getDefaultPosition().insert(var0, var1, Functions.identity(), false);
            }
        }

        return ImmutableList.copyOf(var0);
    }

    private Stream<Pack> getAvailablePacks(Collection<String> param0) {
        return param0.stream().map(this.available::get).filter(Objects::nonNull);
    }

    public Collection<String> getAvailableIds() {
        return this.available.keySet();
    }

    public Collection<Pack> getAvailablePacks() {
        return this.available.values();
    }

    public Collection<String> getSelectedIds() {
        return this.selected.stream().map(Pack::getId).collect(ImmutableSet.toImmutableSet());
    }

    public FeatureFlagSet getRequestedFeatureFlags() {
        return this.getSelectedPacks().stream().map(Pack::getRequestedFeatures).reduce(FeatureFlagSet::join).orElse(FeatureFlagSet.of());
    }

    public Collection<Pack> getSelectedPacks() {
        return this.selected;
    }

    @Nullable
    public Pack getPack(String param0) {
        return this.available.get(param0);
    }

    public boolean isAvailable(String param0) {
        return this.available.containsKey(param0);
    }

    public List<PackResources> openAllSelected() {
        return this.selected.stream().map(Pack::open).collect(ImmutableList.toImmutableList());
    }
}
