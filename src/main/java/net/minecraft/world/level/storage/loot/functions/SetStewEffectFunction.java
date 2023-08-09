package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetStewEffectFunction extends LootItemConditionalFunction {
    private static final Codec<List<SetStewEffectFunction.EffectEntry>> EFFECTS_LIST = ExtraCodecs.validate(
        SetStewEffectFunction.EffectEntry.CODEC.listOf(), param0 -> {
            Set<Holder<MobEffect>> var0 = new ObjectOpenHashSet<>();
    
            for(SetStewEffectFunction.EffectEntry var1 : param0) {
                if (!var0.add(var1.effect())) {
                    return DataResult.error(() -> "Encountered duplicate mob effect: '" + var1.effect() + "'");
                }
            }
    
            return DataResult.success(param0);
        }
    );
    public static final Codec<SetStewEffectFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(ExtraCodecs.strictOptionalField(EFFECTS_LIST, "effects", List.of()).forGetter(param0x -> param0x.effects))
                .apply(param0, SetStewEffectFunction::new)
    );
    private final List<SetStewEffectFunction.EffectEntry> effects;

    SetStewEffectFunction(List<LootItemCondition> param0, List<SetStewEffectFunction.EffectEntry> param1) {
        super(param0);
        this.effects = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_STEW_EFFECT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.effects.stream().flatMap(param0 -> param0.duration().getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        if (param0.is(Items.SUSPICIOUS_STEW) && !this.effects.isEmpty()) {
            SetStewEffectFunction.EffectEntry var0 = Util.getRandom(this.effects, param1.getRandom());
            MobEffect var1 = var0.effect().value();
            int var2 = var0.duration().getInt(param1);
            if (!var1.isInstantenous()) {
                var2 *= 20;
            }

            SuspiciousStewItem.appendMobEffects(param0, List.of(new SuspiciousEffectHolder.EffectEntry(var1, var2)));
            return param0;
        } else {
            return param0;
        }
    }

    public static SetStewEffectFunction.Builder stewEffect() {
        return new SetStewEffectFunction.Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<SetStewEffectFunction.Builder> {
        private final ImmutableList.Builder<SetStewEffectFunction.EffectEntry> effects = ImmutableList.builder();

        protected SetStewEffectFunction.Builder getThis() {
            return this;
        }

        public SetStewEffectFunction.Builder withEffect(MobEffect param0, NumberProvider param1) {
            this.effects.add(new SetStewEffectFunction.EffectEntry(param0.builtInRegistryHolder(), param1));
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetStewEffectFunction(this.getConditions(), this.effects.build());
        }
    }

    static record EffectEntry(Holder<MobEffect> effect, NumberProvider duration) {
        public static final Codec<SetStewEffectFunction.EffectEntry> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("type").forGetter(SetStewEffectFunction.EffectEntry::effect),
                        NumberProviders.CODEC.fieldOf("duration").forGetter(SetStewEffectFunction.EffectEntry::duration)
                    )
                    .apply(param0, SetStewEffectFunction.EffectEntry::new)
        );
    }
}
