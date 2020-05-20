package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public class StatsCounterFix extends DataFix {
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

    public StatsCounterFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getOutputSchema().getType(References.STATS);
        return this.fixTypeEverywhereTyped(
            "StatsCounterFix",
            this.getInputSchema().getType(References.STATS),
            var0,
            param1 -> {
                Dynamic<?> var0x = param1.get(DSL.remainderFinder());
                Map<Dynamic<?>, Dynamic<?>> var1x = Maps.newHashMap();
                Optional<? extends Map<? extends Dynamic<?>, ? extends Dynamic<?>>> var2 = var0x.getMapValues().result();
                if (var2.isPresent()) {
                    Iterator var6 = var2.get().entrySet().iterator();
    
                    while(true) {
                        Entry<? extends Dynamic<?>, ? extends Dynamic<?>> var3;
                        String var5;
                        String var6;
                        while(true) {
                            if (!var6.hasNext()) {
                                return var0.readTyped(var0x.emptyMap().set("stats", var0x.createMap(var1x)))
                                    .result()
                                    .orElseThrow(() -> new IllegalStateException("Could not parse new stats object."))
                                    .getFirst();
                            }
    
                            var3 = (Entry)var6.next();
                            if (var3.getValue().asNumber().result().isPresent()) {
                                String var4 = var3.getKey().asString("");
                                if (!SKIP.contains(var4)) {
                                    if (CUSTOM_MAP.containsKey(var4)) {
                                        var5 = "minecraft:custom";
                                        var6 = CUSTOM_MAP.get(var4);
                                        break;
                                    }
    
                                    int var7 = StringUtils.ordinalIndexOf(var4, ".", 2);
                                    if (var7 >= 0) {
                                        String var8 = var4.substring(0, var7);
                                        if ("stat.mineBlock".equals(var8)) {
                                            var5 = "minecraft:mined";
                                            var6 = this.upgradeBlock(var4.substring(var7 + 1).replace('.', ':'));
                                            break;
                                        }
    
                                        if (ITEM_KEYS.containsKey(var8)) {
                                            var5 = ITEM_KEYS.get(var8);
                                            String var12 = var4.substring(var7 + 1).replace('.', ':');
                                            String var13 = this.upgradeItem(var12);
                                            var6 = var13 == null ? var12 : var13;
                                            break;
                                        }
    
                                        if (ENTITY_KEYS.containsKey(var8)) {
                                            var5 = ENTITY_KEYS.get(var8);
                                            String var16 = var4.substring(var7 + 1).replace('.', ':');
                                            var6 = ENTITIES.getOrDefault(var16, var16);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
    
                        Dynamic<?> var18 = var0x.createString(var5);
                        Dynamic<?> var19 = var1x.computeIfAbsent(var18, param1x -> var0x.emptyMap());
                        var1x.put(var18, var19.set(var6, var3.getValue()));
                    }
                } else {
                    return var0.readTyped(var0x.emptyMap().set("stats", var0x.createMap(var1x)))
                        .result()
                        .orElseThrow(() -> new IllegalStateException("Could not parse new stats object."))
                        .getFirst();
                }
            }
        );
    }

    @Nullable
    protected String upgradeItem(String param0) {
        return ItemStackTheFlatteningFix.updateItem(param0, 0);
    }

    protected String upgradeBlock(String param0) {
        return BlockStateData.upgradeBlock(param0);
    }
}
