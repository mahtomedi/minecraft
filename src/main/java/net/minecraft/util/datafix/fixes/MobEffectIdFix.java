package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class MobEffectIdFix extends DataFix {
    private static final Int2ObjectMap<String> ID_MAP = Util.make(new Int2ObjectOpenHashMap<>(), param0 -> {
        param0.put(1, "minecraft:speed");
        param0.put(2, "minecraft:slowness");
        param0.put(3, "minecraft:haste");
        param0.put(4, "minecraft:mining_fatigue");
        param0.put(5, "minecraft:strength");
        param0.put(6, "minecraft:instant_health");
        param0.put(7, "minecraft:instant_damage");
        param0.put(8, "minecraft:jump_boost");
        param0.put(9, "minecraft:nausea");
        param0.put(10, "minecraft:regeneration");
        param0.put(11, "minecraft:resistance");
        param0.put(12, "minecraft:fire_resistance");
        param0.put(13, "minecraft:water_breathing");
        param0.put(14, "minecraft:invisibility");
        param0.put(15, "minecraft:blindness");
        param0.put(16, "minecraft:night_vision");
        param0.put(17, "minecraft:hunger");
        param0.put(18, "minecraft:weakness");
        param0.put(19, "minecraft:poison");
        param0.put(20, "minecraft:wither");
        param0.put(21, "minecraft:health_boost");
        param0.put(22, "minecraft:absorption");
        param0.put(23, "minecraft:saturation");
        param0.put(24, "minecraft:glowing");
        param0.put(25, "minecraft:levitation");
        param0.put(26, "minecraft:luck");
        param0.put(27, "minecraft:unluck");
        param0.put(28, "minecraft:slow_falling");
        param0.put(29, "minecraft:conduit_power");
        param0.put(30, "minecraft:dolphins_grace");
        param0.put(31, "minecraft:bad_omen");
        param0.put(32, "minecraft:hero_of_the_village");
        param0.put(33, "minecraft:darkness");
    });
    private static final Set<String> MOB_EFFECT_INSTANCE_CARRIER_ITEMS = Set.of(
        "minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow"
    );

    public MobEffectIdFix(Schema param0) {
        super(param0, false);
    }

    private static <T> Optional<Dynamic<T>> getAndConvertMobEffectId(Dynamic<T> param0, String param1) {
        return param0.get(param1).asNumber().result().map(param0x -> ID_MAP.get(param0x.intValue())).map(param0::createString);
    }

    private static <T> Dynamic<T> setFieldIfPresent(Dynamic<T> param0, String param1, Optional<Dynamic<T>> param2) {
        return param2.isEmpty() ? param0 : param0.set(param1, param2.get());
    }

    private static <T> Dynamic<T> replaceField(Dynamic<T> param0, String param1, String param2, Optional<Dynamic<T>> param3) {
        return setFieldIfPresent(param0.remove(param1), param2, param3);
    }

    private static <T> Dynamic<T> renameField(Dynamic<T> param0, String param1, String param2) {
        return setFieldIfPresent(param0.remove(param1), param2, param0.get(param1).result());
    }

    private static <T> Dynamic<T> updateMobEffectIdField(Dynamic<T> param0, String param1, Dynamic<T> param2, String param3) {
        Optional<Dynamic<T>> var0 = getAndConvertMobEffectId(param0, param1);
        return replaceField(param2, param1, param3, var0);
    }

    private static <T> Dynamic<T> updateMobEffectIdField(Dynamic<T> param0, String param1, String param2) {
        return updateMobEffectIdField(param0, param1, param0, param2);
    }

    private static <T> Dynamic<T> updateMobEffectInstance(Dynamic<T> param0) {
        param0 = updateMobEffectIdField(param0, "Id", "id");
        param0 = renameField(param0, "Ambient", "ambient");
        param0 = renameField(param0, "Amplifier", "amplifier");
        param0 = renameField(param0, "Duration", "duration");
        param0 = renameField(param0, "ShowParticles", "show_particles");
        param0 = renameField(param0, "ShowIcon", "show_icon");
        param0 = renameField(param0, "FactorCalculationData", "factor_calculation_data");
        Optional<Dynamic<T>> var0 = param0.get("HiddenEffect").result().map(MobEffectIdFix::updateMobEffectInstance);
        return replaceField(param0, "HiddenEffect", "hidden_effect", var0);
    }

    private static <T> Dynamic<T> updateMobEffectInstanceList(Dynamic<T> param0, String param1, String param2) {
        Optional<Dynamic<T>> var0 = param0.get(param1)
            .asStreamOpt()
            .result()
            .map(param1x -> param0.createList(param1x.map(MobEffectIdFix::updateMobEffectInstance)));
        return replaceField(param0, param1, param2, var0);
    }

    private static <T> Dynamic<T> updateSuspiciousStewEntry(Dynamic<T> param0, Dynamic<T> param1) {
        param1 = updateMobEffectIdField(param0, "EffectId", param1, "id");
        Optional<Dynamic<T>> var0 = param0.get("EffectDuration").result();
        return replaceField(param1, "EffectDuration", "duration", var0);
    }

    private static <T> Dynamic<T> updateSuspiciousStewEntry(Dynamic<T> param0) {
        return updateSuspiciousStewEntry(param0, param0);
    }

    private Typed<?> updateNamedChoice(Typed<?> param0, TypeReference param1, String param2, Function<Dynamic<?>, Dynamic<?>> param3) {
        Type<?> var0 = this.getInputSchema().getChoiceType(param1, param2);
        Type<?> var1 = this.getOutputSchema().getChoiceType(param1, param2);
        return param0.updateTyped(DSL.namedChoice(param2, var0), var1, param1x -> param1x.update(DSL.remainderFinder(), param3));
    }

    private TypeRewriteRule blockEntityFixer() {
        Type<?> var0 = this.getInputSchema().getType(References.BLOCK_ENTITY);
        return this.fixTypeEverywhereTyped(
            "BlockEntityMobEffectIdFix", var0, param0 -> this.updateNamedChoice(param0, References.BLOCK_ENTITY, "minecraft:beacon", param0x -> {
                    param0x = updateMobEffectIdField(param0x, "Primary", "primary_effect");
                    return updateMobEffectIdField(param0x, "Secondary", "secondary_effect");
                })
        );
    }

    private static <T> Dynamic<T> fixMooshroomTag(Dynamic<T> param0) {
        Dynamic<T> var0 = param0.emptyMap();
        Dynamic<T> var1 = updateSuspiciousStewEntry(param0, var0);
        if (!var1.equals(var0)) {
            param0 = param0.set("stew_effects", param0.createList(Stream.of(var1)));
        }

        return param0.remove("EffectId").remove("EffectDuration");
    }

    private static <T> Dynamic<T> fixArrowTag(Dynamic<T> param0) {
        return updateMobEffectInstanceList(param0, "CustomPotionEffects", "custom_potion_effects");
    }

    private static <T> Dynamic<T> fixAreaEffectCloudTag(Dynamic<T> param0) {
        return updateMobEffectInstanceList(param0, "Effects", "effects");
    }

    private static Dynamic<?> updateLivingEntityTag(Dynamic<?> param0) {
        return updateMobEffectInstanceList(param0, "ActiveEffects", "active_effects");
    }

    private TypeRewriteRule entityFixer() {
        Type<?> var0 = this.getInputSchema().getType(References.ENTITY);
        return this.fixTypeEverywhereTyped("EntityMobEffectIdFix", var0, param0 -> {
            param0 = this.updateNamedChoice(param0, References.ENTITY, "minecraft:mooshroom", MobEffectIdFix::fixMooshroomTag);
            param0 = this.updateNamedChoice(param0, References.ENTITY, "minecraft:arrow", MobEffectIdFix::fixArrowTag);
            param0 = this.updateNamedChoice(param0, References.ENTITY, "minecraft:area_effect_cloud", MobEffectIdFix::fixAreaEffectCloudTag);
            return param0.update(DSL.remainderFinder(), MobEffectIdFix::updateLivingEntityTag);
        });
    }

    private static <T> Dynamic<T> fixSuspiciousStewTag(Dynamic<T> param0) {
        Optional<Dynamic<T>> var0 = param0.get("Effects")
            .asStreamOpt()
            .result()
            .map(param1 -> param0.createList(param1.map(MobEffectIdFix::updateSuspiciousStewEntry)));
        return replaceField(param0, "Effects", "effects", var0);
    }

    private TypeRewriteRule itemStackFixer() {
        OpticFinder<Pair<String, String>> var0 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        Type<?> var1 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<?> var2 = var1.findField("tag");
        return this.fixTypeEverywhereTyped(
            "ItemStackMobEffectIdFix",
            var1,
            param2 -> {
                Optional<Pair<String, String>> var0x = param2.getOptional(var0);
                if (var0x.isPresent()) {
                    String var1x = var0x.get().getSecond();
                    if (var1x.equals("minecraft:suspicious_stew")) {
                        return param2.updateTyped(var2, param0x -> param0x.update(DSL.remainderFinder(), MobEffectIdFix::fixSuspiciousStewTag));
                    }
    
                    if (MOB_EFFECT_INSTANCE_CARRIER_ITEMS.contains(var1x)) {
                        return param2.updateTyped(
                            var2,
                            param0x -> param0x.update(
                                    DSL.remainderFinder(), param0xx -> updateMobEffectInstanceList(param0xx, "CustomPotionEffects", "custom_potion_effects")
                                )
                        );
                    }
                }
    
                return param2;
            }
        );
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq(this.blockEntityFixer(), this.entityFixer(), this.itemStackFixer());
    }
}
