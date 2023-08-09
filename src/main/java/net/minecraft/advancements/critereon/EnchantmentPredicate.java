package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;

public record EnchantmentPredicate(Optional<Holder<Enchantment>> enchantment, MinMaxBounds.Ints level) {
    public static final Codec<EnchantmentPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(BuiltInRegistries.ENCHANTMENT.holderByNameCodec(), "enchantment")
                        .forGetter(EnchantmentPredicate::enchantment),
                    ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "levels", MinMaxBounds.Ints.ANY).forGetter(EnchantmentPredicate::level)
                )
                .apply(param0, EnchantmentPredicate::new)
    );

    public EnchantmentPredicate(Enchantment param0, MinMaxBounds.Ints param1) {
        this(Optional.of(param0.builtInRegistryHolder()), param1);
    }

    public boolean containedIn(Map<Enchantment, Integer> param0) {
        if (this.enchantment.isPresent()) {
            Enchantment var0 = this.enchantment.get().value();
            if (!param0.containsKey(var0)) {
                return false;
            }

            int var1 = param0.get(var0);
            if (this.level != MinMaxBounds.Ints.ANY && !this.level.matches(var1)) {
                return false;
            }
        } else if (this.level != MinMaxBounds.Ints.ANY) {
            for(Integer var2 : param0.values()) {
                if (this.level.matches(var2)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }
}
