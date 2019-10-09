package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class BlockStateProviderType<P extends BlockStateProvider> {
    public static final BlockStateProviderType<SimpleStateProvider> SIMPLE_STATE_PROVIDER = register("simple_state_provider", SimpleStateProvider::new);
    public static final BlockStateProviderType<WeightedStateProvider> WEIGHTED_STATE_PROVIDER = register("weighted_state_provider", WeightedStateProvider::new);
    public static final BlockStateProviderType<PlainFlowerProvider> PLAIN_FLOWER_PROVIDER = register("plain_flower_provider", PlainFlowerProvider::new);
    public static final BlockStateProviderType<ForestFlowerProvider> FOREST_FLOWER_PROVIDER = register("forest_flower_provider", ForestFlowerProvider::new);
    private final Function<Dynamic<?>, P> deserializer;

    private static <P extends BlockStateProvider> BlockStateProviderType<P> register(String param0, Function<Dynamic<?>, P> param1) {
        return Registry.register(Registry.BLOCKSTATE_PROVIDER_TYPES, param0, new BlockStateProviderType<>(param1));
    }

    private BlockStateProviderType(Function<Dynamic<?>, P> param0) {
        this.deserializer = param0;
    }

    public P deserialize(Dynamic<?> param0) {
        return this.deserializer.apply(param0);
    }
}
