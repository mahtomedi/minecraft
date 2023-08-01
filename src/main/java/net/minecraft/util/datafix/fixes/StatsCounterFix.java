package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.util.datafix.schemas.V1451_6;
import org.apache.commons.lang3.StringUtils;

public class StatsCounterFix extends DataFix {
    private static final Set<String> SPECIAL_OBJECTIVE_CRITERIA = Set.of(
        "dummy",
        "trigger",
        "deathCount",
        "playerKillCount",
        "totalKillCount",
        "health",
        "food",
        "air",
        "armor",
        "xp",
        "level",
        "killedByTeam.aqua",
        "killedByTeam.black",
        "killedByTeam.blue",
        "killedByTeam.dark_aqua",
        "killedByTeam.dark_blue",
        "killedByTeam.dark_gray",
        "killedByTeam.dark_green",
        "killedByTeam.dark_purple",
        "killedByTeam.dark_red",
        "killedByTeam.gold",
        "killedByTeam.gray",
        "killedByTeam.green",
        "killedByTeam.light_purple",
        "killedByTeam.red",
        "killedByTeam.white",
        "killedByTeam.yellow",
        "teamkill.aqua",
        "teamkill.black",
        "teamkill.blue",
        "teamkill.dark_aqua",
        "teamkill.dark_blue",
        "teamkill.dark_gray",
        "teamkill.dark_green",
        "teamkill.dark_purple",
        "teamkill.dark_red",
        "teamkill.gold",
        "teamkill.gray",
        "teamkill.green",
        "teamkill.light_purple",
        "teamkill.red",
        "teamkill.white",
        "teamkill.yellow"
    );
    private static final Set<String> SKIP = ImmutableSet.<String>builder()
        .add("stat.craftItem.minecraft.spawn_egg")
        .add("stat.useItem.minecraft.spawn_egg")
        .add("stat.breakItem.minecraft.spawn_egg")
        .add("stat.pickup.minecraft.spawn_egg")
        .add("stat.drop.minecraft.spawn_egg")
        .build();
    private static final Map<String, String> CUSTOM_MAP = ImmutableMap.<String, String>builder()
        .put("stat.leaveGame", "minecraft:leave_game")
        .put("stat.playOneMinute", "minecraft:play_one_minute")
        .put("stat.timeSinceDeath", "minecraft:time_since_death")
        .put("stat.sneakTime", "minecraft:sneak_time")
        .put("stat.walkOneCm", "minecraft:walk_one_cm")
        .put("stat.crouchOneCm", "minecraft:crouch_one_cm")
        .put("stat.sprintOneCm", "minecraft:sprint_one_cm")
        .put("stat.swimOneCm", "minecraft:swim_one_cm")
        .put("stat.fallOneCm", "minecraft:fall_one_cm")
        .put("stat.climbOneCm", "minecraft:climb_one_cm")
        .put("stat.flyOneCm", "minecraft:fly_one_cm")
        .put("stat.diveOneCm", "minecraft:dive_one_cm")
        .put("stat.minecartOneCm", "minecraft:minecart_one_cm")
        .put("stat.boatOneCm", "minecraft:boat_one_cm")
        .put("stat.pigOneCm", "minecraft:pig_one_cm")
        .put("stat.horseOneCm", "minecraft:horse_one_cm")
        .put("stat.aviateOneCm", "minecraft:aviate_one_cm")
        .put("stat.jump", "minecraft:jump")
        .put("stat.drop", "minecraft:drop")
        .put("stat.damageDealt", "minecraft:damage_dealt")
        .put("stat.damageTaken", "minecraft:damage_taken")
        .put("stat.deaths", "minecraft:deaths")
        .put("stat.mobKills", "minecraft:mob_kills")
        .put("stat.animalsBred", "minecraft:animals_bred")
        .put("stat.playerKills", "minecraft:player_kills")
        .put("stat.fishCaught", "minecraft:fish_caught")
        .put("stat.talkedToVillager", "minecraft:talked_to_villager")
        .put("stat.tradedWithVillager", "minecraft:traded_with_villager")
        .put("stat.cakeSlicesEaten", "minecraft:eat_cake_slice")
        .put("stat.cauldronFilled", "minecraft:fill_cauldron")
        .put("stat.cauldronUsed", "minecraft:use_cauldron")
        .put("stat.armorCleaned", "minecraft:clean_armor")
        .put("stat.bannerCleaned", "minecraft:clean_banner")
        .put("stat.brewingstandInteraction", "minecraft:interact_with_brewingstand")
        .put("stat.beaconInteraction", "minecraft:interact_with_beacon")
        .put("stat.dropperInspected", "minecraft:inspect_dropper")
        .put("stat.hopperInspected", "minecraft:inspect_hopper")
        .put("stat.dispenserInspected", "minecraft:inspect_dispenser")
        .put("stat.noteblockPlayed", "minecraft:play_noteblock")
        .put("stat.noteblockTuned", "minecraft:tune_noteblock")
        .put("stat.flowerPotted", "minecraft:pot_flower")
        .put("stat.trappedChestTriggered", "minecraft:trigger_trapped_chest")
        .put("stat.enderchestOpened", "minecraft:open_enderchest")
        .put("stat.itemEnchanted", "minecraft:enchant_item")
        .put("stat.recordPlayed", "minecraft:play_record")
        .put("stat.furnaceInteraction", "minecraft:interact_with_furnace")
        .put("stat.craftingTableInteraction", "minecraft:interact_with_crafting_table")
        .put("stat.chestOpened", "minecraft:open_chest")
        .put("stat.sleepInBed", "minecraft:sleep_in_bed")
        .put("stat.shulkerBoxOpened", "minecraft:open_shulker_box")
        .build();
    private static final String BLOCK_KEY = "stat.mineBlock";
    private static final String NEW_BLOCK_KEY = "minecraft:mined";
    private static final Map<String, String> ITEM_KEYS = ImmutableMap.<String, String>builder()
        .put("stat.craftItem", "minecraft:crafted")
        .put("stat.useItem", "minecraft:used")
        .put("stat.breakItem", "minecraft:broken")
        .put("stat.pickup", "minecraft:picked_up")
        .put("stat.drop", "minecraft:dropped")
        .build();
    private static final Map<String, String> ENTITY_KEYS = ImmutableMap.<String, String>builder()
        .put("stat.entityKilledBy", "minecraft:killed_by")
        .put("stat.killEntity", "minecraft:killed")
        .build();
    private static final Map<String, String> ENTITIES = ImmutableMap.<String, String>builder()
        .put("Bat", "minecraft:bat")
        .put("Blaze", "minecraft:blaze")
        .put("CaveSpider", "minecraft:cave_spider")
        .put("Chicken", "minecraft:chicken")
        .put("Cow", "minecraft:cow")
        .put("Creeper", "minecraft:creeper")
        .put("Donkey", "minecraft:donkey")
        .put("ElderGuardian", "minecraft:elder_guardian")
        .put("Enderman", "minecraft:enderman")
        .put("Endermite", "minecraft:endermite")
        .put("EvocationIllager", "minecraft:evocation_illager")
        .put("Ghast", "minecraft:ghast")
        .put("Guardian", "minecraft:guardian")
        .put("Horse", "minecraft:horse")
        .put("Husk", "minecraft:husk")
        .put("Llama", "minecraft:llama")
        .put("LavaSlime", "minecraft:magma_cube")
        .put("MushroomCow", "minecraft:mooshroom")
        .put("Mule", "minecraft:mule")
        .put("Ozelot", "minecraft:ocelot")
        .put("Parrot", "minecraft:parrot")
        .put("Pig", "minecraft:pig")
        .put("PolarBear", "minecraft:polar_bear")
        .put("Rabbit", "minecraft:rabbit")
        .put("Sheep", "minecraft:sheep")
        .put("Shulker", "minecraft:shulker")
        .put("Silverfish", "minecraft:silverfish")
        .put("SkeletonHorse", "minecraft:skeleton_horse")
        .put("Skeleton", "minecraft:skeleton")
        .put("Slime", "minecraft:slime")
        .put("Spider", "minecraft:spider")
        .put("Squid", "minecraft:squid")
        .put("Stray", "minecraft:stray")
        .put("Vex", "minecraft:vex")
        .put("Villager", "minecraft:villager")
        .put("VindicationIllager", "minecraft:vindication_illager")
        .put("Witch", "minecraft:witch")
        .put("WitherSkeleton", "minecraft:wither_skeleton")
        .put("Wolf", "minecraft:wolf")
        .put("ZombieHorse", "minecraft:zombie_horse")
        .put("PigZombie", "minecraft:zombie_pigman")
        .put("ZombieVillager", "minecraft:zombie_villager")
        .put("Zombie", "minecraft:zombie")
        .build();
    private static final String NEW_CUSTOM_KEY = "minecraft:custom";

    public StatsCounterFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Nullable
    private static StatsCounterFix.StatType unpackLegacyKey(String param0) {
        if (SKIP.contains(param0)) {
            return null;
        } else {
            String var0 = CUSTOM_MAP.get(param0);
            if (var0 != null) {
                return new StatsCounterFix.StatType("minecraft:custom", var0);
            } else {
                int var1 = StringUtils.ordinalIndexOf(param0, ".", 2);
                if (var1 < 0) {
                    return null;
                } else {
                    String var2 = param0.substring(0, var1);
                    if ("stat.mineBlock".equals(var2)) {
                        String var3 = upgradeBlock(param0.substring(var1 + 1).replace('.', ':'));
                        return new StatsCounterFix.StatType("minecraft:mined", var3);
                    } else {
                        String var4 = ITEM_KEYS.get(var2);
                        if (var4 != null) {
                            String var5 = param0.substring(var1 + 1).replace('.', ':');
                            String var6 = upgradeItem(var5);
                            String var7 = var6 == null ? var5 : var6;
                            return new StatsCounterFix.StatType(var4, var7);
                        } else {
                            String var8 = ENTITY_KEYS.get(var2);
                            if (var8 != null) {
                                String var9 = param0.substring(var1 + 1).replace('.', ':');
                                String var10 = ENTITIES.getOrDefault(var9, var9);
                                return new StatsCounterFix.StatType(var8, var10);
                            } else {
                                return null;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq(this.makeStatFixer(), this.makeObjectiveFixer());
    }

    private TypeRewriteRule makeStatFixer() {
        Type<?> var0 = this.getInputSchema().getType(References.STATS);
        Type<?> var1 = this.getOutputSchema().getType(References.STATS);
        return this.fixTypeEverywhereTyped(
            "StatsCounterFix",
            var0,
            var1,
            param1 -> {
                Dynamic<?> var0x = param1.get(DSL.remainderFinder());
                Map<Dynamic<?>, Dynamic<?>> var1x = Maps.newHashMap();
                Optional<? extends Map<? extends Dynamic<?>, ? extends Dynamic<?>>> var2x = var0x.getMapValues().result();
                if (var2x.isPresent()) {
                    for(Entry<? extends Dynamic<?>, ? extends Dynamic<?>> var3 : ((Map)var2x.get()).entrySet()) {
                        if (var3.getValue().asNumber().result().isPresent()) {
                            String var4 = var3.getKey().asString("");
                            StatsCounterFix.StatType var5 = unpackLegacyKey(var4);
                            if (var5 != null) {
                                Dynamic<?> var6 = var0x.createString(var5.type());
                                Dynamic<?> var7 = var1x.computeIfAbsent(var6, param1x -> var0x.emptyMap());
                                var1x.put(var6, var7.set(var5.typeKey(), var3.getValue()));
                            }
                        }
                    }
                }
    
                return var1.readTyped(var0x.emptyMap().set("stats", var0x.createMap(var1x)))
                    .result()
                    .orElseThrow(() -> new IllegalStateException("Could not parse new stats object."))
                    .getFirst();
            }
        );
    }

    private TypeRewriteRule makeObjectiveFixer() {
        Type<?> var0 = this.getInputSchema().getType(References.OBJECTIVE);
        Type<?> var1 = this.getOutputSchema().getType(References.OBJECTIVE);
        return this.fixTypeEverywhereTyped("ObjectiveStatFix", var0, var1, param1 -> {
            Dynamic<?> var0x = param1.get(DSL.remainderFinder());
            Dynamic<?> var1x = var0x.update("CriteriaName", param0x -> DataFixUtils.orElse(param0x.asString().result().map(param0xx -> {
                    if (SPECIAL_OBJECTIVE_CRITERIA.contains(param0xx)) {
                        return param0xx;
                    } else {
                        StatsCounterFix.StatType var0xx = unpackLegacyKey(param0xx);
                        return var0xx == null ? "dummy" : V1451_6.packNamespacedWithDot(var0xx.type) + ":" + V1451_6.packNamespacedWithDot(var0xx.typeKey);
                    }
                }).map(param0x::createString), param0x));
            return var1.readTyped(var1x).result().orElseThrow(() -> new IllegalStateException("Could not parse new objective object.")).getFirst();
        });
    }

    @Nullable
    private static String upgradeItem(String param0) {
        return ItemStackTheFlatteningFix.updateItem(param0, 0);
    }

    private static String upgradeBlock(String param0) {
        return BlockStateData.upgradeBlock(param0);
    }

    static record StatType(String type, String typeKey) {
    }
}
