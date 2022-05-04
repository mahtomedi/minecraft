package net.minecraft.data.models;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

public class ModelProvider implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DataGenerator generator;

    public ModelProvider(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(CachedOutput param0) {
        Path var0 = this.generator.getOutputFolder();
        Map<Block, BlockStateGenerator> var1 = Maps.newHashMap();
        Consumer<BlockStateGenerator> var2 = param1 -> {
            Block var0x = param1.getBlock();
            BlockStateGenerator var1x = var1.put(var0x, param1);
            if (var1x != null) {
                throw new IllegalStateException("Duplicate blockstate definition for " + var0x);
            }
        };
        Map<ResourceLocation, Supplier<JsonElement>> var3 = Maps.newHashMap();
        Set<Item> var4 = Sets.newHashSet();
        BiConsumer<ResourceLocation, Supplier<JsonElement>> var5 = (param1, param2) -> {
            Supplier<JsonElement> var0x = var3.put(param1, param2);
            if (var0x != null) {
                throw new IllegalStateException("Duplicate model definition for " + param1);
            }
        };
        Consumer<Item> var6 = var4::add;
        new BlockModelGenerators(var2, var5, var6).run();
        new ItemModelGenerators(var5).run();
        List<Block> var7 = Registry.BLOCK.stream().filter(param1 -> !var1.containsKey(param1)).collect(Collectors.toList());
        if (!var7.isEmpty()) {
            throw new IllegalStateException("Missing blockstate definitions for: " + var7);
        } else {
            Registry.BLOCK.forEach(param2 -> {
                Item var0x = Item.BY_BLOCK.get(param2);
                if (var0x != null) {
                    if (var4.contains(var0x)) {
                        return;
                    }

                    ResourceLocation var1x = ModelLocationUtils.getModelLocation(var0x);
                    if (!var3.containsKey(var1x)) {
                        var3.put(var1x, new DelegatedModel(ModelLocationUtils.getModelLocation(param2)));
                    }
                }

            });
            this.saveCollection(param0, var0, var1, ModelProvider::createBlockStatePath);
            this.saveCollection(param0, var0, var3, ModelProvider::createModelPath);
        }
    }

    private <T> void saveCollection(CachedOutput param0, Path param1, Map<T, ? extends Supplier<JsonElement>> param2, BiFunction<Path, T, Path> param3) {
        param2.forEach((param3x, param4) -> {
            Path var0 = param3.apply(param1, param3x);

            try {
                DataProvider.saveStable(param0, param4.get(), var0);
            } catch (Exception var7) {
                LOGGER.error("Couldn't save {}", var0, var7);
            }

        });
    }

    private static Path createBlockStatePath(Path param0x, Block param1) {
        ResourceLocation var0x = Registry.BLOCK.getKey(param1);
        return param0x.resolve("assets/" + var0x.getNamespace() + "/blockstates/" + var0x.getPath() + ".json");
    }

    private static Path createModelPath(Path param0x, ResourceLocation param1) {
        return param0x.resolve("assets/" + param1.getNamespace() + "/models/" + param1.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Block State Definitions";
    }
}
