package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class IntRange {
    private static final Codec<IntRange> RECORD_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(NumberProviders.CODEC, "min").forGetter(param0x -> Optional.ofNullable(param0x.min)),
                    ExtraCodecs.strictOptionalField(NumberProviders.CODEC, "max").forGetter(param0x -> Optional.ofNullable(param0x.max))
                )
                .apply(param0, IntRange::new)
    );
    public static final Codec<IntRange> CODEC = Codec.either(Codec.INT, RECORD_CODEC)
        .xmap(param0 -> param0.map(IntRange::exact, Function.identity()), param0 -> {
            OptionalInt var0 = param0.unpackExact();
            return var0.isPresent() ? Either.left(var0.getAsInt()) : Either.right(param0);
        });
    @Nullable
    private final NumberProvider min;
    @Nullable
    private final NumberProvider max;
    private final IntRange.IntLimiter limiter;
    private final IntRange.IntChecker predicate;

    public Set<LootContextParam<?>> getReferencedContextParams() {
        Builder<LootContextParam<?>> var0 = ImmutableSet.builder();
        if (this.min != null) {
            var0.addAll(this.min.getReferencedContextParams());
        }

        if (this.max != null) {
            var0.addAll(this.max.getReferencedContextParams());
        }

        return var0.build();
    }

    private IntRange(Optional<NumberProvider> param0, Optional<NumberProvider> param1) {
        this(param0.orElse(null), param1.orElse(null));
    }

    private IntRange(@Nullable NumberProvider param0, @Nullable NumberProvider param1) {
        this.min = param0;
        this.max = param1;
        if (param0 == null) {
            if (param1 == null) {
                this.limiter = (param0x, param1x) -> param1x;
                this.predicate = (param0x, param1x) -> true;
            } else {
                this.limiter = (param1x, param2) -> Math.min(param1.getInt(param1x), param2);
                this.predicate = (param1x, param2) -> param2 <= param1.getInt(param1x);
            }
        } else if (param1 == null) {
            this.limiter = (param1x, param2) -> Math.max(param0.getInt(param1x), param2);
            this.predicate = (param1x, param2) -> param2 >= param0.getInt(param1x);
        } else {
            this.limiter = (param2, param3) -> Mth.clamp(param3, param0.getInt(param2), param1.getInt(param2));
            this.predicate = (param2, param3) -> param3 >= param0.getInt(param2) && param3 <= param1.getInt(param2);
        }

    }

    public static IntRange exact(int param0) {
        ConstantValue var0 = ConstantValue.exactly((float)param0);
        return new IntRange(Optional.of(var0), Optional.of(var0));
    }

    public static IntRange range(int param0, int param1) {
        return new IntRange(Optional.of(ConstantValue.exactly((float)param0)), Optional.of(ConstantValue.exactly((float)param1)));
    }

    public static IntRange lowerBound(int param0) {
        return new IntRange(Optional.of(ConstantValue.exactly((float)param0)), Optional.empty());
    }

    public static IntRange upperBound(int param0) {
        return new IntRange(Optional.empty(), Optional.of(ConstantValue.exactly((float)param0)));
    }

    public int clamp(LootContext param0, int param1) {
        return this.limiter.apply(param0, param1);
    }

    public boolean test(LootContext param0, int param1) {
        return this.predicate.test(param0, param1);
    }

    private OptionalInt unpackExact() {
        if (Objects.equals(this.min, this.max)) {
            NumberProvider var2 = this.min;
            if (var2 instanceof ConstantValue var0 && Math.floor((double)var0.value()) == (double)var0.value()) {
                return OptionalInt.of((int)var0.value());
            }
        }

        return OptionalInt.empty();
    }

    @FunctionalInterface
    interface IntChecker {
        boolean test(LootContext var1, int var2);
    }

    @FunctionalInterface
    interface IntLimiter {
        int apply(LootContext var1, int var2);
    }
}
