package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public interface LootContextUser {
    default Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of();
    }

    default void validate(
        LootTableProblemCollector param0, Function<ResourceLocation, LootTable> param1, Set<ResourceLocation> param2, LootContextParamSet param3
    ) {
        param3.validateUser(param0, this);
    }
}
