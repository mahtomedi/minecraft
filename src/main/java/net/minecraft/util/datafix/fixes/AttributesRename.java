package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;

public class AttributesRename extends DataFix {
    private static final Map<String, String> RENAMES = ImmutableMap.<String, String>builder()
        .put("generic.maxHealth", "generic.max_health")
        .put("Max Health", "generic.max_health")
        .put("zombie.spawnReinforcements", "zombie.spawn_reinforcements")
        .put("Spawn Reinforcements Chance", "zombie.spawn_reinforcements")
        .put("horse.jumpStrength", "horse.jump_strength")
        .put("Jump Strength", "horse.jump_strength")
        .put("generic.followRange", "generic.follow_range")
        .put("Follow Range", "generic.follow_range")
        .put("generic.knockbackResistance", "generic.knockback_resistance")
        .put("Knockback Resistance", "generic.knockback_resistance")
        .put("generic.movementSpeed", "generic.movement_speed")
        .put("Movement Speed", "generic.movement_speed")
        .put("generic.flyingSpeed", "generic.flying_speed")
        .put("Flying Speed", "generic.flying_speed")
        .put("generic.attackDamage", "generic.attack_damage")
        .put("generic.attackKnockback", "generic.attack_knockback")
        .put("generic.attackSpeed", "generic.attack_speed")
        .put("generic.armorToughness", "generic.armor_toughness")
        .build();

    public AttributesRename(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<?> var1 = var0.findField("tag");
        return TypeRewriteRule.seq(
            this.fixTypeEverywhereTyped("Rename ItemStack Attributes", var0, param1 -> param1.updateTyped(var1, AttributesRename::fixItemStackTag)),
            this.fixTypeEverywhereTyped("Rename Entity Attributes", this.getInputSchema().getType(References.ENTITY), AttributesRename::fixEntity),
            this.fixTypeEverywhereTyped("Rename Player Attributes", this.getInputSchema().getType(References.PLAYER), AttributesRename::fixEntity)
        );
    }

    private static Dynamic<?> fixName(Dynamic<?> param0) {
        return DataFixUtils.orElse(param0.asString().result().map(param0x -> RENAMES.getOrDefault(param0x, param0x)).map(param0::createString), param0);
    }

    private static Typed<?> fixItemStackTag(Typed<?> param0) {
        return param0.update(
            DSL.remainderFinder(),
            param0x -> param0x.update(
                    "AttributeModifiers",
                    param0xx -> DataFixUtils.orElse(
                            param0xx.asStreamOpt()
                                .result()
                                .map(param0xxx -> param0xxx.map(param0xxxx -> param0xxxx.update("AttributeName", AttributesRename::fixName)))
                                .map(param0xx::createList),
                            param0xx
                        )
                )
        );
    }

    private static Typed<?> fixEntity(Typed<?> param0) {
        return param0.update(
            DSL.remainderFinder(),
            param0x -> param0x.update(
                    "Attributes",
                    param0xx -> DataFixUtils.orElse(
                            param0xx.asStreamOpt()
                                .result()
                                .map(param0xxx -> param0xxx.map(param0xxxx -> param0xxxx.update("Name", AttributesRename::fixName)))
                                .map(param0xx::createList),
                            param0xx
                        )
                )
        );
    }
}
