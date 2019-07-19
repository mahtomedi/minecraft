package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V100 extends Schema {
    public V100(int param0, Schema param1) {
        super(param0, param1);
    }

    protected static TypeTemplate equipment(Schema param0) {
        return DSL.optionalFields("ArmorItems", DSL.list(References.ITEM_STACK.in(param0)), "HandItems", DSL.list(References.ITEM_STACK.in(param0)));
    }

    protected static void registerMob(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> equipment(param0));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
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
        registerMob(param0, var0, "Shulker");
        param0.registerSimple(var0, "AreaEffectCloud");
        param0.registerSimple(var0, "ShulkerBullet");
        return var0;
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        super.registerTypes(param0, param1, param2);
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
        param0.registerType(false, References.BLOCK_STATE, DSL::remainder);
    }
}
