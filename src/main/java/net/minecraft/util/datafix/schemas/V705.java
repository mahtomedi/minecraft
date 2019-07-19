package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V705 extends NamespacedSchema {
    protected static final HookFunction ADD_NAMES = new HookFunction() {
        @Override
        public <T> T apply(DynamicOps<T> param0, T param1) {
            return V99.addNames(new Dynamic<>(param0, param1), V704.ITEM_TO_BLOCKENTITY, "minecraft:armor_stand");
        }
    };

    public V705(int param0, Schema param1) {
        super(param0, param1);
    }

    protected static void registerMob(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> V100.equipment(param0));
    }

    protected static void registerThrowableProjectile(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(param0)));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = Maps.newHashMap();
        param0.registerSimple(var0, "minecraft:area_effect_cloud");
        registerMob(param0, var0, "minecraft:armor_stand");
        param0.register(var0, "minecraft:arrow", param1 -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(param0)));
        registerMob(param0, var0, "minecraft:bat");
        registerMob(param0, var0, "minecraft:blaze");
        param0.registerSimple(var0, "minecraft:boat");
        registerMob(param0, var0, "minecraft:cave_spider");
        param0.register(
            var0,
            "minecraft:chest_minecart",
            param1 -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(param0), "Items", DSL.list(References.ITEM_STACK.in(param0)))
        );
        registerMob(param0, var0, "minecraft:chicken");
        param0.register(var0, "minecraft:commandblock_minecart", param1 -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(param0)));
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
        registerThrowableProjectile(param0, var0, "minecraft:egg");
        registerMob(param0, var0, "minecraft:elder_guardian");
        param0.registerSimple(var0, "minecraft:ender_crystal");
        registerMob(param0, var0, "minecraft:ender_dragon");
        param0.register(var0, "minecraft:enderman", param1 -> DSL.optionalFields("carried", References.BLOCK_NAME.in(param0), V100.equipment(param0)));
        registerMob(param0, var0, "minecraft:endermite");
        registerThrowableProjectile(param0, var0, "minecraft:ender_pearl");
        param0.registerSimple(var0, "minecraft:eye_of_ender_signal");
        param0.register(
            var0,
            "minecraft:falling_block",
            param1 -> DSL.optionalFields("Block", References.BLOCK_NAME.in(param0), "TileEntityData", References.BLOCK_ENTITY.in(param0))
        );
        registerThrowableProjectile(param0, var0, "minecraft:fireball");
        param0.register(var0, "minecraft:fireworks_rocket", param1 -> DSL.optionalFields("FireworksItem", References.ITEM_STACK.in(param0)));
        param0.register(var0, "minecraft:furnace_minecart", param1 -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(param0)));
        registerMob(param0, var0, "minecraft:ghast");
        registerMob(param0, var0, "minecraft:giant");
        registerMob(param0, var0, "minecraft:guardian");
        param0.register(
            var0,
            "minecraft:hopper_minecart",
            param1 -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(param0), "Items", DSL.list(References.ITEM_STACK.in(param0)))
        );
        param0.register(
            var0,
            "minecraft:horse",
            param1 -> DSL.optionalFields("ArmorItem", References.ITEM_STACK.in(param0), "SaddleItem", References.ITEM_STACK.in(param0), V100.equipment(param0))
        );
        registerMob(param0, var0, "minecraft:husk");
        param0.register(var0, "minecraft:item", param1 -> DSL.optionalFields("Item", References.ITEM_STACK.in(param0)));
        param0.register(var0, "minecraft:item_frame", param1 -> DSL.optionalFields("Item", References.ITEM_STACK.in(param0)));
        param0.registerSimple(var0, "minecraft:leash_knot");
        registerMob(param0, var0, "minecraft:magma_cube");
        param0.register(var0, "minecraft:minecart", param1 -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(param0)));
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
        param0.register(
            var0, "minecraft:potion", param1 -> DSL.optionalFields("Potion", References.ITEM_STACK.in(param0), "inTile", References.BLOCK_NAME.in(param0))
        );
        registerMob(param0, var0, "minecraft:rabbit");
        registerMob(param0, var0, "minecraft:sheep");
        registerMob(param0, var0, "minecraft:shulker");
        param0.registerSimple(var0, "minecraft:shulker_bullet");
        registerMob(param0, var0, "minecraft:silverfish");
        registerMob(param0, var0, "minecraft:skeleton");
        param0.register(var0, "minecraft:skeleton_horse", param1 -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(param0), V100.equipment(param0)));
        registerMob(param0, var0, "minecraft:slime");
        registerThrowableProjectile(param0, var0, "minecraft:small_fireball");
        registerThrowableProjectile(param0, var0, "minecraft:snowball");
        registerMob(param0, var0, "minecraft:snowman");
        param0.register(
            var0,
            "minecraft:spawner_minecart",
            param1 -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(param0), References.UNTAGGED_SPAWNER.in(param0))
        );
        param0.register(var0, "minecraft:spectral_arrow", param1 -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(param0)));
        registerMob(param0, var0, "minecraft:spider");
        registerMob(param0, var0, "minecraft:squid");
        registerMob(param0, var0, "minecraft:stray");
        param0.registerSimple(var0, "minecraft:tnt");
        param0.register(var0, "minecraft:tnt_minecart", param1 -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(param0)));
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
        registerMob(param0, var0, "minecraft:witch");
        registerMob(param0, var0, "minecraft:wither");
        registerMob(param0, var0, "minecraft:wither_skeleton");
        registerThrowableProjectile(param0, var0, "minecraft:wither_skull");
        registerMob(param0, var0, "minecraft:wolf");
        registerThrowableProjectile(param0, var0, "minecraft:xp_bottle");
        param0.registerSimple(var0, "minecraft:xp_orb");
        registerMob(param0, var0, "minecraft:zombie");
        param0.register(var0, "minecraft:zombie_horse", param1 -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(param0), V100.equipment(param0)));
        registerMob(param0, var0, "minecraft:zombie_pigman");
        registerMob(param0, var0, "minecraft:zombie_villager");
        param0.registerSimple(var0, "minecraft:evocation_fangs");
        registerMob(param0, var0, "minecraft:evocation_illager");
        param0.registerSimple(var0, "minecraft:illusion_illager");
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
        registerMob(param0, var0, "minecraft:vex");
        registerMob(param0, var0, "minecraft:vindication_illager");
        return var0;
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        super.registerTypes(param0, param1, param2);
        param0.registerType(true, References.ENTITY, () -> DSL.taggedChoiceLazy("id", DSL.namespacedString(), param1));
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
                            DSL.list(References.BLOCK_NAME.in(param0))
                        )
                    ),
                    ADD_NAMES,
                    HookFunction.IDENTITY
                )
        );
    }
}
