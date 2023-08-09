package net.minecraft.world.level.storage.loot.providers.nbt;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public record StorageNbtProvider(ResourceLocation id) implements NbtProvider {
    public static final Codec<StorageNbtProvider> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(ResourceLocation.CODEC.fieldOf("source").forGetter(StorageNbtProvider::id)).apply(param0, StorageNbtProvider::new)
    );

    @Override
    public LootNbtProviderType getType() {
        return NbtProviders.STORAGE;
    }

    @Nullable
    @Override
    public Tag get(LootContext param0) {
        return param0.getLevel().getServer().getCommandStorage().get(this.id);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of();
    }
}
