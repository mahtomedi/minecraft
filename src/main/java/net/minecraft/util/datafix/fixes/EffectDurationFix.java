package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EffectDurationFix extends DataFix {
    private static final Set<String> ITEM_TYPES = Set.of("minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow");

    public EffectDurationFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Schema var0 = this.getInputSchema();
        Type<?> var1 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> var2 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> var3 = var1.findField("tag");
        return TypeRewriteRule.seq(
            this.fixTypeEverywhereTyped(
                "EffectDurationEntity", var0.getType(References.ENTITY), param0 -> param0.update(DSL.remainderFinder(), this::updateEntity)
            ),
            this.fixTypeEverywhereTyped(
                "EffectDurationPlayer", var0.getType(References.PLAYER), param0 -> param0.update(DSL.remainderFinder(), this::updateEntity)
            ),
            this.fixTypeEverywhereTyped("EffectDurationItem", var1, param2 -> {
                Optional<Pair<String, String>> var0x = param2.getOptional(var2);
                if (var0x.filter(ITEM_TYPES::contains).isPresent()) {
                    Optional<? extends Typed<?>> var1x = param2.getOptionalTyped(var3);
                    if (var1x.isPresent()) {
                        Dynamic<?> var2x = var1x.get().get(DSL.remainderFinder());
                        Typed<?> var3x = var1x.get().set(DSL.remainderFinder(), var2x.update("CustomPotionEffects", this::fix));
                        return param2.set(var3, var3x);
                    }
                }
    
                return param2;
            })
        );
    }

    private Dynamic<?> fixEffect(Dynamic<?> param0) {
        return param0.update("FactorCalculationData", param1 -> {
            int var0 = param1.get("effect_changed_timestamp").asInt(-1);
            param1 = param1.remove("effect_changed_timestamp");
            int var1x = param0.get("Duration").asInt(-1);
            int var2 = var0 - var1x;
            return param1.set("ticks_active", param1.createInt(var2));
        });
    }

    private Dynamic<?> fix(Dynamic<?> param0) {
        return param0.createList(param0.asStream().map(this::fixEffect));
    }

    private Dynamic<?> updateEntity(Dynamic<?> param0) {
        param0 = param0.update("Effects", this::fix);
        param0 = param0.update("ActiveEffects", this::fix);
        return param0.update("CustomPotionEffects", this::fix);
    }
}
