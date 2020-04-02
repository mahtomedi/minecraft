package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public class TreeConfiguration implements FeatureConfiguration {
    public final BlockStateProvider trunkProvider;
    public final BlockStateProvider leavesProvider;
    public final List<TreeDecorator> decorators;
    public final int baseHeight;
    public transient boolean fromSapling;

    protected TreeConfiguration(BlockStateProvider param0, BlockStateProvider param1, List<TreeDecorator> param2, int param3) {
        this.trunkProvider = param0;
        this.leavesProvider = param1;
        this.decorators = param2;
        this.baseHeight = param3;
    }

    public void setFromSapling() {
        this.fromSapling = true;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("trunk_provider"), this.trunkProvider.serialize(param0))
            .put(param0.createString("leaves_provider"), this.leavesProvider.serialize(param0))
            .put(param0.createString("decorators"), param0.createList(this.decorators.stream().map(param1 -> param1.serialize(param0))))
            .put(param0.createString("base_height"), param0.createInt(this.baseHeight));
        return new Dynamic<>(param0, param0.createMap(var0.build()));
    }

    public static <T> TreeConfiguration deserialize(Dynamic<T> param0) {
        BlockStateProviderType<?> var0 = Registry.BLOCKSTATE_PROVIDER_TYPES
            .get(new ResourceLocation(param0.get("trunk_provider").get("type").asString().orElseThrow(RuntimeException::new)));
        BlockStateProviderType<?> var1 = Registry.BLOCKSTATE_PROVIDER_TYPES
            .get(new ResourceLocation(param0.get("leaves_provider").get("type").asString().orElseThrow(RuntimeException::new)));
        return new TreeConfiguration(
            var0.deserialize(param0.get("trunk_provider").orElseEmptyMap()),
            var1.deserialize(param0.get("leaves_provider").orElseEmptyMap()),
            param0.get("decorators")
                .asList(
                    param0x -> Registry.TREE_DECORATOR_TYPES
                            .get(new ResourceLocation(param0x.get("type").asString().orElseThrow(RuntimeException::new)))
                            .deserialize(param0x)
                ),
            param0.get("base_height").asInt(0)
        );
    }

    public static class TreeConfigurationBuilder {
        public final BlockStateProvider trunkProvider;
        public final BlockStateProvider leavesProvider;
        private List<TreeDecorator> decorators = Lists.newArrayList();
        private int baseHeight = 0;

        public TreeConfigurationBuilder(BlockStateProvider param0, BlockStateProvider param1) {
            this.trunkProvider = param0;
            this.leavesProvider = param1;
        }

        public TreeConfiguration.TreeConfigurationBuilder baseHeight(int param0) {
            this.baseHeight = param0;
            return this;
        }

        public TreeConfiguration build() {
            return new TreeConfiguration(this.trunkProvider, this.leavesProvider, this.decorators, this.baseHeight);
        }
    }
}
