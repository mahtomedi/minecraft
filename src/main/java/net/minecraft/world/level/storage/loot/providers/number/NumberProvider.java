package net.minecraft.world.level.storage.loot.providers.number;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

public interface NumberProvider extends LootContextUser {
    float getFloat(LootContext var1);

    default int getInt(LootContext param0) {
        return Math.round(this.getFloat(param0));
    }

    LootNumberProviderType getType();
}
