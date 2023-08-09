package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBannerPatternFunction extends LootItemConditionalFunction {
    private static final Codec<Pair<Holder<BannerPattern>, DyeColor>> PATTERN_CODEC = Codec.mapPair(
            BuiltInRegistries.BANNER_PATTERN.holderByNameCodec().fieldOf("pattern"), DyeColor.CODEC.fieldOf("color")
        )
        .codec();
    public static final Codec<SetBannerPatternFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        PATTERN_CODEC.listOf().fieldOf("patterns").forGetter(param0x -> param0x.patterns),
                        Codec.BOOL.fieldOf("append").forGetter(param0x -> param0x.append)
                    )
                )
                .apply(param0, SetBannerPatternFunction::new)
    );
    private final List<Pair<Holder<BannerPattern>, DyeColor>> patterns;
    private final boolean append;

    SetBannerPatternFunction(List<LootItemCondition> param0, List<Pair<Holder<BannerPattern>, DyeColor>> param1, boolean param2) {
        super(param0);
        this.patterns = param1;
        this.append = param2;
    }

    @Override
    protected ItemStack run(ItemStack param0, LootContext param1) {
        CompoundTag var0 = BlockItem.getBlockEntityData(param0);
        if (var0 == null) {
            var0 = new CompoundTag();
        }

        BannerPattern.Builder var1 = new BannerPattern.Builder();
        this.patterns.forEach(var1::addPattern);
        ListTag var2 = var1.toListTag();
        ListTag var3;
        if (this.append) {
            var3 = var0.getList("Patterns", 10).copy();
            var3.addAll(var2);
        } else {
            var3 = var2;
        }

        var0.put("Patterns", var3);
        BlockItem.setBlockEntityData(param0, BlockEntityType.BANNER, var0);
        return param0;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_BANNER_PATTERN;
    }

    public static SetBannerPatternFunction.Builder setBannerPattern(boolean param0) {
        return new SetBannerPatternFunction.Builder(param0);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<SetBannerPatternFunction.Builder> {
        private final ImmutableList.Builder<Pair<Holder<BannerPattern>, DyeColor>> patterns = ImmutableList.builder();
        private final boolean append;

        Builder(boolean param0) {
            this.append = param0;
        }

        protected SetBannerPatternFunction.Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetBannerPatternFunction(this.getConditions(), this.patterns.build(), this.append);
        }

        public SetBannerPatternFunction.Builder addPattern(ResourceKey<BannerPattern> param0, DyeColor param1) {
            return this.addPattern(BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(param0), param1);
        }

        public SetBannerPatternFunction.Builder addPattern(Holder<BannerPattern> param0, DyeColor param1) {
            this.patterns.add(Pair.of(param0, param1));
            return this;
        }
    }
}
