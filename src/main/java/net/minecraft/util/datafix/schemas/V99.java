package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class V99 extends Schema {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, String> ITEM_TO_BLOCKENTITY = DataFixUtils.make(Maps.newHashMap(), param0 -> {
        param0.put("minecraft:furnace", "Furnace");
        param0.put("minecraft:lit_furnace", "Furnace");
        param0.put("minecraft:chest", "Chest");
        param0.put("minecraft:trapped_chest", "Chest");
        param0.put("minecraft:ender_chest", "EnderChest");
        param0.put("minecraft:jukebox", "RecordPlayer");
        param0.put("minecraft:dispenser", "Trap");
        param0.put("minecraft:dropper", "Dropper");
        param0.put("minecraft:sign", "Sign");
        param0.put("minecraft:mob_spawner", "MobSpawner");
        param0.put("minecraft:noteblock", "Music");
        param0.put("minecraft:brewing_stand", "Cauldron");
        param0.put("minecraft:enhanting_table", "EnchantTable");
        param0.put("minecraft:command_block", "CommandBlock");
        param0.put("minecraft:beacon", "Beacon");
        param0.put("minecraft:skull", "Skull");
        param0.put("minecraft:daylight_detector", "DLDetector");
        param0.put("minecraft:hopper", "Hopper");
        param0.put("minecraft:banner", "Banner");
        param0.put("minecraft:flower_pot", "FlowerPot");
        param0.put("minecraft:repeating_command_block", "CommandBlock");
        param0.put("minecraft:chain_command_block", "CommandBlock");
        param0.put("minecraft:standing_sign", "Sign");
        param0.put("minecraft:wall_sign", "Sign");
        param0.put("minecraft:piston_head", "Piston");
        param0.put("minecraft:daylight_detector_inverted", "DLDetector");
        param0.put("minecraft:unpowered_comparator", "Comparator");
        param0.put("minecraft:powered_comparator", "Comparator");
        param0.put("minecraft:wall_banner", "Banner");
        param0.put("minecraft:standing_banner", "Banner");
        param0.put("minecraft:structure_block", "Structure");
        param0.put("minecraft:end_portal", "Airportal");
        param0.put("minecraft:end_gateway", "EndGateway");
        param0.put("minecraft:shield", "Banner");
    });
    protected static final HookFunction ADD_NAMES = new HookFunction() {
        @Override
        public <T> T apply(DynamicOps<T> param0, T param1) {
            return V99.addNames(new Dynamic<>(param0, param1), V99.ITEM_TO_BLOCKENTITY, "ArmorStand");
        }
    };

    public V99(int param0, Schema param1) {
        super(param0, param1);
    }

    protected static TypeTemplate equipment(Schema param0) {
        return DSL.optionalFields("Equipment", DSL.list(References.ITEM_STACK.in(param0)));
    }

    protected static void registerMob(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> equipment(param0));
    }

    protected static void registerThrowableProjectile(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(param0)));
    }

    protected static void registerMinecart(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(param0)));
    }

    protected static void registerInventory(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(param0))));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = Maps.newHashMap();
        param0.register(var0, "Item", param1 -> DSL.optionalFields("Item", References.ITEM_STACK.in(param0)));
        param0.registerSimple(var0, "XPOrb");
        registerThrowableProjectile(param0, var0, "ThrownEgg");
        param0.registerSimple(var0, "LeashKnot");
        param0.registerSimple(var0, "Painting");
        param0.register(var0, "Arrow", param1 -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(param0)));
        param0.register(var0, "TippedArrow", param1 -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(param0)));
        param0.register(var0, "SpectralArrow", param1 -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(param0)));
        registerThrowableProjectile(param0, var0, "Snowball");
        registerThrowableProjectile(param0, var0, "Fireball");
        registerThrowableProjectile(param0, var0, "SmallFireball");
        registerThrowableProjectile(param0, var0, "ThrownEnderpearl");
        param0.registerSimple(var0, "EyeOfEnderSignal");
        param0.register(
            var0, "ThrownPotion", param1 -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(param0), "Potion", References.ITEM_STACK.in(param0))
        );
        registerThrowableProjectile(param0, var0, "ThrownExpBottle");
        param0.register(var0, "ItemFrame", param1 -> DSL.optionalFields("Item", References.ITEM_STACK.in(param0)));
        registerThrowableProjectile(param0, var0, "WitherSkull");
        param0.registerSimple(var0, "PrimedTnt");
        param0.register(
            var0, "FallingSand", param1 -> DSL.optionalFields("Block", References.BLOCK_NAME.in(param0), "TileEntityData", References.BLOCK_ENTITY.in(param0))
        );
        param0.register(var0, "FireworksRocketEntity", param1 -> DSL.optionalFields("FireworksItem", References.ITEM_STACK.in(param0)));
        param0.registerSimple(var0, "Boat");
        param0.register(
            var0, "Minecart", () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(param0), "Items", DSL.list(References.ITEM_STACK.in(param0)))
        );
        registerMinecart(param0, var0, "MinecartRideable");
        param0.register(
            var0,
            "MinecartChest",
            param1 -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(param0), "Items", DSL.list(References.ITEM_STACK.in(param0)))
        );
        registerMinecart(param0, var0, "MinecartFurnace");
        registerMinecart(param0, var0, "MinecartTNT");
        param0.register(
            var0, "MinecartSpawner", () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(param0), References.UNTAGGED_SPAWNER.in(param0))
        );
        param0.register(
            var0,
            "MinecartHopper",
            param1 -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(param0), "Items", DSL.list(References.ITEM_STACK.in(param0)))
        );
        registerMinecart(param0, var0, "MinecartCommandBlock");
        registerMob(param0, var0, "ArmorStand");
        registerMob(param0, var0, "Creeper");
        registerMob(param0, var0, "Skeleton");
        registerMob(param0, var0, "Spider");
        registerMob(param0, var0, "Giant");
        registerMob(param0, var0, "Zombie");
        registerMob(param0, var0, "Slime");
        registerMob(param0, var0, "Ghast");
        registerMob(param0, var0, "PigZombie");
        param0.register(var0, "Enderman", param1 -> DSL.optionalFields("carried", References.BLOCK_NAME.in(param0), equipment(param0)));
        registerMob(param0, var0, "CaveSpider");
        registerMob(param0, var0, "Silverfish");
        registerMob(param0, var0, "Blaze");
        registerMob(param0, var0, "LavaSlime");
        registerMob(param0, var0, "EnderDragon");
        registerMob(param0, var0, "WitherBoss");
        registerMob(param0, var0, "Bat");
        registerMob(param0, var0, "Witch");
        registerMob(param0, var0, "Endermite");
        registerMob(param0, var0, "Guardian");
        registerMob(param0, var0, "Pig");
        registerMob(param0, var0, "Sheep");
        registerMob(param0, var0, "Cow");
        registerMob(param0, var0, "Chicken");
        registerMob(param0, var0, "Squid");
        registerMob(param0, var0, "Wolf");
        registerMob(param0, var0, "MushroomCow");
        registerMob(param0, var0, "SnowMan");
        registerMob(param0, var0, "Ozelot");
        registerMob(param0, var0, "VillagerGolem");
        param0.register(
            var0,
            "EntityHorse",
            param1 -> DSL.optionalFields(
                    "Items",
                    DSL.list(References.ITEM_STACK.in(param0)),
                    "ArmorItem",
                    References.ITEM_STACK.in(param0),
                    "SaddleItem",
                    References.ITEM_STACK.in(param0),
                    equipment(param0)
                )
        );
        registerMob(param0, var0, "Rabbit");
        param0.register(
            var0,
            "Villager",
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
                    equipment(param0)
                )
        );
        param0.registerSimple(var0, "EnderCrystal");
        param0.registerSimple(var0, "AreaEffectCloud");
        param0.registerSimple(var0, "ShulkerBullet");
        registerMob(param0, var0, "Shulker");
        return var0;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = Maps.newHashMap();
        registerInventory(param0, var0, "Furnace");
        registerInventory(param0, var0, "Chest");
        param0.registerSimple(var0, "EnderChest");
        param0.register(var0, "RecordPlayer", param1 -> DSL.optionalFields("RecordItem", References.ITEM_STACK.in(param0)));
        registerInventory(param0, var0, "Trap");
        registerInventory(param0, var0, "Dropper");
        param0.registerSimple(var0, "Sign");
        param0.register(var0, "MobSpawner", param1 -> References.UNTAGGED_SPAWNER.in(param0));
        param0.registerSimple(var0, "Music");
        param0.registerSimple(var0, "Piston");
        registerInventory(param0, var0, "Cauldron");
        param0.registerSimple(var0, "EnchantTable");
        param0.registerSimple(var0, "Airportal");
        param0.registerSimple(var0, "Control");
        param0.registerSimple(var0, "Beacon");
        param0.registerSimple(var0, "Skull");
        param0.registerSimple(var0, "DLDetector");
        registerInventory(param0, var0, "Hopper");
        param0.registerSimple(var0, "Comparator");
        param0.register(var0, "FlowerPot", param1 -> DSL.optionalFields("Item", DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in(param0))));
        param0.registerSimple(var0, "Banner");
        param0.registerSimple(var0, "Structure");
        param0.registerSimple(var0, "EndGateway");
        return var0;
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        param0.registerType(false, References.LEVEL, DSL::remainder);
        param0.registerType(
            false,
            References.PLAYER,
            () -> DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(param0)), "EnderItems", DSL.list(References.ITEM_STACK.in(param0)))
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
                        DSL.list(DSL.fields("i", References.BLOCK_NAME.in(param0)))
                    )
                )
        );
        param0.registerType(true, References.BLOCK_ENTITY, () -> DSL.taggedChoiceLazy("id", DSL.string(), param2));
        param0.registerType(true, References.ENTITY_TREE, () -> DSL.optionalFields("Riding", References.ENTITY_TREE.in(param0), References.ENTITY.in(param0)));
        param0.registerType(false, References.ENTITY_NAME, () -> DSL.constType(NamespacedSchema.namespacedString()));
        param0.registerType(true, References.ENTITY, () -> DSL.taggedChoiceLazy("id", DSL.string(), param1));
        param0.registerType(
            true,
            References.ITEM_STACK,
            () -> DSL.hook(
                    DSL.optionalFields(
                        "id",
                        DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in(param0)),
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
        param0.registerType(false, References.OPTIONS, DSL::remainder);
        param0.registerType(false, References.BLOCK_NAME, () -> DSL.or(DSL.constType(DSL.intType()), DSL.constType(NamespacedSchema.namespacedString())));
        param0.registerType(false, References.ITEM_NAME, () -> DSL.constType(NamespacedSchema.namespacedString()));
        param0.registerType(false, References.STATS, DSL::remainder);
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
        param0.registerType(false, References.STRUCTURE_FEATURE, DSL::remainder);
        param0.registerType(false, References.OBJECTIVE, DSL::remainder);
        param0.registerType(false, References.TEAM, DSL::remainder);
        param0.registerType(true, References.UNTAGGED_SPAWNER, DSL::remainder);
        param0.registerType(false, References.POI_CHUNK, DSL::remainder);
        param0.registerType(true, References.WORLD_GEN_SETTINGS, DSL::remainder);
    }

    protected static <T> T addNames(Dynamic<T> param0, Map<String, String> param1, String param2) {
        return param0.update(
                "tag",
                param3 -> param3.update("BlockEntityTag", param2x -> {
                            String var0x = param0.get("id").asString("");
                            String var1x = param1.get(NamespacedSchema.ensureNamespaced(var0x));
                            if (var1x == null) {
                                LOGGER.warn("Unable to resolve BlockEntity for ItemStack: {}", var0x);
                                return param2x;
                            } else {
                                return param2x.set("id", param0.createString(var1x));
                            }
                        })
                        .update(
                            "EntityTag",
                            param2x -> {
                                String var0x = param0.get("id").asString("");
                                return Objects.equals(NamespacedSchema.ensureNamespaced(var0x), "minecraft:armor_stand")
                                    ? param2x.set("id", param0.createString(param2))
                                    : param2x;
                            }
                        )
            )
            .getValue();
    }
}
