package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1460 extends NamespacedSchema {
    public V1460(int param0, Schema param1) {
        super(param0, param1);
    }

    protected static void registerMob(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> V100.equipment(param0));
    }

    protected static void registerInventory(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(param0))));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = Maps.newHashMap();
        param0.registerSimple(var0, "minecraft:area_effect_cloud");
        registerMob(param0, var0, "minecraft:armor_stand");
        param0.register(var0, "minecraft:arrow", param1 -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(param0)));
        registerMob(param0, var0, "minecraft:bat");
        registerMob(param0, var0, "minecraft:blaze");
        param0.registerSimple(var0, "minecraft:boat");
        registerMob(param0, var0, "minecraft:cave_spider");
        param0.register(
            var0,
            "minecraft:chest_minecart",
            param1 -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0), "Items", DSL.list(References.ITEM_STACK.in(param0)))
        );
        registerMob(param0, var0, "minecraft:chicken");
        param0.register(var0, "minecraft:commandblock_minecart", param1 -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0)));
        registerMob(param0, var0, "minecraft:cow");
        registerMob(param0, var0, "minecraft:creeper");
        param0.register(
            var0,
            "minecraft:donkey",
            param1 -> DSL.optionalFields(
                    "Items", DSL.list(References.ITEM_STACK.in(param0)), "SaddleItem", References.ITEM_STACK.in(param0), V100.equipment(param0)
                )
        );
        param0.registerSimple(var0, "minecraft:dragon_fireball");
        param0.registerSimple(var0, "minecraft:egg");
        registerMob(param0, var0, "minecraft:elder_guardian");
        param0.registerSimple(var0, "minecraft:ender_crystal");
        registerMob(param0, var0, "minecraft:ender_dragon");
        param0.register(
            var0, "minecraft:enderman", param1 -> DSL.optionalFields("carriedBlockState", References.BLOCK_STATE.in(param0), V100.equipment(param0))
        );
        registerMob(param0, var0, "minecraft:endermite");
        param0.registerSimple(var0, "minecraft:ender_pearl");
        param0.registerSimple(var0, "minecraft:evocation_fangs");
        registerMob(param0, var0, "minecraft:evocation_illager");
        param0.registerSimple(var0, "minecraft:eye_of_ender_signal");
        param0.register(
            var0,
            "minecraft:falling_block",
            param1 -> DSL.optionalFields("BlockState", References.BLOCK_STATE.in(param0), "TileEntityData", References.BLOCK_ENTITY.in(param0))
        );
        param0.registerSimple(var0, "minecraft:fireball");
        param0.register(var0, "minecraft:fireworks_rocket", param1 -> DSL.optionalFields("FireworksItem", References.ITEM_STACK.in(param0)));
        param0.register(var0, "minecraft:furnace_minecart", param1 -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0)));
        registerMob(param0, var0, "minecraft:ghast");
        registerMob(param0, var0, "minecraft:giant");
        registerMob(param0, var0, "minecraft:guardian");
        param0.register(
            var0,
            "minecraft:hopper_minecart",
            param1 -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0), "Items", DSL.list(References.ITEM_STACK.in(param0)))
        );
        param0.register(
            var0,
            "minecraft:horse",
            param1 -> DSL.optionalFields("ArmorItem", References.ITEM_STACK.in(param0), "SaddleItem", References.ITEM_STACK.in(param0), V100.equipment(param0))
        );
        registerMob(param0, var0, "minecraft:husk");
        param0.registerSimple(var0, "minecraft:illusion_illager");
        param0.register(var0, "minecraft:item", param1 -> DSL.optionalFields("Item", References.ITEM_STACK.in(param0)));
        param0.register(var0, "minecraft:item_frame", param1 -> DSL.optionalFields("Item", References.ITEM_STACK.in(param0)));
        param0.registerSimple(var0, "minecraft:leash_knot");
        param0.register(
            var0,
            "minecraft:llama",
            param1 -> DSL.optionalFields(
                    "Items",
                    DSL.list(References.ITEM_STACK.in(param0)),
                    "SaddleItem",
                    References.ITEM_STACK.in(param0),
                    "DecorItem",
                    References.ITEM_STACK.in(param0),
                    V100.equipment(param0)
                )
        );
        param0.registerSimple(var0, "minecraft:llama_spit");
        registerMob(param0, var0, "minecraft:magma_cube");
        param0.register(var0, "minecraft:minecart", param1 -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0)));
        registerMob(param0, var0, "minecraft:mooshroom");
        param0.register(
            var0,
            "minecraft:mule",
            param1 -> DSL.optionalFields(
                    "Items", DSL.list(References.ITEM_STACK.in(param0)), "SaddleItem", References.ITEM_STACK.in(param0), V100.equipment(param0)
                )
        );
        registerMob(param0, var0, "minecraft:ocelot");
        param0.registerSimple(var0, "minecraft:painting");
        param0.registerSimple(var0, "minecraft:parrot");
        registerMob(param0, var0, "minecraft:pig");
        registerMob(param0, var0, "minecraft:polar_bear");
        param0.register(var0, "minecraft:potion", param1 -> DSL.optionalFields("Potion", References.ITEM_STACK.in(param0)));
        registerMob(param0, var0, "minecraft:rabbit");
        registerMob(param0, var0, "minecraft:sheep");
        registerMob(param0, var0, "minecraft:shulker");
        param0.registerSimple(var0, "minecraft:shulker_bullet");
        registerMob(param0, var0, "minecraft:silverfish");
        registerMob(param0, var0, "minecraft:skeleton");
        param0.register(var0, "minecraft:skeleton_horse", param1 -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(param0), V100.equipment(param0)));
        registerMob(param0, var0, "minecraft:slime");
        param0.registerSimple(var0, "minecraft:small_fireball");
        param0.registerSimple(var0, "minecraft:snowball");
        registerMob(param0, var0, "minecraft:snowman");
        param0.register(
            var0,
            "minecraft:spawner_minecart",
            param1 -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0), References.UNTAGGED_SPAWNER.in(param0))
        );
        param0.register(var0, "minecraft:spectral_arrow", param1 -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(param0)));
        registerMob(param0, var0, "minecraft:spider");
        registerMob(param0, var0, "minecraft:squid");
        registerMob(param0, var0, "minecraft:stray");
        param0.registerSimple(var0, "minecraft:tnt");
        param0.register(var0, "minecraft:tnt_minecart", param1 -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(param0)));
        registerMob(param0, var0, "minecraft:vex");
        param0.register(
            var0,
            "minecraft:villager",
            param1 -> DSL.optionalFields(
                    "Inventory",
                    DSL.list(References.ITEM_STACK.in(param0)),
                    "Offers",
                    DSL.optionalFields(
                        "Recipes",
                        DSL.list(
                            DSL.optionalFields(
                                "buy", References.ITEM_STACK.in(param0), "buyB", References.ITEM_STACK.in(param0), "sell", References.ITEM_STACK.in(param0)
                            )
                        )
                    ),
                    V100.equipment(param0)
                )
        );
        registerMob(param0, var0, "minecraft:villager_golem");
        registerMob(param0, var0, "minecraft:vindication_illager");
        registerMob(param0, var0, "minecraft:witch");
        registerMob(param0, var0, "minecraft:wither");
        registerMob(param0, var0, "minecraft:wither_skeleton");
        param0.registerSimple(var0, "minecraft:wither_skull");
        registerMob(param0, var0, "minecraft:wolf");
        param0.registerSimple(var0, "minecraft:xp_bottle");
        param0.registerSimple(var0, "minecraft:xp_orb");
        registerMob(param0, var0, "minecraft:zombie");
        param0.register(var0, "minecraft:zombie_horse", param1 -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(param0), V100.equipment(param0)));
        registerMob(param0, var0, "minecraft:zombie_pigman");
        registerMob(param0, var0, "minecraft:zombie_villager");
        return var0;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = Maps.newHashMap();
        registerInventory(param0, var0, "minecraft:furnace");
        registerInventory(param0, var0, "minecraft:chest");
        registerInventory(param0, var0, "minecraft:trapped_chest");
        param0.registerSimple(var0, "minecraft:ender_chest");
        param0.register(var0, "minecraft:jukebox", param1 -> DSL.optionalFields("RecordItem", References.ITEM_STACK.in(param0)));
        registerInventory(param0, var0, "minecraft:dispenser");
        registerInventory(param0, var0, "minecraft:dropper");
        param0.registerSimple(var0, "minecraft:sign");
        param0.register(var0, "minecraft:mob_spawner", param1 -> References.UNTAGGED_SPAWNER.in(param0));
        param0.register(var0, "minecraft:piston", param1 -> DSL.optionalFields("blockState", References.BLOCK_STATE.in(param0)));
        registerInventory(param0, var0, "minecraft:brewing_stand");
        param0.registerSimple(var0, "minecraft:enchanting_table");
        param0.registerSimple(var0, "minecraft:end_portal");
        param0.registerSimple(var0, "minecraft:beacon");
        param0.registerSimple(var0, "minecraft:skull");
        param0.registerSimple(var0, "minecraft:daylight_detector");
        registerInventory(param0, var0, "minecraft:hopper");
        param0.registerSimple(var0, "minecraft:comparator");
        param0.registerSimple(var0, "minecraft:banner");
        param0.registerSimple(var0, "minecraft:structure_block");
        param0.registerSimple(var0, "minecraft:end_gateway");
        param0.registerSimple(var0, "minecraft:command_block");
        registerInventory(param0, var0, "minecraft:shulker_box");
        param0.registerSimple(var0, "minecraft:bed");
        return var0;
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        param0.registerType(false, References.LEVEL, DSL::remainder);
        param0.registerType(false, References.RECIPE, () -> DSL.constType(namespacedString()));
        param0.registerType(
            false,
            References.PLAYER,
            () -> DSL.optionalFields(
                    "RootVehicle",
                    DSL.optionalFields("Entity", References.ENTITY_TREE.in(param0)),
                    "Inventory",
                    DSL.list(References.ITEM_STACK.in(param0)),
                    "EnderItems",
                    DSL.list(References.ITEM_STACK.in(param0)),
                    DSL.optionalFields(
                        "ShoulderEntityLeft",
                        References.ENTITY_TREE.in(param0),
                        "ShoulderEntityRight",
                        References.ENTITY_TREE.in(param0),
                        "recipeBook",
                        DSL.optionalFields("recipes", DSL.list(References.RECIPE.in(param0)), "toBeDisplayed", DSL.list(References.RECIPE.in(param0)))
                    )
                )
        );
        param0.registerType(
            false,
            References.CHUNK,
            () -> DSL.fields(
                    "Level",
                    DSL.optionalFields(
                        "Entities",
                        DSL.list(References.ENTITY_TREE.in(param0)),
                        "TileEntities",
                        DSL.list(References.BLOCK_ENTITY.in(param0)),
                        "TileTicks",
                        DSL.list(DSL.fields("i", References.BLOCK_NAME.in(param0))),
                        "Sections",
                        DSL.list(DSL.optionalFields("Palette", DSL.list(References.BLOCK_STATE.in(param0))))
                    )
                )
        );
        param0.registerType(true, References.BLOCK_ENTITY, () -> DSL.taggedChoiceLazy("id", namespacedString(), param2));
        param0.registerType(
            true, References.ENTITY_TREE, () -> DSL.optionalFields("Passengers", DSL.list(References.ENTITY_TREE.in(param0)), References.ENTITY.in(param0))
        );
        param0.registerType(true, References.ENTITY, () -> DSL.taggedChoiceLazy("id", namespacedString(), param1));
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
                    V705.ADD_NAMES,
                    HookFunction.IDENTITY
                )
        );
        param0.registerType(false, References.HOTBAR, () -> DSL.compoundList(DSL.list(References.ITEM_STACK.in(param0))));
        param0.registerType(false, References.OPTIONS, DSL::remainder);
        param0.registerType(
            false,
            References.STRUCTURE,
            () -> DSL.optionalFields(
                    "entities",
                    DSL.list(DSL.optionalFields("nbt", References.ENTITY_TREE.in(param0))),
                    "blocks",
                    DSL.list(DSL.optionalFields("nbt", References.BLOCK_ENTITY.in(param0))),
                    "palette",
                    DSL.list(References.BLOCK_STATE.in(param0))
                )
        );
        param0.registerType(false, References.BLOCK_NAME, () -> DSL.constType(namespacedString()));
        param0.registerType(false, References.ITEM_NAME, () -> DSL.constType(namespacedString()));
        param0.registerType(false, References.BLOCK_STATE, DSL::remainder);
        Supplier<TypeTemplate> var0 = () -> DSL.compoundList(References.ITEM_NAME.in(param0), DSL.constType(DSL.intType()));
        param0.registerType(
            false,
            References.STATS,
            () -> DSL.optionalFields(
                    "stats",
                    DSL.optionalFields(
                        "minecraft:mined",
                        DSL.compoundList(References.BLOCK_NAME.in(param0), DSL.constType(DSL.intType())),
                        "minecraft:crafted",
                        var0.get(),
                        "minecraft:used",
                        var0.get(),
                        "minecraft:broken",
                        var0.get(),
                        "minecraft:picked_up",
                        var0.get(),
                        DSL.optionalFields(
                            "minecraft:dropped",
                            var0.get(),
                            "minecraft:killed",
                            DSL.compoundList(References.ENTITY_NAME.in(param0), DSL.constType(DSL.intType())),
                            "minecraft:killed_by",
                            DSL.compoundList(References.ENTITY_NAME.in(param0), DSL.constType(DSL.intType())),
                            "minecraft:custom",
                            DSL.compoundList(DSL.constType(namespacedString()), DSL.constType(DSL.intType()))
                        )
                    )
                )
        );
        param0.registerType(
            false,
            References.SAVED_DATA,
            () -> DSL.optionalFields(
                    "data",
                    DSL.optionalFields(
                        "Features",
                        DSL.compoundList(References.STRUCTURE_FEATURE.in(param0)),
                        "Objectives",
                        DSL.list(References.OBJECTIVE.in(param0)),
                        "Teams",
                        DSL.list(References.TEAM.in(param0))
                    )
                )
        );
        param0.registerType(
            false,
            References.STRUCTURE_FEATURE,
            () -> DSL.optionalFields(
                    "Children",
                    DSL.list(
                        DSL.optionalFields(
                            "CA",
                            References.BLOCK_STATE.in(param0),
                            "CB",
                            References.BLOCK_STATE.in(param0),
                            "CC",
                            References.BLOCK_STATE.in(param0),
                            "CD",
                            References.BLOCK_STATE.in(param0)
                        )
                    )
                )
        );
        Map<String, Supplier<TypeTemplate>> var1 = V1451_6.createCriterionTypes(param0);
        param0.registerType(
            false,
            References.OBJECTIVE,
            () -> DSL.hook(
                    DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), var1)),
                    V1451_6.UNPACK_OBJECTIVE_ID,
                    V1451_6.REPACK_OBJECTIVE_ID
                )
        );
        param0.registerType(false, References.TEAM, DSL::remainder);
        param0.registerType(
            true,
            References.UNTAGGED_SPAWNER,
            () -> DSL.optionalFields(
                    "SpawnPotentials", DSL.list(DSL.fields("Entity", References.ENTITY_TREE.in(param0))), "SpawnData", References.ENTITY_TREE.in(param0)
                )
        );
        param0.registerType(
            false,
            References.ADVANCEMENTS,
            () -> DSL.optionalFields(
                    "minecraft:adventure/adventuring_time",
                    DSL.optionalFields("criteria", DSL.compoundList(References.BIOME.in(param0), DSL.constType(DSL.string()))),
                    "minecraft:adventure/kill_a_mob",
                    DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(param0), DSL.constType(DSL.string()))),
                    "minecraft:adventure/kill_all_mobs",
                    DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(param0), DSL.constType(DSL.string()))),
                    "minecraft:husbandry/bred_all_animals",
                    DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(param0), DSL.constType(DSL.string())))
                )
        );
        param0.registerType(false, References.BIOME, () -> DSL.constType(namespacedString()));
        param0.registerType(false, References.ENTITY_NAME, () -> DSL.constType(namespacedString()));
        param0.registerType(false, References.POI_CHUNK, DSL::remainder);
        param0.registerType(true, References.WORLD_GEN_SETTINGS, DSL::remainder);
        param0.registerType(false, References.ENTITY_CHUNK, () -> DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(param0))));
    }
}
