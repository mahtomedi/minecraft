package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemIdFix extends DataFix {
    private static final Int2ObjectMap<String> ITEM_NAMES = DataFixUtils.make(new Int2ObjectOpenHashMap<>(), param0 -> {
        param0.put(1, "minecraft:stone");
        param0.put(2, "minecraft:grass");
        param0.put(3, "minecraft:dirt");
        param0.put(4, "minecraft:cobblestone");
        param0.put(5, "minecraft:planks");
        param0.put(6, "minecraft:sapling");
        param0.put(7, "minecraft:bedrock");
        param0.put(8, "minecraft:flowing_water");
        param0.put(9, "minecraft:water");
        param0.put(10, "minecraft:flowing_lava");
        param0.put(11, "minecraft:lava");
        param0.put(12, "minecraft:sand");
        param0.put(13, "minecraft:gravel");
        param0.put(14, "minecraft:gold_ore");
        param0.put(15, "minecraft:iron_ore");
        param0.put(16, "minecraft:coal_ore");
        param0.put(17, "minecraft:log");
        param0.put(18, "minecraft:leaves");
        param0.put(19, "minecraft:sponge");
        param0.put(20, "minecraft:glass");
        param0.put(21, "minecraft:lapis_ore");
        param0.put(22, "minecraft:lapis_block");
        param0.put(23, "minecraft:dispenser");
        param0.put(24, "minecraft:sandstone");
        param0.put(25, "minecraft:noteblock");
        param0.put(27, "minecraft:golden_rail");
        param0.put(28, "minecraft:detector_rail");
        param0.put(29, "minecraft:sticky_piston");
        param0.put(30, "minecraft:web");
        param0.put(31, "minecraft:tallgrass");
        param0.put(32, "minecraft:deadbush");
        param0.put(33, "minecraft:piston");
        param0.put(35, "minecraft:wool");
        param0.put(37, "minecraft:yellow_flower");
        param0.put(38, "minecraft:red_flower");
        param0.put(39, "minecraft:brown_mushroom");
        param0.put(40, "minecraft:red_mushroom");
        param0.put(41, "minecraft:gold_block");
        param0.put(42, "minecraft:iron_block");
        param0.put(43, "minecraft:double_stone_slab");
        param0.put(44, "minecraft:stone_slab");
        param0.put(45, "minecraft:brick_block");
        param0.put(46, "minecraft:tnt");
        param0.put(47, "minecraft:bookshelf");
        param0.put(48, "minecraft:mossy_cobblestone");
        param0.put(49, "minecraft:obsidian");
        param0.put(50, "minecraft:torch");
        param0.put(51, "minecraft:fire");
        param0.put(52, "minecraft:mob_spawner");
        param0.put(53, "minecraft:oak_stairs");
        param0.put(54, "minecraft:chest");
        param0.put(56, "minecraft:diamond_ore");
        param0.put(57, "minecraft:diamond_block");
        param0.put(58, "minecraft:crafting_table");
        param0.put(60, "minecraft:farmland");
        param0.put(61, "minecraft:furnace");
        param0.put(62, "minecraft:lit_furnace");
        param0.put(65, "minecraft:ladder");
        param0.put(66, "minecraft:rail");
        param0.put(67, "minecraft:stone_stairs");
        param0.put(69, "minecraft:lever");
        param0.put(70, "minecraft:stone_pressure_plate");
        param0.put(72, "minecraft:wooden_pressure_plate");
        param0.put(73, "minecraft:redstone_ore");
        param0.put(76, "minecraft:redstone_torch");
        param0.put(77, "minecraft:stone_button");
        param0.put(78, "minecraft:snow_layer");
        param0.put(79, "minecraft:ice");
        param0.put(80, "minecraft:snow");
        param0.put(81, "minecraft:cactus");
        param0.put(82, "minecraft:clay");
        param0.put(84, "minecraft:jukebox");
        param0.put(85, "minecraft:fence");
        param0.put(86, "minecraft:pumpkin");
        param0.put(87, "minecraft:netherrack");
        param0.put(88, "minecraft:soul_sand");
        param0.put(89, "minecraft:glowstone");
        param0.put(90, "minecraft:portal");
        param0.put(91, "minecraft:lit_pumpkin");
        param0.put(95, "minecraft:stained_glass");
        param0.put(96, "minecraft:trapdoor");
        param0.put(97, "minecraft:monster_egg");
        param0.put(98, "minecraft:stonebrick");
        param0.put(99, "minecraft:brown_mushroom_block");
        param0.put(100, "minecraft:red_mushroom_block");
        param0.put(101, "minecraft:iron_bars");
        param0.put(102, "minecraft:glass_pane");
        param0.put(103, "minecraft:melon_block");
        param0.put(106, "minecraft:vine");
        param0.put(107, "minecraft:fence_gate");
        param0.put(108, "minecraft:brick_stairs");
        param0.put(109, "minecraft:stone_brick_stairs");
        param0.put(110, "minecraft:mycelium");
        param0.put(111, "minecraft:waterlily");
        param0.put(112, "minecraft:nether_brick");
        param0.put(113, "minecraft:nether_brick_fence");
        param0.put(114, "minecraft:nether_brick_stairs");
        param0.put(116, "minecraft:enchanting_table");
        param0.put(119, "minecraft:end_portal");
        param0.put(120, "minecraft:end_portal_frame");
        param0.put(121, "minecraft:end_stone");
        param0.put(122, "minecraft:dragon_egg");
        param0.put(123, "minecraft:redstone_lamp");
        param0.put(125, "minecraft:double_wooden_slab");
        param0.put(126, "minecraft:wooden_slab");
        param0.put(127, "minecraft:cocoa");
        param0.put(128, "minecraft:sandstone_stairs");
        param0.put(129, "minecraft:emerald_ore");
        param0.put(130, "minecraft:ender_chest");
        param0.put(131, "minecraft:tripwire_hook");
        param0.put(133, "minecraft:emerald_block");
        param0.put(134, "minecraft:spruce_stairs");
        param0.put(135, "minecraft:birch_stairs");
        param0.put(136, "minecraft:jungle_stairs");
        param0.put(137, "minecraft:command_block");
        param0.put(138, "minecraft:beacon");
        param0.put(139, "minecraft:cobblestone_wall");
        param0.put(141, "minecraft:carrots");
        param0.put(142, "minecraft:potatoes");
        param0.put(143, "minecraft:wooden_button");
        param0.put(145, "minecraft:anvil");
        param0.put(146, "minecraft:trapped_chest");
        param0.put(147, "minecraft:light_weighted_pressure_plate");
        param0.put(148, "minecraft:heavy_weighted_pressure_plate");
        param0.put(151, "minecraft:daylight_detector");
        param0.put(152, "minecraft:redstone_block");
        param0.put(153, "minecraft:quartz_ore");
        param0.put(154, "minecraft:hopper");
        param0.put(155, "minecraft:quartz_block");
        param0.put(156, "minecraft:quartz_stairs");
        param0.put(157, "minecraft:activator_rail");
        param0.put(158, "minecraft:dropper");
        param0.put(159, "minecraft:stained_hardened_clay");
        param0.put(160, "minecraft:stained_glass_pane");
        param0.put(161, "minecraft:leaves2");
        param0.put(162, "minecraft:log2");
        param0.put(163, "minecraft:acacia_stairs");
        param0.put(164, "minecraft:dark_oak_stairs");
        param0.put(170, "minecraft:hay_block");
        param0.put(171, "minecraft:carpet");
        param0.put(172, "minecraft:hardened_clay");
        param0.put(173, "minecraft:coal_block");
        param0.put(174, "minecraft:packed_ice");
        param0.put(175, "minecraft:double_plant");
        param0.put(256, "minecraft:iron_shovel");
        param0.put(257, "minecraft:iron_pickaxe");
        param0.put(258, "minecraft:iron_axe");
        param0.put(259, "minecraft:flint_and_steel");
        param0.put(260, "minecraft:apple");
        param0.put(261, "minecraft:bow");
        param0.put(262, "minecraft:arrow");
        param0.put(263, "minecraft:coal");
        param0.put(264, "minecraft:diamond");
        param0.put(265, "minecraft:iron_ingot");
        param0.put(266, "minecraft:gold_ingot");
        param0.put(267, "minecraft:iron_sword");
        param0.put(268, "minecraft:wooden_sword");
        param0.put(269, "minecraft:wooden_shovel");
        param0.put(270, "minecraft:wooden_pickaxe");
        param0.put(271, "minecraft:wooden_axe");
        param0.put(272, "minecraft:stone_sword");
        param0.put(273, "minecraft:stone_shovel");
        param0.put(274, "minecraft:stone_pickaxe");
        param0.put(275, "minecraft:stone_axe");
        param0.put(276, "minecraft:diamond_sword");
        param0.put(277, "minecraft:diamond_shovel");
        param0.put(278, "minecraft:diamond_pickaxe");
        param0.put(279, "minecraft:diamond_axe");
        param0.put(280, "minecraft:stick");
        param0.put(281, "minecraft:bowl");
        param0.put(282, "minecraft:mushroom_stew");
        param0.put(283, "minecraft:golden_sword");
        param0.put(284, "minecraft:golden_shovel");
        param0.put(285, "minecraft:golden_pickaxe");
        param0.put(286, "minecraft:golden_axe");
        param0.put(287, "minecraft:string");
        param0.put(288, "minecraft:feather");
        param0.put(289, "minecraft:gunpowder");
        param0.put(290, "minecraft:wooden_hoe");
        param0.put(291, "minecraft:stone_hoe");
        param0.put(292, "minecraft:iron_hoe");
        param0.put(293, "minecraft:diamond_hoe");
        param0.put(294, "minecraft:golden_hoe");
        param0.put(295, "minecraft:wheat_seeds");
        param0.put(296, "minecraft:wheat");
        param0.put(297, "minecraft:bread");
        param0.put(298, "minecraft:leather_helmet");
        param0.put(299, "minecraft:leather_chestplate");
        param0.put(300, "minecraft:leather_leggings");
        param0.put(301, "minecraft:leather_boots");
        param0.put(302, "minecraft:chainmail_helmet");
        param0.put(303, "minecraft:chainmail_chestplate");
        param0.put(304, "minecraft:chainmail_leggings");
        param0.put(305, "minecraft:chainmail_boots");
        param0.put(306, "minecraft:iron_helmet");
        param0.put(307, "minecraft:iron_chestplate");
        param0.put(308, "minecraft:iron_leggings");
        param0.put(309, "minecraft:iron_boots");
        param0.put(310, "minecraft:diamond_helmet");
        param0.put(311, "minecraft:diamond_chestplate");
        param0.put(312, "minecraft:diamond_leggings");
        param0.put(313, "minecraft:diamond_boots");
        param0.put(314, "minecraft:golden_helmet");
        param0.put(315, "minecraft:golden_chestplate");
        param0.put(316, "minecraft:golden_leggings");
        param0.put(317, "minecraft:golden_boots");
        param0.put(318, "minecraft:flint");
        param0.put(319, "minecraft:porkchop");
        param0.put(320, "minecraft:cooked_porkchop");
        param0.put(321, "minecraft:painting");
        param0.put(322, "minecraft:golden_apple");
        param0.put(323, "minecraft:sign");
        param0.put(324, "minecraft:wooden_door");
        param0.put(325, "minecraft:bucket");
        param0.put(326, "minecraft:water_bucket");
        param0.put(327, "minecraft:lava_bucket");
        param0.put(328, "minecraft:minecart");
        param0.put(329, "minecraft:saddle");
        param0.put(330, "minecraft:iron_door");
        param0.put(331, "minecraft:redstone");
        param0.put(332, "minecraft:snowball");
        param0.put(333, "minecraft:boat");
        param0.put(334, "minecraft:leather");
        param0.put(335, "minecraft:milk_bucket");
        param0.put(336, "minecraft:brick");
        param0.put(337, "minecraft:clay_ball");
        param0.put(338, "minecraft:reeds");
        param0.put(339, "minecraft:paper");
        param0.put(340, "minecraft:book");
        param0.put(341, "minecraft:slime_ball");
        param0.put(342, "minecraft:chest_minecart");
        param0.put(343, "minecraft:furnace_minecart");
        param0.put(344, "minecraft:egg");
        param0.put(345, "minecraft:compass");
        param0.put(346, "minecraft:fishing_rod");
        param0.put(347, "minecraft:clock");
        param0.put(348, "minecraft:glowstone_dust");
        param0.put(349, "minecraft:fish");
        param0.put(350, "minecraft:cooked_fished");
        param0.put(351, "minecraft:dye");
        param0.put(352, "minecraft:bone");
        param0.put(353, "minecraft:sugar");
        param0.put(354, "minecraft:cake");
        param0.put(355, "minecraft:bed");
        param0.put(356, "minecraft:repeater");
        param0.put(357, "minecraft:cookie");
        param0.put(358, "minecraft:filled_map");
        param0.put(359, "minecraft:shears");
        param0.put(360, "minecraft:melon");
        param0.put(361, "minecraft:pumpkin_seeds");
        param0.put(362, "minecraft:melon_seeds");
        param0.put(363, "minecraft:beef");
        param0.put(364, "minecraft:cooked_beef");
        param0.put(365, "minecraft:chicken");
        param0.put(366, "minecraft:cooked_chicken");
        param0.put(367, "minecraft:rotten_flesh");
        param0.put(368, "minecraft:ender_pearl");
        param0.put(369, "minecraft:blaze_rod");
        param0.put(370, "minecraft:ghast_tear");
        param0.put(371, "minecraft:gold_nugget");
        param0.put(372, "minecraft:nether_wart");
        param0.put(373, "minecraft:potion");
        param0.put(374, "minecraft:glass_bottle");
        param0.put(375, "minecraft:spider_eye");
        param0.put(376, "minecraft:fermented_spider_eye");
        param0.put(377, "minecraft:blaze_powder");
        param0.put(378, "minecraft:magma_cream");
        param0.put(379, "minecraft:brewing_stand");
        param0.put(380, "minecraft:cauldron");
        param0.put(381, "minecraft:ender_eye");
        param0.put(382, "minecraft:speckled_melon");
        param0.put(383, "minecraft:spawn_egg");
        param0.put(384, "minecraft:experience_bottle");
        param0.put(385, "minecraft:fire_charge");
        param0.put(386, "minecraft:writable_book");
        param0.put(387, "minecraft:written_book");
        param0.put(388, "minecraft:emerald");
        param0.put(389, "minecraft:item_frame");
        param0.put(390, "minecraft:flower_pot");
        param0.put(391, "minecraft:carrot");
        param0.put(392, "minecraft:potato");
        param0.put(393, "minecraft:baked_potato");
        param0.put(394, "minecraft:poisonous_potato");
        param0.put(395, "minecraft:map");
        param0.put(396, "minecraft:golden_carrot");
        param0.put(397, "minecraft:skull");
        param0.put(398, "minecraft:carrot_on_a_stick");
        param0.put(399, "minecraft:nether_star");
        param0.put(400, "minecraft:pumpkin_pie");
        param0.put(401, "minecraft:fireworks");
        param0.put(402, "minecraft:firework_charge");
        param0.put(403, "minecraft:enchanted_book");
        param0.put(404, "minecraft:comparator");
        param0.put(405, "minecraft:netherbrick");
        param0.put(406, "minecraft:quartz");
        param0.put(407, "minecraft:tnt_minecart");
        param0.put(408, "minecraft:hopper_minecart");
        param0.put(417, "minecraft:iron_horse_armor");
        param0.put(418, "minecraft:golden_horse_armor");
        param0.put(419, "minecraft:diamond_horse_armor");
        param0.put(420, "minecraft:lead");
        param0.put(421, "minecraft:name_tag");
        param0.put(422, "minecraft:command_block_minecart");
        param0.put(2256, "minecraft:record_13");
        param0.put(2257, "minecraft:record_cat");
        param0.put(2258, "minecraft:record_blocks");
        param0.put(2259, "minecraft:record_chirp");
        param0.put(2260, "minecraft:record_far");
        param0.put(2261, "minecraft:record_mall");
        param0.put(2262, "minecraft:record_mellohi");
        param0.put(2263, "minecraft:record_stal");
        param0.put(2264, "minecraft:record_strad");
        param0.put(2265, "minecraft:record_ward");
        param0.put(2266, "minecraft:record_11");
        param0.put(2267, "minecraft:record_wait");
        param0.defaultReturnValue("minecraft:air");
    });

    public ItemIdFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    public static String getItem(int param0) {
        return ITEM_NAMES.get(param0);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<Either<Integer, Pair<String, String>>> var0 = DSL.or(
            DSL.intType(), DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString())
        );
        Type<Pair<String, String>> var1 = DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString());
        OpticFinder<Either<Integer, Pair<String, String>>> var2 = DSL.fieldFinder("id", var0);
        return this.fixTypeEverywhereTyped(
            "ItemIdFix",
            this.getInputSchema().getType(References.ITEM_STACK),
            this.getOutputSchema().getType(References.ITEM_STACK),
            param2 -> param2.update(
                    var2,
                    var1,
                    param0x -> param0x.map(
                            param0xx -> Pair.of(References.ITEM_NAME.typeName(), getItem(param0xx)),
                            (Function<? super Pair<String, String>, ? extends Pair<String, String>>)(param0xx -> param0xx)
                        )
                )
        );
    }
}
