package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityBlockStateFix extends DataFix {
    private static final Map<String, Integer> MAP = DataFixUtils.make(Maps.newHashMap(), param0 -> {
        param0.put("minecraft:air", 0);
        param0.put("minecraft:stone", 1);
        param0.put("minecraft:grass", 2);
        param0.put("minecraft:dirt", 3);
        param0.put("minecraft:cobblestone", 4);
        param0.put("minecraft:planks", 5);
        param0.put("minecraft:sapling", 6);
        param0.put("minecraft:bedrock", 7);
        param0.put("minecraft:flowing_water", 8);
        param0.put("minecraft:water", 9);
        param0.put("minecraft:flowing_lava", 10);
        param0.put("minecraft:lava", 11);
        param0.put("minecraft:sand", 12);
        param0.put("minecraft:gravel", 13);
        param0.put("minecraft:gold_ore", 14);
        param0.put("minecraft:iron_ore", 15);
        param0.put("minecraft:coal_ore", 16);
        param0.put("minecraft:log", 17);
        param0.put("minecraft:leaves", 18);
        param0.put("minecraft:sponge", 19);
        param0.put("minecraft:glass", 20);
        param0.put("minecraft:lapis_ore", 21);
        param0.put("minecraft:lapis_block", 22);
        param0.put("minecraft:dispenser", 23);
        param0.put("minecraft:sandstone", 24);
        param0.put("minecraft:noteblock", 25);
        param0.put("minecraft:bed", 26);
        param0.put("minecraft:golden_rail", 27);
        param0.put("minecraft:detector_rail", 28);
        param0.put("minecraft:sticky_piston", 29);
        param0.put("minecraft:web", 30);
        param0.put("minecraft:tallgrass", 31);
        param0.put("minecraft:deadbush", 32);
        param0.put("minecraft:piston", 33);
        param0.put("minecraft:piston_head", 34);
        param0.put("minecraft:wool", 35);
        param0.put("minecraft:piston_extension", 36);
        param0.put("minecraft:yellow_flower", 37);
        param0.put("minecraft:red_flower", 38);
        param0.put("minecraft:brown_mushroom", 39);
        param0.put("minecraft:red_mushroom", 40);
        param0.put("minecraft:gold_block", 41);
        param0.put("minecraft:iron_block", 42);
        param0.put("minecraft:double_stone_slab", 43);
        param0.put("minecraft:stone_slab", 44);
        param0.put("minecraft:brick_block", 45);
        param0.put("minecraft:tnt", 46);
        param0.put("minecraft:bookshelf", 47);
        param0.put("minecraft:mossy_cobblestone", 48);
        param0.put("minecraft:obsidian", 49);
        param0.put("minecraft:torch", 50);
        param0.put("minecraft:fire", 51);
        param0.put("minecraft:mob_spawner", 52);
        param0.put("minecraft:oak_stairs", 53);
        param0.put("minecraft:chest", 54);
        param0.put("minecraft:redstone_wire", 55);
        param0.put("minecraft:diamond_ore", 56);
        param0.put("minecraft:diamond_block", 57);
        param0.put("minecraft:crafting_table", 58);
        param0.put("minecraft:wheat", 59);
        param0.put("minecraft:farmland", 60);
        param0.put("minecraft:furnace", 61);
        param0.put("minecraft:lit_furnace", 62);
        param0.put("minecraft:standing_sign", 63);
        param0.put("minecraft:wooden_door", 64);
        param0.put("minecraft:ladder", 65);
        param0.put("minecraft:rail", 66);
        param0.put("minecraft:stone_stairs", 67);
        param0.put("minecraft:wall_sign", 68);
        param0.put("minecraft:lever", 69);
        param0.put("minecraft:stone_pressure_plate", 70);
        param0.put("minecraft:iron_door", 71);
        param0.put("minecraft:wooden_pressure_plate", 72);
        param0.put("minecraft:redstone_ore", 73);
        param0.put("minecraft:lit_redstone_ore", 74);
        param0.put("minecraft:unlit_redstone_torch", 75);
        param0.put("minecraft:redstone_torch", 76);
        param0.put("minecraft:stone_button", 77);
        param0.put("minecraft:snow_layer", 78);
        param0.put("minecraft:ice", 79);
        param0.put("minecraft:snow", 80);
        param0.put("minecraft:cactus", 81);
        param0.put("minecraft:clay", 82);
        param0.put("minecraft:reeds", 83);
        param0.put("minecraft:jukebox", 84);
        param0.put("minecraft:fence", 85);
        param0.put("minecraft:pumpkin", 86);
        param0.put("minecraft:netherrack", 87);
        param0.put("minecraft:soul_sand", 88);
        param0.put("minecraft:glowstone", 89);
        param0.put("minecraft:portal", 90);
        param0.put("minecraft:lit_pumpkin", 91);
        param0.put("minecraft:cake", 92);
        param0.put("minecraft:unpowered_repeater", 93);
        param0.put("minecraft:powered_repeater", 94);
        param0.put("minecraft:stained_glass", 95);
        param0.put("minecraft:trapdoor", 96);
        param0.put("minecraft:monster_egg", 97);
        param0.put("minecraft:stonebrick", 98);
        param0.put("minecraft:brown_mushroom_block", 99);
        param0.put("minecraft:red_mushroom_block", 100);
        param0.put("minecraft:iron_bars", 101);
        param0.put("minecraft:glass_pane", 102);
        param0.put("minecraft:melon_block", 103);
        param0.put("minecraft:pumpkin_stem", 104);
        param0.put("minecraft:melon_stem", 105);
        param0.put("minecraft:vine", 106);
        param0.put("minecraft:fence_gate", 107);
        param0.put("minecraft:brick_stairs", 108);
        param0.put("minecraft:stone_brick_stairs", 109);
        param0.put("minecraft:mycelium", 110);
        param0.put("minecraft:waterlily", 111);
        param0.put("minecraft:nether_brick", 112);
        param0.put("minecraft:nether_brick_fence", 113);
        param0.put("minecraft:nether_brick_stairs", 114);
        param0.put("minecraft:nether_wart", 115);
        param0.put("minecraft:enchanting_table", 116);
        param0.put("minecraft:brewing_stand", 117);
        param0.put("minecraft:cauldron", 118);
        param0.put("minecraft:end_portal", 119);
        param0.put("minecraft:end_portal_frame", 120);
        param0.put("minecraft:end_stone", 121);
        param0.put("minecraft:dragon_egg", 122);
        param0.put("minecraft:redstone_lamp", 123);
        param0.put("minecraft:lit_redstone_lamp", 124);
        param0.put("minecraft:double_wooden_slab", 125);
        param0.put("minecraft:wooden_slab", 126);
        param0.put("minecraft:cocoa", 127);
        param0.put("minecraft:sandstone_stairs", 128);
        param0.put("minecraft:emerald_ore", 129);
        param0.put("minecraft:ender_chest", 130);
        param0.put("minecraft:tripwire_hook", 131);
        param0.put("minecraft:tripwire", 132);
        param0.put("minecraft:emerald_block", 133);
        param0.put("minecraft:spruce_stairs", 134);
        param0.put("minecraft:birch_stairs", 135);
        param0.put("minecraft:jungle_stairs", 136);
        param0.put("minecraft:command_block", 137);
        param0.put("minecraft:beacon", 138);
        param0.put("minecraft:cobblestone_wall", 139);
        param0.put("minecraft:flower_pot", 140);
        param0.put("minecraft:carrots", 141);
        param0.put("minecraft:potatoes", 142);
        param0.put("minecraft:wooden_button", 143);
        param0.put("minecraft:skull", 144);
        param0.put("minecraft:anvil", 145);
        param0.put("minecraft:trapped_chest", 146);
        param0.put("minecraft:light_weighted_pressure_plate", 147);
        param0.put("minecraft:heavy_weighted_pressure_plate", 148);
        param0.put("minecraft:unpowered_comparator", 149);
        param0.put("minecraft:powered_comparator", 150);
        param0.put("minecraft:daylight_detector", 151);
        param0.put("minecraft:redstone_block", 152);
        param0.put("minecraft:quartz_ore", 153);
        param0.put("minecraft:hopper", 154);
        param0.put("minecraft:quartz_block", 155);
        param0.put("minecraft:quartz_stairs", 156);
        param0.put("minecraft:activator_rail", 157);
        param0.put("minecraft:dropper", 158);
        param0.put("minecraft:stained_hardened_clay", 159);
        param0.put("minecraft:stained_glass_pane", 160);
        param0.put("minecraft:leaves2", 161);
        param0.put("minecraft:log2", 162);
        param0.put("minecraft:acacia_stairs", 163);
        param0.put("minecraft:dark_oak_stairs", 164);
        param0.put("minecraft:slime", 165);
        param0.put("minecraft:barrier", 166);
        param0.put("minecraft:iron_trapdoor", 167);
        param0.put("minecraft:prismarine", 168);
        param0.put("minecraft:sea_lantern", 169);
        param0.put("minecraft:hay_block", 170);
        param0.put("minecraft:carpet", 171);
        param0.put("minecraft:hardened_clay", 172);
        param0.put("minecraft:coal_block", 173);
        param0.put("minecraft:packed_ice", 174);
        param0.put("minecraft:double_plant", 175);
        param0.put("minecraft:standing_banner", 176);
        param0.put("minecraft:wall_banner", 177);
        param0.put("minecraft:daylight_detector_inverted", 178);
        param0.put("minecraft:red_sandstone", 179);
        param0.put("minecraft:red_sandstone_stairs", 180);
        param0.put("minecraft:double_stone_slab2", 181);
        param0.put("minecraft:stone_slab2", 182);
        param0.put("minecraft:spruce_fence_gate", 183);
        param0.put("minecraft:birch_fence_gate", 184);
        param0.put("minecraft:jungle_fence_gate", 185);
        param0.put("minecraft:dark_oak_fence_gate", 186);
        param0.put("minecraft:acacia_fence_gate", 187);
        param0.put("minecraft:spruce_fence", 188);
        param0.put("minecraft:birch_fence", 189);
        param0.put("minecraft:jungle_fence", 190);
        param0.put("minecraft:dark_oak_fence", 191);
        param0.put("minecraft:acacia_fence", 192);
        param0.put("minecraft:spruce_door", 193);
        param0.put("minecraft:birch_door", 194);
        param0.put("minecraft:jungle_door", 195);
        param0.put("minecraft:acacia_door", 196);
        param0.put("minecraft:dark_oak_door", 197);
        param0.put("minecraft:end_rod", 198);
        param0.put("minecraft:chorus_plant", 199);
        param0.put("minecraft:chorus_flower", 200);
        param0.put("minecraft:purpur_block", 201);
        param0.put("minecraft:purpur_pillar", 202);
        param0.put("minecraft:purpur_stairs", 203);
        param0.put("minecraft:purpur_double_slab", 204);
        param0.put("minecraft:purpur_slab", 205);
        param0.put("minecraft:end_bricks", 206);
        param0.put("minecraft:beetroots", 207);
        param0.put("minecraft:grass_path", 208);
        param0.put("minecraft:end_gateway", 209);
        param0.put("minecraft:repeating_command_block", 210);
        param0.put("minecraft:chain_command_block", 211);
        param0.put("minecraft:frosted_ice", 212);
        param0.put("minecraft:magma", 213);
        param0.put("minecraft:nether_wart_block", 214);
        param0.put("minecraft:red_nether_brick", 215);
        param0.put("minecraft:bone_block", 216);
        param0.put("minecraft:structure_void", 217);
        param0.put("minecraft:observer", 218);
        param0.put("minecraft:white_shulker_box", 219);
        param0.put("minecraft:orange_shulker_box", 220);
        param0.put("minecraft:magenta_shulker_box", 221);
        param0.put("minecraft:light_blue_shulker_box", 222);
        param0.put("minecraft:yellow_shulker_box", 223);
        param0.put("minecraft:lime_shulker_box", 224);
        param0.put("minecraft:pink_shulker_box", 225);
        param0.put("minecraft:gray_shulker_box", 226);
        param0.put("minecraft:silver_shulker_box", 227);
        param0.put("minecraft:cyan_shulker_box", 228);
        param0.put("minecraft:purple_shulker_box", 229);
        param0.put("minecraft:blue_shulker_box", 230);
        param0.put("minecraft:brown_shulker_box", 231);
        param0.put("minecraft:green_shulker_box", 232);
        param0.put("minecraft:red_shulker_box", 233);
        param0.put("minecraft:black_shulker_box", 234);
        param0.put("minecraft:white_glazed_terracotta", 235);
        param0.put("minecraft:orange_glazed_terracotta", 236);
        param0.put("minecraft:magenta_glazed_terracotta", 237);
        param0.put("minecraft:light_blue_glazed_terracotta", 238);
        param0.put("minecraft:yellow_glazed_terracotta", 239);
        param0.put("minecraft:lime_glazed_terracotta", 240);
        param0.put("minecraft:pink_glazed_terracotta", 241);
        param0.put("minecraft:gray_glazed_terracotta", 242);
        param0.put("minecraft:silver_glazed_terracotta", 243);
        param0.put("minecraft:cyan_glazed_terracotta", 244);
        param0.put("minecraft:purple_glazed_terracotta", 245);
        param0.put("minecraft:blue_glazed_terracotta", 246);
        param0.put("minecraft:brown_glazed_terracotta", 247);
        param0.put("minecraft:green_glazed_terracotta", 248);
        param0.put("minecraft:red_glazed_terracotta", 249);
        param0.put("minecraft:black_glazed_terracotta", 250);
        param0.put("minecraft:concrete", 251);
        param0.put("minecraft:concrete_powder", 252);
        param0.put("minecraft:structure_block", 255);
    });

    public EntityBlockStateFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    public static int getBlockId(String param0) {
        Integer var0 = MAP.get(param0);
        return var0 == null ? 0 : var0;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Schema var0 = this.getInputSchema();
        Schema var1 = this.getOutputSchema();
        Function<Typed<?>, Typed<?>> var2 = param0 -> this.updateBlockToBlockState(param0, "DisplayTile", "DisplayData", "DisplayState");
        Function<Typed<?>, Typed<?>> var3 = param0 -> this.updateBlockToBlockState(param0, "inTile", "inData", "inBlockState");
        Type<Pair<Either<Pair<String, Either<Integer, String>>, Unit>, Dynamic<?>>> var4 = DSL.and(
            DSL.optional(DSL.field("inTile", DSL.named(References.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), NamespacedSchema.namespacedString())))),
            DSL.remainderType()
        );
        Function<Typed<?>, Typed<?>> var5 = param1 -> param1.update(var4.finder(), DSL.remainderType(), Pair::getSecond);
        return this.fixTypeEverywhereTyped(
            "EntityBlockStateFix",
            var0.getType(References.ENTITY),
            var1.getType(References.ENTITY),
            param3 -> {
                param3 = this.updateEntity(param3, "minecraft:falling_block", this::updateFallingBlock);
                param3 = this.updateEntity(
                    param3, "minecraft:enderman", param0x -> this.updateBlockToBlockState(param0x, "carried", "carriedData", "carriedBlockState")
                );
                param3 = this.updateEntity(param3, "minecraft:arrow", var3);
                param3 = this.updateEntity(param3, "minecraft:spectral_arrow", var3);
                param3 = this.updateEntity(param3, "minecraft:egg", var5);
                param3 = this.updateEntity(param3, "minecraft:ender_pearl", var5);
                param3 = this.updateEntity(param3, "minecraft:fireball", var5);
                param3 = this.updateEntity(param3, "minecraft:potion", var5);
                param3 = this.updateEntity(param3, "minecraft:small_fireball", var5);
                param3 = this.updateEntity(param3, "minecraft:snowball", var5);
                param3 = this.updateEntity(param3, "minecraft:wither_skull", var5);
                param3 = this.updateEntity(param3, "minecraft:xp_bottle", var5);
                param3 = this.updateEntity(param3, "minecraft:commandblock_minecart", var2);
                param3 = this.updateEntity(param3, "minecraft:minecart", var2);
                param3 = this.updateEntity(param3, "minecraft:chest_minecart", var2);
                param3 = this.updateEntity(param3, "minecraft:furnace_minecart", var2);
                param3 = this.updateEntity(param3, "minecraft:tnt_minecart", var2);
                param3 = this.updateEntity(param3, "minecraft:hopper_minecart", var2);
                return this.updateEntity(param3, "minecraft:spawner_minecart", var2);
            }
        );
    }

    private Typed<?> updateFallingBlock(Typed<?> param0) {
        Type<Either<Pair<String, Either<Integer, String>>, Unit>> var0 = DSL.optional(
            DSL.field("Block", DSL.named(References.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), NamespacedSchema.namespacedString())))
        );
        Type<Either<Pair<String, Dynamic<?>>, Unit>> var1 = DSL.optional(
            DSL.field("BlockState", DSL.named(References.BLOCK_STATE.typeName(), DSL.remainderType()))
        );
        Dynamic<?> var2 = param0.get(DSL.remainderFinder());
        return param0.update(var0.finder(), var1, param1 -> {
            int var0x = param1.map(param0x -> param0x.getSecond().map(param0xx -> param0xx, EntityBlockStateFix::getBlockId), param1x -> {
                Optional<Number> var0xx = var2.get("TileID").asNumber().result();
                return var0xx.map(Number::intValue).orElseGet(() -> var2.get("Tile").asByte((byte)0) & 0xFF);
            });
            int var1x = var2.get("Data").asInt(0) & 15;
            return Either.left(Pair.of(References.BLOCK_STATE.typeName(), BlockStateData.getTag(var0x << 4 | var1x)));
        }).set(DSL.remainderFinder(), var2.remove("Data").remove("TileID").remove("Tile"));
    }

    private Typed<?> updateBlockToBlockState(Typed<?> param0, String param1, String param2, String param3) {
        Type<Pair<String, Either<Integer, String>>> var0 = DSL.field(
            param1, DSL.named(References.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), NamespacedSchema.namespacedString()))
        );
        Type<Pair<String, Dynamic<?>>> var1 = DSL.field(param3, DSL.named(References.BLOCK_STATE.typeName(), DSL.remainderType()));
        Dynamic<?> var2 = param0.getOrCreate(DSL.remainderFinder());
        return param0.update(var0.finder(), var1, param2x -> {
            int var0x = param2x.getSecond().map(param0x -> param0x, EntityBlockStateFix::getBlockId);
            int var1x = var2.get(param2).asInt(0) & 15;
            return Pair.of(References.BLOCK_STATE.typeName(), BlockStateData.getTag(var0x << 4 | var1x));
        }).set(DSL.remainderFinder(), var2.remove(param2));
    }

    private Typed<?> updateEntity(Typed<?> param0, String param1, Function<Typed<?>, Typed<?>> param2) {
        Type<?> var0 = this.getInputSchema().getChoiceType(References.ENTITY, param1);
        Type<?> var1 = this.getOutputSchema().getChoiceType(References.ENTITY, param1);
        return param0.updateTyped(DSL.namedChoice(param1, var0), var1, param2);
    }
}
