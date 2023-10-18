package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public class PoolAliasBindings {
    public static Codec<? extends PoolAliasBinding> bootstrap(Registry<Codec<? extends PoolAliasBinding>> param0) {
        Registry.register(param0, "random", Random.CODEC);
        Registry.register(param0, "random_group", RandomGroup.CODEC);
        return Registry.register(param0, "direct", Direct.CODEC);
    }
}
