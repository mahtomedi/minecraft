package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class ItemStackTheFlatteningFix extends DataFix {
    private static final Map<String, String> MAP = DataFixUtils.make(Maps.newHashMap(), param0 -> {
        param0.put("minecraft:stone.0", "minecraft:stone");
        param0.put("minecraft:stone.1", "minecraft:granite");
        param0.put("minecraft:stone.2", "minecraft:polished_granite");
        param0.put("minecraft:stone.3", "minecraft:diorite");
        param0.put("minecraft:stone.4", "minecraft:polished_diorite");
        param0.put("minecraft:stone.5", "minecraft:andesite");
        param0.put("minecraft:stone.6", "minecraft:polished_andesite");
        param0.put("minecraft:dirt.0", "minecraft:dirt");
        param0.put("minecraft:dirt.1", "minecraft:coarse_dirt");
        param0.put("minecraft:dirt.2", "minecraft:podzol");
        param0.put("minecraft:leaves.0", "minecraft:oak_leaves");
        param0.put("minecraft:leaves.1", "minecraft:spruce_leaves");
        param0.put("minecraft:leaves.2", "minecraft:birch_leaves");
        param0.put("minecraft:leaves.3", "minecraft:jungle_leaves");
        param0.put("minecraft:leaves2.0", "minecraft:acacia_leaves");
        param0.put("minecraft:leaves2.1", "minecraft:dark_oak_leaves");
        param0.put("minecraft:log.0", "minecraft:oak_log");
        param0.put("minecraft:log.1", "minecraft:spruce_log");
        param0.put("minecraft:log.2", "minecraft:birch_log");
        param0.put("minecraft:log.3", "minecraft:jungle_log");
        param0.put("minecraft:log2.0", "minecraft:acacia_log");
        param0.put("minecraft:log2.1", "minecraft:dark_oak_log");
        param0.put("minecraft:sapling.0", "minecraft:oak_sapling");
        param0.put("minecraft:sapling.1", "minecraft:spruce_sapling");
        param0.put("minecraft:sapling.2", "minecraft:birch_sapling");
        param0.put("minecraft:sapling.3", "minecraft:jungle_sapling");
        param0.put("minecraft:sapling.4", "minecraft:acacia_sapling");
        param0.put("minecraft:sapling.5", "minecraft:dark_oak_sapling");
        param0.put("minecraft:planks.0", "minecraft:oak_planks");
        param0.put("minecraft:planks.1", "minecraft:spruce_planks");
        param0.put("minecraft:planks.2", "minecraft:birch_planks");
        param0.put("minecraft:planks.3", "minecraft:jungle_planks");
        param0.put("minecraft:planks.4", "minecraft:acacia_planks");
        param0.put("minecraft:planks.5", "minecraft:dark_oak_planks");
        param0.put("minecraft:sand.0", "minecraft:sand");
        param0.put("minecraft:sand.1", "minecraft:red_sand");
        param0.put("minecraft:quartz_block.0", "minecraft:quartz_block");
        param0.put("minecraft:quartz_block.1", "minecraft:chiseled_quartz_block");
        param0.put("minecraft:quartz_block.2", "minecraft:quartz_pillar");
        param0.put("minecraft:anvil.0", "minecraft:anvil");
        param0.put("minecraft:anvil.1", "minecraft:chipped_anvil");
        param0.put("minecraft:anvil.2", "minecraft:damaged_anvil");
        param0.put("minecraft:wool.0", "minecraft:white_wool");
        param0.put("minecraft:wool.1", "minecraft:orange_wool");
        param0.put("minecraft:wool.2", "minecraft:magenta_wool");
        param0.put("minecraft:wool.3", "minecraft:light_blue_wool");
        param0.put("minecraft:wool.4", "minecraft:yellow_wool");
        param0.put("minecraft:wool.5", "minecraft:lime_wool");
        param0.put("minecraft:wool.6", "minecraft:pink_wool");
        param0.put("minecraft:wool.7", "minecraft:gray_wool");
        param0.put("minecraft:wool.8", "minecraft:light_gray_wool");
        param0.put("minecraft:wool.9", "minecraft:cyan_wool");
        param0.put("minecraft:wool.10", "minecraft:purple_wool");
        param0.put("minecraft:wool.11", "minecraft:blue_wool");
        param0.put("minecraft:wool.12", "minecraft:brown_wool");
        param0.put("minecraft:wool.13", "minecraft:green_wool");
        param0.put("minecraft:wool.14", "minecraft:red_wool");
        param0.put("minecraft:wool.15", "minecraft:black_wool");
        param0.put("minecraft:carpet.0", "minecraft:white_carpet");
        param0.put("minecraft:carpet.1", "minecraft:orange_carpet");
        param0.put("minecraft:carpet.2", "minecraft:magenta_carpet");
        param0.put("minecraft:carpet.3", "minecraft:light_blue_carpet");
        param0.put("minecraft:carpet.4", "minecraft:yellow_carpet");
        param0.put("minecraft:carpet.5", "minecraft:lime_carpet");
        param0.put("minecraft:carpet.6", "minecraft:pink_carpet");
        param0.put("minecraft:carpet.7", "minecraft:gray_carpet");
        param0.put("minecraft:carpet.8", "minecraft:light_gray_carpet");
        param0.put("minecraft:carpet.9", "minecraft:cyan_carpet");
        param0.put("minecraft:carpet.10", "minecraft:purple_carpet");
        param0.put("minecraft:carpet.11", "minecraft:blue_carpet");
        param0.put("minecraft:carpet.12", "minecraft:brown_carpet");
        param0.put("minecraft:carpet.13", "minecraft:green_carpet");
        param0.put("minecraft:carpet.14", "minecraft:red_carpet");
        param0.put("minecraft:carpet.15", "minecraft:black_carpet");
        param0.put("minecraft:hardened_clay.0", "minecraft:terracotta");
        param0.put("minecraft:stained_hardened_clay.0", "minecraft:white_terracotta");
        param0.put("minecraft:stained_hardened_clay.1", "minecraft:orange_terracotta");
        param0.put("minecraft:stained_hardened_clay.2", "minecraft:magenta_terracotta");
        param0.put("minecraft:stained_hardened_clay.3", "minecraft:light_blue_terracotta");
        param0.put("minecraft:stained_hardened_clay.4", "minecraft:yellow_terracotta");
        param0.put("minecraft:stained_hardened_clay.5", "minecraft:lime_terracotta");
        param0.put("minecraft:stained_hardened_clay.6", "minecraft:pink_terracotta");
        param0.put("minecraft:stained_hardened_clay.7", "minecraft:gray_terracotta");
        param0.put("minecraft:stained_hardened_clay.8", "minecraft:light_gray_terracotta");
        param0.put("minecraft:stained_hardened_clay.9", "minecraft:cyan_terracotta");
        param0.put("minecraft:stained_hardened_clay.10", "minecraft:purple_terracotta");
        param0.put("minecraft:stained_hardened_clay.11", "minecraft:blue_terracotta");
        param0.put("minecraft:stained_hardened_clay.12", "minecraft:brown_terracotta");
        param0.put("minecraft:stained_hardened_clay.13", "minecraft:green_terracotta");
        param0.put("minecraft:stained_hardened_clay.14", "minecraft:red_terracotta");
        param0.put("minecraft:stained_hardened_clay.15", "minecraft:black_terracotta");
        param0.put("minecraft:silver_glazed_terracotta.0", "minecraft:light_gray_glazed_terracotta");
        param0.put("minecraft:stained_glass.0", "minecraft:white_stained_glass");
        param0.put("minecraft:stained_glass.1", "minecraft:orange_stained_glass");
        param0.put("minecraft:stained_glass.2", "minecraft:magenta_stained_glass");
        param0.put("minecraft:stained_glass.3", "minecraft:light_blue_stained_glass");
        param0.put("minecraft:stained_glass.4", "minecraft:yellow_stained_glass");
        param0.put("minecraft:stained_glass.5", "minecraft:lime_stained_glass");
        param0.put("minecraft:stained_glass.6", "minecraft:pink_stained_glass");
        param0.put("minecraft:stained_glass.7", "minecraft:gray_stained_glass");
        param0.put("minecraft:stained_glass.8", "minecraft:light_gray_stained_glass");
        param0.put("minecraft:stained_glass.9", "minecraft:cyan_stained_glass");
        param0.put("minecraft:stained_glass.10", "minecraft:purple_stained_glass");
        param0.put("minecraft:stained_glass.11", "minecraft:blue_stained_glass");
        param0.put("minecraft:stained_glass.12", "minecraft:brown_stained_glass");
        param0.put("minecraft:stained_glass.13", "minecraft:green_stained_glass");
        param0.put("minecraft:stained_glass.14", "minecraft:red_stained_glass");
        param0.put("minecraft:stained_glass.15", "minecraft:black_stained_glass");
        param0.put("minecraft:stained_glass_pane.0", "minecraft:white_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.1", "minecraft:orange_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.2", "minecraft:magenta_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.3", "minecraft:light_blue_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.4", "minecraft:yellow_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.5", "minecraft:lime_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.6", "minecraft:pink_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.7", "minecraft:gray_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.8", "minecraft:light_gray_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.9", "minecraft:cyan_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.10", "minecraft:purple_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.11", "minecraft:blue_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.12", "minecraft:brown_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.13", "minecraft:green_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.14", "minecraft:red_stained_glass_pane");
        param0.put("minecraft:stained_glass_pane.15", "minecraft:black_stained_glass_pane");
        param0.put("minecraft:prismarine.0", "minecraft:prismarine");
        param0.put("minecraft:prismarine.1", "minecraft:prismarine_bricks");
        param0.put("minecraft:prismarine.2", "minecraft:dark_prismarine");
        param0.put("minecraft:concrete.0", "minecraft:white_concrete");
        param0.put("minecraft:concrete.1", "minecraft:orange_concrete");
        param0.put("minecraft:concrete.2", "minecraft:magenta_concrete");
        param0.put("minecraft:concrete.3", "minecraft:light_blue_concrete");
        param0.put("minecraft:concrete.4", "minecraft:yellow_concrete");
        param0.put("minecraft:concrete.5", "minecraft:lime_concrete");
        param0.put("minecraft:concrete.6", "minecraft:pink_concrete");
        param0.put("minecraft:concrete.7", "minecraft:gray_concrete");
        param0.put("minecraft:concrete.8", "minecraft:light_gray_concrete");
        param0.put("minecraft:concrete.9", "minecraft:cyan_concrete");
        param0.put("minecraft:concrete.10", "minecraft:purple_concrete");
        param0.put("minecraft:concrete.11", "minecraft:blue_concrete");
        param0.put("minecraft:concrete.12", "minecraft:brown_concrete");
        param0.put("minecraft:concrete.13", "minecraft:green_concrete");
        param0.put("minecraft:concrete.14", "minecraft:red_concrete");
        param0.put("minecraft:concrete.15", "minecraft:black_concrete");
        param0.put("minecraft:concrete_powder.0", "minecraft:white_concrete_powder");
        param0.put("minecraft:concrete_powder.1", "minecraft:orange_concrete_powder");
        param0.put("minecraft:concrete_powder.2", "minecraft:magenta_concrete_powder");
        param0.put("minecraft:concrete_powder.3", "minecraft:light_blue_concrete_powder");
        param0.put("minecraft:concrete_powder.4", "minecraft:yellow_concrete_powder");
        param0.put("minecraft:concrete_powder.5", "minecraft:lime_concrete_powder");
        param0.put("minecraft:concrete_powder.6", "minecraft:pink_concrete_powder");
        param0.put("minecraft:concrete_powder.7", "minecraft:gray_concrete_powder");
        param0.put("minecraft:concrete_powder.8", "minecraft:light_gray_concrete_powder");
        param0.put("minecraft:concrete_powder.9", "minecraft:cyan_concrete_powder");
        param0.put("minecraft:concrete_powder.10", "minecraft:purple_concrete_powder");
        param0.put("minecraft:concrete_powder.11", "minecraft:blue_concrete_powder");
        param0.put("minecraft:concrete_powder.12", "minecraft:brown_concrete_powder");
        param0.put("minecraft:concrete_powder.13", "minecraft:green_concrete_powder");
        param0.put("minecraft:concrete_powder.14", "minecraft:red_concrete_powder");
        param0.put("minecraft:concrete_powder.15", "minecraft:black_concrete_powder");
        param0.put("minecraft:cobblestone_wall.0", "minecraft:cobblestone_wall");
        param0.put("minecraft:cobblestone_wall.1", "minecraft:mossy_cobblestone_wall");
        param0.put("minecraft:sandstone.0", "minecraft:sandstone");
        param0.put("minecraft:sandstone.1", "minecraft:chiseled_sandstone");
        param0.put("minecraft:sandstone.2", "minecraft:cut_sandstone");
        param0.put("minecraft:red_sandstone.0", "minecraft:red_sandstone");
        param0.put("minecraft:red_sandstone.1", "minecraft:chiseled_red_sandstone");
        param0.put("minecraft:red_sandstone.2", "minecraft:cut_red_sandstone");
        param0.put("minecraft:stonebrick.0", "minecraft:stone_bricks");
        param0.put("minecraft:stonebrick.1", "minecraft:mossy_stone_bricks");
        param0.put("minecraft:stonebrick.2", "minecraft:cracked_stone_bricks");
        param0.put("minecraft:stonebrick.3", "minecraft:chiseled_stone_bricks");
        param0.put("minecraft:monster_egg.0", "minecraft:infested_stone");
        param0.put("minecraft:monster_egg.1", "minecraft:infested_cobblestone");
        param0.put("minecraft:monster_egg.2", "minecraft:infested_stone_bricks");
        param0.put("minecraft:monster_egg.3", "minecraft:infested_mossy_stone_bricks");
        param0.put("minecraft:monster_egg.4", "minecraft:infested_cracked_stone_bricks");
        param0.put("minecraft:monster_egg.5", "minecraft:infested_chiseled_stone_bricks");
        param0.put("minecraft:yellow_flower.0", "minecraft:dandelion");
        param0.put("minecraft:red_flower.0", "minecraft:poppy");
        param0.put("minecraft:red_flower.1", "minecraft:blue_orchid");
        param0.put("minecraft:red_flower.2", "minecraft:allium");
        param0.put("minecraft:red_flower.3", "minecraft:azure_bluet");
        param0.put("minecraft:red_flower.4", "minecraft:red_tulip");
        param0.put("minecraft:red_flower.5", "minecraft:orange_tulip");
        param0.put("minecraft:red_flower.6", "minecraft:white_tulip");
        param0.put("minecraft:red_flower.7", "minecraft:pink_tulip");
        param0.put("minecraft:red_flower.8", "minecraft:oxeye_daisy");
        param0.put("minecraft:double_plant.0", "minecraft:sunflower");
        param0.put("minecraft:double_plant.1", "minecraft:lilac");
        param0.put("minecraft:double_plant.2", "minecraft:tall_grass");
        param0.put("minecraft:double_plant.3", "minecraft:large_fern");
        param0.put("minecraft:double_plant.4", "minecraft:rose_bush");
        param0.put("minecraft:double_plant.5", "minecraft:peony");
        param0.put("minecraft:deadbush.0", "minecraft:dead_bush");
        param0.put("minecraft:tallgrass.0", "minecraft:dead_bush");
        param0.put("minecraft:tallgrass.1", "minecraft:grass");
        param0.put("minecraft:tallgrass.2", "minecraft:fern");
        param0.put("minecraft:sponge.0", "minecraft:sponge");
        param0.put("minecraft:sponge.1", "minecraft:wet_sponge");
        param0.put("minecraft:purpur_slab.0", "minecraft:purpur_slab");
        param0.put("minecraft:stone_slab.0", "minecraft:stone_slab");
        param0.put("minecraft:stone_slab.1", "minecraft:sandstone_slab");
        param0.put("minecraft:stone_slab.2", "minecraft:petrified_oak_slab");
        param0.put("minecraft:stone_slab.3", "minecraft:cobblestone_slab");
        param0.put("minecraft:stone_slab.4", "minecraft:brick_slab");
        param0.put("minecraft:stone_slab.5", "minecraft:stone_brick_slab");
        param0.put("minecraft:stone_slab.6", "minecraft:nether_brick_slab");
        param0.put("minecraft:stone_slab.7", "minecraft:quartz_slab");
        param0.put("minecraft:stone_slab2.0", "minecraft:red_sandstone_slab");
        param0.put("minecraft:wooden_slab.0", "minecraft:oak_slab");
        param0.put("minecraft:wooden_slab.1", "minecraft:spruce_slab");
        param0.put("minecraft:wooden_slab.2", "minecraft:birch_slab");
        param0.put("minecraft:wooden_slab.3", "minecraft:jungle_slab");
        param0.put("minecraft:wooden_slab.4", "minecraft:acacia_slab");
        param0.put("minecraft:wooden_slab.5", "minecraft:dark_oak_slab");
        param0.put("minecraft:coal.0", "minecraft:coal");
        param0.put("minecraft:coal.1", "minecraft:charcoal");
        param0.put("minecraft:fish.0", "minecraft:cod");
        param0.put("minecraft:fish.1", "minecraft:salmon");
        param0.put("minecraft:fish.2", "minecraft:clownfish");
        param0.put("minecraft:fish.3", "minecraft:pufferfish");
        param0.put("minecraft:cooked_fish.0", "minecraft:cooked_cod");
        param0.put("minecraft:cooked_fish.1", "minecraft:cooked_salmon");
        param0.put("minecraft:skull.0", "minecraft:skeleton_skull");
        param0.put("minecraft:skull.1", "minecraft:wither_skeleton_skull");
        param0.put("minecraft:skull.2", "minecraft:zombie_head");
        param0.put("minecraft:skull.3", "minecraft:player_head");
        param0.put("minecraft:skull.4", "minecraft:creeper_head");
        param0.put("minecraft:skull.5", "minecraft:dragon_head");
        param0.put("minecraft:golden_apple.0", "minecraft:golden_apple");
        param0.put("minecraft:golden_apple.1", "minecraft:enchanted_golden_apple");
        param0.put("minecraft:fireworks.0", "minecraft:firework_rocket");
        param0.put("minecraft:firework_charge.0", "minecraft:firework_star");
        param0.put("minecraft:dye.0", "minecraft:ink_sac");
        param0.put("minecraft:dye.1", "minecraft:rose_red");
        param0.put("minecraft:dye.2", "minecraft:cactus_green");
        param0.put("minecraft:dye.3", "minecraft:cocoa_beans");
        param0.put("minecraft:dye.4", "minecraft:lapis_lazuli");
        param0.put("minecraft:dye.5", "minecraft:purple_dye");
        param0.put("minecraft:dye.6", "minecraft:cyan_dye");
        param0.put("minecraft:dye.7", "minecraft:light_gray_dye");
        param0.put("minecraft:dye.8", "minecraft:gray_dye");
        param0.put("minecraft:dye.9", "minecraft:pink_dye");
        param0.put("minecraft:dye.10", "minecraft:lime_dye");
        param0.put("minecraft:dye.11", "minecraft:dandelion_yellow");
        param0.put("minecraft:dye.12", "minecraft:light_blue_dye");
        param0.put("minecraft:dye.13", "minecraft:magenta_dye");
        param0.put("minecraft:dye.14", "minecraft:orange_dye");
        param0.put("minecraft:dye.15", "minecraft:bone_meal");
        param0.put("minecraft:silver_shulker_box.0", "minecraft:light_gray_shulker_box");
        param0.put("minecraft:fence.0", "minecraft:oak_fence");
        param0.put("minecraft:fence_gate.0", "minecraft:oak_fence_gate");
        param0.put("minecraft:wooden_door.0", "minecraft:oak_door");
        param0.put("minecraft:boat.0", "minecraft:oak_boat");
        param0.put("minecraft:lit_pumpkin.0", "minecraft:jack_o_lantern");
        param0.put("minecraft:pumpkin.0", "minecraft:carved_pumpkin");
        param0.put("minecraft:trapdoor.0", "minecraft:oak_trapdoor");
        param0.put("minecraft:nether_brick.0", "minecraft:nether_bricks");
        param0.put("minecraft:red_nether_brick.0", "minecraft:red_nether_bricks");
        param0.put("minecraft:netherbrick.0", "minecraft:nether_brick");
        param0.put("minecraft:wooden_button.0", "minecraft:oak_button");
        param0.put("minecraft:wooden_pressure_plate.0", "minecraft:oak_pressure_plate");
        param0.put("minecraft:noteblock.0", "minecraft:note_block");
        param0.put("minecraft:bed.0", "minecraft:white_bed");
        param0.put("minecraft:bed.1", "minecraft:orange_bed");
        param0.put("minecraft:bed.2", "minecraft:magenta_bed");
        param0.put("minecraft:bed.3", "minecraft:light_blue_bed");
        param0.put("minecraft:bed.4", "minecraft:yellow_bed");
        param0.put("minecraft:bed.5", "minecraft:lime_bed");
        param0.put("minecraft:bed.6", "minecraft:pink_bed");
        param0.put("minecraft:bed.7", "minecraft:gray_bed");
        param0.put("minecraft:bed.8", "minecraft:light_gray_bed");
        param0.put("minecraft:bed.9", "minecraft:cyan_bed");
        param0.put("minecraft:bed.10", "minecraft:purple_bed");
        param0.put("minecraft:bed.11", "minecraft:blue_bed");
        param0.put("minecraft:bed.12", "minecraft:brown_bed");
        param0.put("minecraft:bed.13", "minecraft:green_bed");
        param0.put("minecraft:bed.14", "minecraft:red_bed");
        param0.put("minecraft:bed.15", "minecraft:black_bed");
        param0.put("minecraft:banner.15", "minecraft:white_banner");
        param0.put("minecraft:banner.14", "minecraft:orange_banner");
        param0.put("minecraft:banner.13", "minecraft:magenta_banner");
        param0.put("minecraft:banner.12", "minecraft:light_blue_banner");
        param0.put("minecraft:banner.11", "minecraft:yellow_banner");
        param0.put("minecraft:banner.10", "minecraft:lime_banner");
        param0.put("minecraft:banner.9", "minecraft:pink_banner");
        param0.put("minecraft:banner.8", "minecraft:gray_banner");
        param0.put("minecraft:banner.7", "minecraft:light_gray_banner");
        param0.put("minecraft:banner.6", "minecraft:cyan_banner");
        param0.put("minecraft:banner.5", "minecraft:purple_banner");
        param0.put("minecraft:banner.4", "minecraft:blue_banner");
        param0.put("minecraft:banner.3", "minecraft:brown_banner");
        param0.put("minecraft:banner.2", "minecraft:green_banner");
        param0.put("minecraft:banner.1", "minecraft:red_banner");
        param0.put("minecraft:banner.0", "minecraft:black_banner");
        param0.put("minecraft:grass.0", "minecraft:grass_block");
        param0.put("minecraft:brick_block.0", "minecraft:bricks");
        param0.put("minecraft:end_bricks.0", "minecraft:end_stone_bricks");
        param0.put("minecraft:golden_rail.0", "minecraft:powered_rail");
        param0.put("minecraft:magma.0", "minecraft:magma_block");
        param0.put("minecraft:quartz_ore.0", "minecraft:nether_quartz_ore");
        param0.put("minecraft:reeds.0", "minecraft:sugar_cane");
        param0.put("minecraft:slime.0", "minecraft:slime_block");
        param0.put("minecraft:stone_stairs.0", "minecraft:cobblestone_stairs");
        param0.put("minecraft:waterlily.0", "minecraft:lily_pad");
        param0.put("minecraft:web.0", "minecraft:cobweb");
        param0.put("minecraft:snow.0", "minecraft:snow_block");
        param0.put("minecraft:snow_layer.0", "minecraft:snow");
        param0.put("minecraft:record_11.0", "minecraft:music_disc_11");
        param0.put("minecraft:record_13.0", "minecraft:music_disc_13");
        param0.put("minecraft:record_blocks.0", "minecraft:music_disc_blocks");
        param0.put("minecraft:record_cat.0", "minecraft:music_disc_cat");
        param0.put("minecraft:record_chirp.0", "minecraft:music_disc_chirp");
        param0.put("minecraft:record_far.0", "minecraft:music_disc_far");
        param0.put("minecraft:record_mall.0", "minecraft:music_disc_mall");
        param0.put("minecraft:record_mellohi.0", "minecraft:music_disc_mellohi");
        param0.put("minecraft:record_stal.0", "minecraft:music_disc_stal");
        param0.put("minecraft:record_strad.0", "minecraft:music_disc_strad");
        param0.put("minecraft:record_wait.0", "minecraft:music_disc_wait");
        param0.put("minecraft:record_ward.0", "minecraft:music_disc_ward");
    });
    private static final Set<String> IDS = MAP.keySet().stream().map(param0 -> param0.substring(0, param0.indexOf(46))).collect(Collectors.toSet());
    private static final Set<String> DAMAGE_IDS = Sets.newHashSet(
        "minecraft:bow",
        "minecraft:carrot_on_a_stick",
        "minecraft:chainmail_boots",
        "minecraft:chainmail_chestplate",
        "minecraft:chainmail_helmet",
        "minecraft:chainmail_leggings",
        "minecraft:diamond_axe",
        "minecraft:diamond_boots",
        "minecraft:diamond_chestplate",
        "minecraft:diamond_helmet",
        "minecraft:diamond_hoe",
        "minecraft:diamond_leggings",
        "minecraft:diamond_pickaxe",
        "minecraft:diamond_shovel",
        "minecraft:diamond_sword",
        "minecraft:elytra",
        "minecraft:fishing_rod",
        "minecraft:flint_and_steel",
        "minecraft:golden_axe",
        "minecraft:golden_boots",
        "minecraft:golden_chestplate",
        "minecraft:golden_helmet",
        "minecraft:golden_hoe",
        "minecraft:golden_leggings",
        "minecraft:golden_pickaxe",
        "minecraft:golden_shovel",
        "minecraft:golden_sword",
        "minecraft:iron_axe",
        "minecraft:iron_boots",
        "minecraft:iron_chestplate",
        "minecraft:iron_helmet",
        "minecraft:iron_hoe",
        "minecraft:iron_leggings",
        "minecraft:iron_pickaxe",
        "minecraft:iron_shovel",
        "minecraft:iron_sword",
        "minecraft:leather_boots",
        "minecraft:leather_chestplate",
        "minecraft:leather_helmet",
        "minecraft:leather_leggings",
        "minecraft:shears",
        "minecraft:shield",
        "minecraft:stone_axe",
        "minecraft:stone_hoe",
        "minecraft:stone_pickaxe",
        "minecraft:stone_shovel",
        "minecraft:stone_sword",
        "minecraft:wooden_axe",
        "minecraft:wooden_hoe",
        "minecraft:wooden_pickaxe",
        "minecraft:wooden_shovel",
        "minecraft:wooden_sword"
    );

    public ItemStackTheFlatteningFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> var1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), DSL.namespacedString()));
        OpticFinder<?> var2 = var0.findField("tag");
        return this.fixTypeEverywhereTyped("ItemInstanceTheFlatteningFix", var0, param2 -> {
            Optional<Pair<String, String>> var0x = param2.getOptional(var1);
            if (!var0x.isPresent()) {
                return param2;
            } else {
                Typed<?> var1x = param2;
                Dynamic<?> var2x = param2.get(DSL.remainderFinder());
                int var3x = var2x.get("Damage").asInt(0);
                String var4 = updateItem(var0x.get().getSecond(), var3x);
                if (var4 != null) {
                    var1x = param2.set(var1, Pair.of(References.ITEM_NAME.typeName(), var4));
                }

                if (DAMAGE_IDS.contains(var0x.get().getSecond())) {
                    Typed<?> var5 = param2.getOrCreateTyped(var2);
                    Dynamic<?> var6 = var5.get(DSL.remainderFinder());
                    var6 = var6.set("Damage", var6.createInt(var3x));
                    var1x = var1x.set(var2, var5.set(DSL.remainderFinder(), var6));
                }

                return var1x.set(DSL.remainderFinder(), var2x.remove("Damage"));
            }
        });
    }

    @Nullable
    public static String updateItem(@Nullable String param0, int param1) {
        if (IDS.contains(param0)) {
            String var0 = MAP.get(param0 + '.' + param1);
            return var0 == null ? MAP.get(param0 + ".0") : var0;
        } else {
            return null;
        }
    }
}
