package net.minecraft.world.level.storage.loot;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface LootDataResolver {
    @Nullable
    <T> T getElement(LootDataId<T> var1);

    @Nullable
    default <T> T getElement(LootDataType<T> param0, ResourceLocation param1) {
        return this.getElement(new LootDataId<>(param0, param1));
    }

    default <T> Optional<T> getElementOptional(LootDataId<T> param0) {
        return Optional.ofNullable(this.getElement(param0));
    }

    default <T> Optional<T> getElementOptional(LootDataType<T> param0, ResourceLocation param1) {
        return this.getElementOptional(new LootDataId<>(param0, param1));
    }

    default LootTable getLootTable(ResourceLocation param0) {
        return this.getElementOptional(LootDataType.TABLE, param0).orElse(LootTable.EMPTY);
    }
}
