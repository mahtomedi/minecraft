package net.minecraft.data.models;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModelProvider implements DataProvider {
    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider modelPathProvider;

    public ModelProvider(PackOutput param0) {
        this.blockStatePathProvider = param0.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.modelPathProvider = param0.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        Map<Block, BlockStateGenerator> var0 = Maps.newHashMap();
        Consumer<BlockStateGenerator> var1 = param1 -> {
            Block var0x = param1.getBlock();
            BlockStateGenerator var1x = var0.put(var0x, param1);
            if (var1x != null) {
                throw new IllegalStateException("Duplicate blockstate definition for " + var0x);
            }
        };
        Map<ResourceLocation, Supplier<JsonElement>> var2 = Maps.newHashMap();
        Set<Item> var3 = Sets.newHashSet();
        BiConsumer<ResourceLocation, Supplier<JsonElement>> var4 = (param1, param2) -> {
            Supplier<JsonElement> var0x = var2.put(param1, param2);
            if (var0x != null) {
                throw new IllegalStateException("Duplicate model definition for " + param1);
            }
        };
        Consumer<Item> var5 = var3::add;
        new BlockModelGenerators(var1, var4, var5).run();
        new ItemModelGenerators(var4).run();
        List<Block> var6 = BuiltInRegistries.BLOCK
            .entrySet()
            .stream()
            .filter(param0x -> true)
            .map(Entry::getValue)
            .filter(param1 -> !var0.containsKey(param1))
            .toList();
        if (!var6.isEmpty()) {
            throw new IllegalStateException("Missing blockstate definitions for: " + var6);
        } else {
            BuiltInRegistries.BLOCK.forEach(param2 -> {
                Item var0x = Item.BY_BLOCK.get(param2);
                if (var0x != null) {
                    if (var3.contains(var0x)) {
                        return;
                    }

                    ResourceLocation var1x = ModelLocationUtils.getModelLocation(var0x);
                    if (!var2.containsKey(var1x)) {
                        var2.put(var1x, new DelegatedModel(ModelLocationUtils.getModelLocation(param2)));
                    }
                }

            });
            return CompletableFuture.allOf(
                this.saveCollection(param0, var0, param0x -> this.blockStatePathProvider.json(param0x.builtInRegistryHolder().key().location())),
                this.saveCollection(param0, var2, this.modelPathProvider::json)
            );
        }
    }

    private <T> CompletableFuture<?> saveCollection(CachedOutput param0, Map<T, ? extends Supplier<JsonElement>> param1, Function<T, Path> param2) {
        return CompletableFuture.allOf(param1.entrySet().stream().map(param2x -> {
            Path var0 = param2.apply(param2x.getKey());
            JsonElement var1x = param2x.getValue().get();
            return DataProvider.saveStable(param0, var1x, var0);
        }).toArray(param0x -> new CompletableFuture[param0x]));
    }

    @Override
    public final String getName() {
        return "Model Definitions";
    }
}
