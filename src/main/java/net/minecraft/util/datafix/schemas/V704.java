package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V704 extends Schema {
    protected static final Map<String, String> ITEM_TO_BLOCKENTITY = DataFixUtils.make(() -> {
        Map<String, String> var0 = Maps.newHashMap();
        var0.put("minecraft:furnace", "minecraft:furnace");
        var0.put("minecraft:lit_furnace", "minecraft:furnace");
        var0.put("minecraft:chest", "minecraft:chest");
        var0.put("minecraft:trapped_chest", "minecraft:chest");
        var0.put("minecraft:ender_chest", "minecraft:ender_chest");
        var0.put("minecraft:jukebox", "minecraft:jukebox");
        var0.put("minecraft:dispenser", "minecraft:dispenser");
        var0.put("minecraft:dropper", "minecraft:dropper");
        var0.put("minecraft:sign", "minecraft:sign");
        var0.put("minecraft:mob_spawner", "minecraft:mob_spawner");
        var0.put("minecraft:spawner", "minecraft:mob_spawner");
        var0.put("minecraft:noteblock", "minecraft:noteblock");
        var0.put("minecraft:brewing_stand", "minecraft:brewing_stand");
        var0.put("minecraft:enhanting_table", "minecraft:enchanting_table");
        var0.put("minecraft:command_block", "minecraft:command_block");
        var0.put("minecraft:beacon", "minecraft:beacon");
        var0.put("minecraft:skull", "minecraft:skull");
        var0.put("minecraft:daylight_detector", "minecraft:daylight_detector");
        var0.put("minecraft:hopper", "minecraft:hopper");
        var0.put("minecraft:banner", "minecraft:banner");
        var0.put("minecraft:flower_pot", "minecraft:flower_pot");
        var0.put("minecraft:repeating_command_block", "minecraft:command_block");
        var0.put("minecraft:chain_command_block", "minecraft:command_block");
        var0.put("minecraft:shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:white_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:orange_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:magenta_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:light_blue_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:yellow_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:lime_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:pink_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:gray_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:silver_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:cyan_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:purple_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:blue_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:brown_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:green_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:red_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:black_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:bed", "minecraft:bed");
        var0.put("minecraft:light_gray_shulker_box", "minecraft:shulker_box");
        var0.put("minecraft:banner", "minecraft:banner");
        var0.put("minecraft:white_banner", "minecraft:banner");
        var0.put("minecraft:orange_banner", "minecraft:banner");
        var0.put("minecraft:magenta_banner", "minecraft:banner");
        var0.put("minecraft:light_blue_banner", "minecraft:banner");
        var0.put("minecraft:yellow_banner", "minecraft:banner");
        var0.put("minecraft:lime_banner", "minecraft:banner");
        var0.put("minecraft:pink_banner", "minecraft:banner");
        var0.put("minecraft:gray_banner", "minecraft:banner");
        var0.put("minecraft:silver_banner", "minecraft:banner");
        var0.put("minecraft:light_gray_banner", "minecraft:banner");
        var0.put("minecraft:cyan_banner", "minecraft:banner");
        var0.put("minecraft:purple_banner", "minecraft:banner");
        var0.put("minecraft:blue_banner", "minecraft:banner");
        var0.put("minecraft:brown_banner", "minecraft:banner");
        var0.put("minecraft:green_banner", "minecraft:banner");
        var0.put("minecraft:red_banner", "minecraft:banner");
        var0.put("minecraft:black_banner", "minecraft:banner");
        var0.put("minecraft:standing_sign", "minecraft:sign");
        var0.put("minecraft:wall_sign", "minecraft:sign");
        var0.put("minecraft:piston_head", "minecraft:piston");
        var0.put("minecraft:daylight_detector_inverted", "minecraft:daylight_detector");
        var0.put("minecraft:unpowered_comparator", "minecraft:comparator");
        var0.put("minecraft:powered_comparator", "minecraft:comparator");
        var0.put("minecraft:wall_banner", "minecraft:banner");
        var0.put("minecraft:standing_banner", "minecraft:banner");
        var0.put("minecraft:structure_block", "minecraft:structure_block");
        var0.put("minecraft:end_portal", "minecraft:end_portal");
        var0.put("minecraft:end_gateway", "minecraft:end_gateway");
        var0.put("minecraft:sign", "minecraft:sign");
        var0.put("minecraft:shield", "minecraft:banner");
        var0.put("minecraft:white_bed", "minecraft:bed");
        var0.put("minecraft:orange_bed", "minecraft:bed");
        var0.put("minecraft:magenta_bed", "minecraft:bed");
        var0.put("minecraft:light_blue_bed", "minecraft:bed");
        var0.put("minecraft:yellow_bed", "minecraft:bed");
        var0.put("minecraft:lime_bed", "minecraft:bed");
        var0.put("minecraft:pink_bed", "minecraft:bed");
        var0.put("minecraft:gray_bed", "minecraft:bed");
        var0.put("minecraft:silver_bed", "minecraft:bed");
        var0.put("minecraft:light_gray_bed", "minecraft:bed");
        var0.put("minecraft:cyan_bed", "minecraft:bed");
        var0.put("minecraft:purple_bed", "minecraft:bed");
        var0.put("minecraft:blue_bed", "minecraft:bed");
        var0.put("minecraft:brown_bed", "minecraft:bed");
        var0.put("minecraft:green_bed", "minecraft:bed");
        var0.put("minecraft:red_bed", "minecraft:bed");
        var0.put("minecraft:black_bed", "minecraft:bed");
        var0.put("minecraft:oak_sign", "minecraft:sign");
        var0.put("minecraft:spruce_sign", "minecraft:sign");
        var0.put("minecraft:birch_sign", "minecraft:sign");
        var0.put("minecraft:jungle_sign", "minecraft:sign");
        var0.put("minecraft:acacia_sign", "minecraft:sign");
        var0.put("minecraft:dark_oak_sign", "minecraft:sign");
        var0.put("minecraft:crimson_sign", "minecraft:sign");
        var0.put("minecraft:warped_sign", "minecraft:sign");
        var0.put("minecraft:skeleton_skull", "minecraft:skull");
        var0.put("minecraft:wither_skeleton_skull", "minecraft:skull");
        var0.put("minecraft:zombie_head", "minecraft:skull");
        var0.put("minecraft:player_head", "minecraft:skull");
        var0.put("minecraft:creeper_head", "minecraft:skull");
        var0.put("minecraft:dragon_head", "minecraft:skull");
        var0.put("minecraft:barrel", "minecraft:barrel");
        var0.put("minecraft:conduit", "minecraft:conduit");
        var0.put("minecraft:smoker", "minecraft:smoker");
        var0.put("minecraft:blast_furnace", "minecraft:blast_furnace");
        var0.put("minecraft:lectern", "minecraft:lectern");
        var0.put("minecraft:bell", "minecraft:bell");
        var0.put("minecraft:jigsaw", "minecraft:jigsaw");
        var0.put("minecraft:campfire", "minecraft:campfire");
        var0.put("minecraft:bee_nest", "minecraft:beehive");
        var0.put("minecraft:beehive", "minecraft:beehive");
        var0.put("minecraft:sculk_sensor", "minecraft:sculk_sensor");
        var0.put("minecraft:decorated_pot", "minecraft:decorated_pot");
        return ImmutableMap.copyOf(var0);
    });
    protected static final HookFunction ADD_NAMES = new HookFunction() {
        @Override
        public <T> T apply(DynamicOps<T> param0, T param1) {
            return V99.addNames(new Dynamic<>(param0, param1), V704.ITEM_TO_BLOCKENTITY, "ArmorStand");
        }
    };

    public V704(int param0, Schema param1) {
        super(param0, param1);
    }

    protected static void registerInventory(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(param0))));
    }

    @Override
    public Type<?> getChoiceType(TypeReference param0, String param1) {
        return Objects.equals(param0.typeName(), References.BLOCK_ENTITY.typeName())
            ? super.getChoiceType(param0, NamespacedSchema.ensureNamespaced(param1))
            : super.getChoiceType(param0, param1);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = Maps.newHashMap();
        registerInventory(param0, var0, "minecraft:furnace");
        registerInventory(param0, var0, "minecraft:chest");
        param0.registerSimple(var0, "minecraft:ender_chest");
        param0.register(var0, "minecraft:jukebox", param1 -> DSL.optionalFields("RecordItem", References.ITEM_STACK.in(param0)));
        registerInventory(param0, var0, "minecraft:dispenser");
        registerInventory(param0, var0, "minecraft:dropper");
        param0.registerSimple(var0, "minecraft:sign");
        param0.register(var0, "minecraft:mob_spawner", param1 -> References.UNTAGGED_SPAWNER.in(param0));
        param0.registerSimple(var0, "minecraft:noteblock");
        param0.registerSimple(var0, "minecraft:piston");
        registerInventory(param0, var0, "minecraft:brewing_stand");
        param0.registerSimple(var0, "minecraft:enchanting_table");
        param0.registerSimple(var0, "minecraft:end_portal");
        param0.registerSimple(var0, "minecraft:beacon");
        param0.registerSimple(var0, "minecraft:skull");
        param0.registerSimple(var0, "minecraft:daylight_detector");
        registerInventory(param0, var0, "minecraft:hopper");
        param0.registerSimple(var0, "minecraft:comparator");
        param0.register(
            var0, "minecraft:flower_pot", param1 -> DSL.optionalFields("Item", DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in(param0)))
        );
        param0.registerSimple(var0, "minecraft:banner");
        param0.registerSimple(var0, "minecraft:structure_block");
        param0.registerSimple(var0, "minecraft:end_gateway");
        param0.registerSimple(var0, "minecraft:command_block");
        return var0;
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        super.registerTypes(param0, param1, param2);
        param0.registerType(false, References.BLOCK_ENTITY, () -> DSL.taggedChoiceLazy("id", NamespacedSchema.namespacedString(), param2));
        param0.registerType(
            true,
            References.ITEM_STACK,
            () -> DSL.hook(
                    DSL.optionalFields(
                        "id",
                        References.ITEM_NAME.in(param0),
                        "tag",
                        DSL.optionalFields(
                            "EntityTag",
                            References.ENTITY_TREE.in(param0),
                            "BlockEntityTag",
                            References.BLOCK_ENTITY.in(param0),
                            "CanDestroy",
                            DSL.list(References.BLOCK_NAME.in(param0)),
                            "CanPlaceOn",
                            DSL.list(References.BLOCK_NAME.in(param0)),
                            "Items",
                            DSL.list(References.ITEM_STACK.in(param0))
                        )
                    ),
                    ADD_NAMES,
                    HookFunction.IDENTITY
                )
        );
    }
}
