package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public record BlockPredicate(
    Optional<TagKey<Block>> tag, Optional<HolderSet<Block>> blocks, Optional<StatePropertiesPredicate> properties, Optional<NbtPredicate> nbt
) {
    private static final Codec<HolderSet<Block>> BLOCKS_CODEC = BuiltInRegistries.BLOCK
        .holderByNameCodec()
        .listOf()
        .xmap(HolderSet::direct, param0 -> param0.stream().toList());
    public static final Codec<BlockPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(TagKey.codec(Registries.BLOCK), "tag").forGetter(BlockPredicate::tag),
                    ExtraCodecs.strictOptionalField(BLOCKS_CODEC, "blocks").forGetter(BlockPredicate::blocks),
                    ExtraCodecs.strictOptionalField(StatePropertiesPredicate.CODEC, "state").forGetter(BlockPredicate::properties),
                    ExtraCodecs.strictOptionalField(NbtPredicate.CODEC, "nbt").forGetter(BlockPredicate::nbt)
                )
                .apply(param0, BlockPredicate::new)
    );

    static Optional<BlockPredicate> of(
        Optional<TagKey<Block>> param0, Optional<HolderSet<Block>> param1, Optional<StatePropertiesPredicate> param2, Optional<NbtPredicate> param3
    ) {
        return param0.isEmpty() && param1.isEmpty() && param2.isEmpty() && param3.isEmpty()
            ? Optional.empty()
            : Optional.of(new BlockPredicate(param0, param1, param2, param3));
    }

    public boolean matches(ServerLevel param0, BlockPos param1) {
        if (!param0.isLoaded(param1)) {
            return false;
        } else {
            BlockState var0 = param0.getBlockState(param1);
            if (this.tag.isPresent() && !var0.is(this.tag.get())) {
                return false;
            } else if (this.blocks.isPresent() && !var0.is(this.blocks.get())) {
                return false;
            } else if (this.properties.isPresent() && !this.properties.get().matches(var0)) {
                return false;
            } else {
                if (this.nbt.isPresent()) {
                    BlockEntity var1 = param0.getBlockEntity(param1);
                    if (var1 == null || !this.nbt.get().matches(var1.saveWithFullMetadata())) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public static class Builder {
        private Optional<HolderSet<Block>> blocks = Optional.empty();
        private Optional<TagKey<Block>> tag = Optional.empty();
        private Optional<StatePropertiesPredicate> properties = Optional.empty();
        private Optional<NbtPredicate> nbt = Optional.empty();

        private Builder() {
        }

        public static BlockPredicate.Builder block() {
            return new BlockPredicate.Builder();
        }

        public BlockPredicate.Builder of(Block... param0) {
            this.blocks = Optional.of(HolderSet.direct(Block::builtInRegistryHolder, param0));
            return this;
        }

        public BlockPredicate.Builder of(Collection<Block> param0) {
            this.blocks = Optional.of(HolderSet.direct(Block::builtInRegistryHolder, param0));
            return this;
        }

        public BlockPredicate.Builder of(TagKey<Block> param0) {
            this.tag = Optional.of(param0);
            return this;
        }

        public BlockPredicate.Builder hasNbt(CompoundTag param0) {
            this.nbt = Optional.of(new NbtPredicate(param0));
            return this;
        }

        public BlockPredicate.Builder setProperties(StatePropertiesPredicate.Builder param0) {
            this.properties = param0.build();
            return this;
        }

        public Optional<BlockPredicate> build() {
            return BlockPredicate.of(this.tag, this.blocks, this.properties, this.nbt);
        }
    }
}
