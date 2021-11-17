package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class EntityUUIDFix extends AbstractUUIDFix {
    private static final Set<String> ABSTRACT_HORSES = Sets.newHashSet();
    private static final Set<String> TAMEABLE_ANIMALS = Sets.newHashSet();
    private static final Set<String> ANIMALS = Sets.newHashSet();
    private static final Set<String> MOBS = Sets.newHashSet();
    private static final Set<String> LIVING_ENTITIES = Sets.newHashSet();
    private static final Set<String> PROJECTILES = Sets.newHashSet();

    public EntityUUIDFix(Schema param0) {
        super(param0, References.ENTITY);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("EntityUUIDFixes", this.getInputSchema().getType(this.typeReference), param0 -> {
            param0 = param0.update(DSL.remainderFinder(), EntityUUIDFix::updateEntityUUID);

            for(String var0 : ABSTRACT_HORSES) {
                param0 = this.updateNamedChoice(param0, var0, EntityUUIDFix::updateAnimalOwner);
            }

            for(String var1 : TAMEABLE_ANIMALS) {
                param0 = this.updateNamedChoice(param0, var1, EntityUUIDFix::updateAnimalOwner);
            }

            for(String var2 : ANIMALS) {
                param0 = this.updateNamedChoice(param0, var2, EntityUUIDFix::updateAnimal);
            }

            for(String var3 : MOBS) {
                param0 = this.updateNamedChoice(param0, var3, EntityUUIDFix::updateMob);
            }

            for(String var4 : LIVING_ENTITIES) {
                param0 = this.updateNamedChoice(param0, var4, EntityUUIDFix::updateLivingEntity);
            }

            for(String var5 : PROJECTILES) {
                param0 = this.updateNamedChoice(param0, var5, EntityUUIDFix::updateProjectile);
            }

            param0 = this.updateNamedChoice(param0, "minecraft:bee", EntityUUIDFix::updateHurtBy);
            param0 = this.updateNamedChoice(param0, "minecraft:zombified_piglin", EntityUUIDFix::updateHurtBy);
            param0 = this.updateNamedChoice(param0, "minecraft:fox", EntityUUIDFix::updateFox);
            param0 = this.updateNamedChoice(param0, "minecraft:item", EntityUUIDFix::updateItem);
            param0 = this.updateNamedChoice(param0, "minecraft:shulker_bullet", EntityUUIDFix::updateShulkerBullet);
            param0 = this.updateNamedChoice(param0, "minecraft:area_effect_cloud", EntityUUIDFix::updateAreaEffectCloud);
            param0 = this.updateNamedChoice(param0, "minecraft:zombie_villager", EntityUUIDFix::updateZombieVillager);
            param0 = this.updateNamedChoice(param0, "minecraft:evoker_fangs", EntityUUIDFix::updateEvokerFangs);
            return this.updateNamedChoice(param0, "minecraft:piglin", EntityUUIDFix::updatePiglin);
        });
    }

    private static Dynamic<?> updatePiglin(Dynamic<?> param0) {
        return param0.update(
            "Brain",
            param0x -> param0x.update(
                    "memories", param0xx -> param0xx.update("minecraft:angry_at", param0xxx -> replaceUUIDString(param0xxx, "value", "value").orElseGet(() -> {
                                LOGGER.warn("angry_at has no value.");
                                return param0xxx;
                            }))
                )
        );
    }

    private static Dynamic<?> updateEvokerFangs(Dynamic<?> param0) {
        return replaceUUIDLeastMost(param0, "OwnerUUID", "Owner").orElse(param0);
    }

    private static Dynamic<?> updateZombieVillager(Dynamic<?> param0) {
        return replaceUUIDLeastMost(param0, "ConversionPlayer", "ConversionPlayer").orElse(param0);
    }

    private static Dynamic<?> updateAreaEffectCloud(Dynamic<?> param0) {
        return replaceUUIDLeastMost(param0, "OwnerUUID", "Owner").orElse(param0);
    }

    private static Dynamic<?> updateShulkerBullet(Dynamic<?> param0) {
        param0 = replaceUUIDMLTag(param0, "Owner", "Owner").orElse(param0);
        return replaceUUIDMLTag(param0, "Target", "Target").orElse(param0);
    }

    private static Dynamic<?> updateItem(Dynamic<?> param0) {
        param0 = replaceUUIDMLTag(param0, "Owner", "Owner").orElse(param0);
        return replaceUUIDMLTag(param0, "Thrower", "Thrower").orElse(param0);
    }

    private static Dynamic<?> updateFox(Dynamic<?> param0) {
        Optional<Dynamic<?>> var0 = param0.get("TrustedUUIDs")
            .result()
            .map(param1 -> param0.createList(param1.asStream().map(param0x -> createUUIDFromML(param0x).orElseGet((Supplier<? extends Dynamic<?>>)(() -> {
                        LOGGER.warn("Trusted contained invalid data.");
                        return param0x;
                    })))));
        return DataFixUtils.orElse(var0.map(param1 -> param0.remove("TrustedUUIDs").set("Trusted", param1)), param0);
    }

    private static Dynamic<?> updateHurtBy(Dynamic<?> param0) {
        return replaceUUIDString(param0, "HurtBy", "HurtBy").orElse(param0);
    }

    private static Dynamic<?> updateAnimalOwner(Dynamic<?> param0) {
        Dynamic<?> var0 = updateAnimal(param0);
        return replaceUUIDString(var0, "OwnerUUID", "Owner").orElse(var0);
    }

    private static Dynamic<?> updateAnimal(Dynamic<?> param0) {
        Dynamic<?> var0 = updateMob(param0);
        return replaceUUIDLeastMost(var0, "LoveCause", "LoveCause").orElse(var0);
    }

    private static Dynamic<?> updateMob(Dynamic<?> param0) {
        return updateLivingEntity(param0).update("Leash", param0x -> replaceUUIDLeastMost(param0x, "UUID", "UUID").orElse(param0x));
    }

    public static Dynamic<?> updateLivingEntity(Dynamic<?> param0) {
        return param0.update(
            "Attributes",
            param1 -> param0.createList(
                    param1.asStream()
                        .map(
                            param0x -> param0x.update(
                                    "Modifiers",
                                    param1x -> param0x.createList(
                                            param1x.asStream().map(param0xxx -> replaceUUIDLeastMost(param0xxx, "UUID", "UUID").orElse(param0xxx))
                                        )
                                )
                        )
                )
        );
    }

    private static Dynamic<?> updateProjectile(Dynamic<?> param0) {
        return DataFixUtils.orElse(param0.get("OwnerUUID").result().map(param1 -> param0.remove("OwnerUUID").set("Owner", param1)), param0);
    }

    public static Dynamic<?> updateEntityUUID(Dynamic<?> param0) {
        return replaceUUIDLeastMost(param0, "UUID", "UUID").orElse(param0);
    }

    static {
        ABSTRACT_HORSES.add("minecraft:donkey");
        ABSTRACT_HORSES.add("minecraft:horse");
        ABSTRACT_HORSES.add("minecraft:llama");
        ABSTRACT_HORSES.add("minecraft:mule");
        ABSTRACT_HORSES.add("minecraft:skeleton_horse");
        ABSTRACT_HORSES.add("minecraft:trader_llama");
        ABSTRACT_HORSES.add("minecraft:zombie_horse");
        TAMEABLE_ANIMALS.add("minecraft:cat");
        TAMEABLE_ANIMALS.add("minecraft:parrot");
        TAMEABLE_ANIMALS.add("minecraft:wolf");
        ANIMALS.add("minecraft:bee");
        ANIMALS.add("minecraft:chicken");
        ANIMALS.add("minecraft:cow");
        ANIMALS.add("minecraft:fox");
        ANIMALS.add("minecraft:mooshroom");
        ANIMALS.add("minecraft:ocelot");
        ANIMALS.add("minecraft:panda");
        ANIMALS.add("minecraft:pig");
        ANIMALS.add("minecraft:polar_bear");
        ANIMALS.add("minecraft:rabbit");
        ANIMALS.add("minecraft:sheep");
        ANIMALS.add("minecraft:turtle");
        ANIMALS.add("minecraft:hoglin");
        MOBS.add("minecraft:bat");
        MOBS.add("minecraft:blaze");
        MOBS.add("minecraft:cave_spider");
        MOBS.add("minecraft:cod");
        MOBS.add("minecraft:creeper");
        MOBS.add("minecraft:dolphin");
        MOBS.add("minecraft:drowned");
        MOBS.add("minecraft:elder_guardian");
        MOBS.add("minecraft:ender_dragon");
        MOBS.add("minecraft:enderman");
        MOBS.add("minecraft:endermite");
        MOBS.add("minecraft:evoker");
        MOBS.add("minecraft:ghast");
        MOBS.add("minecraft:giant");
        MOBS.add("minecraft:guardian");
        MOBS.add("minecraft:husk");
        MOBS.add("minecraft:illusioner");
        MOBS.add("minecraft:magma_cube");
        MOBS.add("minecraft:pufferfish");
        MOBS.add("minecraft:zombified_piglin");
        MOBS.add("minecraft:salmon");
        MOBS.add("minecraft:shulker");
        MOBS.add("minecraft:silverfish");
        MOBS.add("minecraft:skeleton");
        MOBS.add("minecraft:slime");
        MOBS.add("minecraft:snow_golem");
        MOBS.add("minecraft:spider");
        MOBS.add("minecraft:squid");
        MOBS.add("minecraft:stray");
        MOBS.add("minecraft:tropical_fish");
        MOBS.add("minecraft:vex");
        MOBS.add("minecraft:villager");
        MOBS.add("minecraft:iron_golem");
        MOBS.add("minecraft:vindicator");
        MOBS.add("minecraft:pillager");
        MOBS.add("minecraft:wandering_trader");
        MOBS.add("minecraft:witch");
        MOBS.add("minecraft:wither");
        MOBS.add("minecraft:wither_skeleton");
        MOBS.add("minecraft:zombie");
        MOBS.add("minecraft:zombie_villager");
        MOBS.add("minecraft:phantom");
        MOBS.add("minecraft:ravager");
        MOBS.add("minecraft:piglin");
        LIVING_ENTITIES.add("minecraft:armor_stand");
        PROJECTILES.add("minecraft:arrow");
        PROJECTILES.add("minecraft:dragon_fireball");
        PROJECTILES.add("minecraft:firework_rocket");
        PROJECTILES.add("minecraft:fireball");
        PROJECTILES.add("minecraft:llama_spit");
        PROJECTILES.add("minecraft:small_fireball");
        PROJECTILES.add("minecraft:snowball");
        PROJECTILES.add("minecraft:spectral_arrow");
        PROJECTILES.add("minecraft:egg");
        PROJECTILES.add("minecraft:ender_pearl");
        PROJECTILES.add("minecraft:experience_bottle");
        PROJECTILES.add("minecraft:potion");
        PROJECTILES.add("minecraft:trident");
        PROJECTILES.add("minecraft:wither_skull");
    }
}
