package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyBonusCount extends LootItemConditionalFunction {
    private static final Map<ResourceLocation, ApplyBonusCount.FormulaType> FORMULAS = Stream.of(
            ApplyBonusCount.BinomialWithBonusCount.TYPE, ApplyBonusCount.OreDrops.TYPE, ApplyBonusCount.UniformBonusCount.TYPE
        )
        .collect(Collectors.toMap(ApplyBonusCount.FormulaType::id, Function.identity()));
    private static final Codec<ApplyBonusCount.FormulaType> FORMULA_TYPE_CODEC = ResourceLocation.CODEC.comapFlatMap(param0 -> {
        ApplyBonusCount.FormulaType var0 = FORMULAS.get(param0);
        return var0 != null ? DataResult.success(var0) : DataResult.error(() -> "No formula type with id: '" + param0 + "'");
    }, ApplyBonusCount.FormulaType::id);
    private static final MapCodec<ApplyBonusCount.Formula> FORMULA_CODEC = ExtraCodecs.dispatchOptionalValue(
        "formula", "parameters", FORMULA_TYPE_CODEC, ApplyBonusCount.Formula::getType, ApplyBonusCount.FormulaType::codec
    );
    public static final Codec<ApplyBonusCount> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        BuiltInRegistries.ENCHANTMENT.holderByNameCodec().fieldOf("enchantment").forGetter(param0x -> param0x.enchantment),
                        FORMULA_CODEC.forGetter(param0x -> param0x.formula)
                    )
                )
                .apply(param0, ApplyBonusCount::new)
    );
    private final Holder<Enchantment> enchantment;
    private final ApplyBonusCount.Formula formula;

    private ApplyBonusCount(List<LootItemCondition> param0, Holder<Enchantment> param1, ApplyBonusCount.Formula param2) {
        super(param0);
        this.enchantment = param1;
        this.formula = param2;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.APPLY_BONUS;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        ItemStack var0 = param1.getParamOrNull(LootContextParams.TOOL);
        if (var0 != null) {
            int var1 = EnchantmentHelper.getItemEnchantmentLevel(this.enchantment.value(), var0);
            int var2 = this.formula.calculateNewCount(param1.getRandom(), param0.getCount(), var1);
            param0.setCount(var2);
        }

        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> addBonusBinomialDistributionCount(Enchantment param0, float param1, int param2) {
        return simpleBuilder(param3 -> new ApplyBonusCount(param3, param0.builtInRegistryHolder(), new ApplyBonusCount.BinomialWithBonusCount(param2, param1)));
    }

    public static LootItemConditionalFunction.Builder<?> addOreBonusCount(Enchantment param0) {
        return simpleBuilder(param1 -> new ApplyBonusCount(param1, param0.builtInRegistryHolder(), new ApplyBonusCount.OreDrops()));
    }

    public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment param0) {
        return simpleBuilder(param1 -> new ApplyBonusCount(param1, param0.builtInRegistryHolder(), new ApplyBonusCount.UniformBonusCount(1)));
    }

    public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment param0, int param1) {
        return simpleBuilder(param2 -> new ApplyBonusCount(param2, param0.builtInRegistryHolder(), new ApplyBonusCount.UniformBonusCount(param1)));
    }

    static record BinomialWithBonusCount(int extraRounds, float probability) implements ApplyBonusCount.Formula {
        private static final Codec<ApplyBonusCount.BinomialWithBonusCount> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.INT.fieldOf("extra").forGetter(ApplyBonusCount.BinomialWithBonusCount::extraRounds),
                        Codec.FLOAT.fieldOf("probability").forGetter(ApplyBonusCount.BinomialWithBonusCount::probability)
                    )
                    .apply(param0, ApplyBonusCount.BinomialWithBonusCount::new)
        );
        public static final ApplyBonusCount.FormulaType TYPE = new ApplyBonusCount.FormulaType(new ResourceLocation("binomial_with_bonus_count"), CODEC);

        @Override
        public int calculateNewCount(RandomSource param0, int param1, int param2) {
            for(int var0 = 0; var0 < param2 + this.extraRounds; ++var0) {
                if (param0.nextFloat() < this.probability) {
                    ++param1;
                }
            }

            return param1;
        }

        @Override
        public ApplyBonusCount.FormulaType getType() {
            return TYPE;
        }
    }

    interface Formula {
        int calculateNewCount(RandomSource var1, int var2, int var3);

        ApplyBonusCount.FormulaType getType();
    }

    static record FormulaType(ResourceLocation id, Codec<? extends ApplyBonusCount.Formula> codec) {
    }

    static record OreDrops() implements ApplyBonusCount.Formula {
        public static final Codec<ApplyBonusCount.OreDrops> CODEC = Codec.unit(ApplyBonusCount.OreDrops::new);
        public static final ApplyBonusCount.FormulaType TYPE = new ApplyBonusCount.FormulaType(new ResourceLocation("ore_drops"), CODEC);

        @Override
        public int calculateNewCount(RandomSource param0, int param1, int param2) {
            if (param2 > 0) {
                int var0 = param0.nextInt(param2 + 2) - 1;
                if (var0 < 0) {
                    var0 = 0;
                }

                return param1 * (var0 + 1);
            } else {
                return param1;
            }
        }

        @Override
        public ApplyBonusCount.FormulaType getType() {
            return TYPE;
        }
    }

    static record UniformBonusCount(int bonusMultiplier) implements ApplyBonusCount.Formula {
        public static final Codec<ApplyBonusCount.UniformBonusCount> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(Codec.INT.fieldOf("bonusMultiplier").forGetter(ApplyBonusCount.UniformBonusCount::bonusMultiplier))
                    .apply(param0, ApplyBonusCount.UniformBonusCount::new)
        );
        public static final ApplyBonusCount.FormulaType TYPE = new ApplyBonusCount.FormulaType(new ResourceLocation("uniform_bonus_count"), CODEC);

        @Override
        public int calculateNewCount(RandomSource param0, int param1, int param2) {
            return param1 + param0.nextInt(this.bonusMultiplier * param2 + 1);
        }

        @Override
        public ApplyBonusCount.FormulaType getType() {
            return TYPE;
        }
    }
}
