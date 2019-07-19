package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;
import java.util.Set;

public class EntityHealthFix extends DataFix {
    private static final Set<String> ENTITIES = Sets.newHashSet(
        "ArmorStand",
        "Bat",
        "Blaze",
        "CaveSpider",
        "Chicken",
        "Cow",
        "Creeper",
        "EnderDragon",
        "Enderman",
        "Endermite",
        "EntityHorse",
        "Ghast",
        "Giant",
        "Guardian",
        "LavaSlime",
        "MushroomCow",
        "Ozelot",
        "Pig",
        "PigZombie",
        "Rabbit",
        "Sheep",
        "Shulker",
        "Silverfish",
        "Skeleton",
        "Slime",
        "SnowMan",
        "Spider",
        "Squid",
        "Villager",
        "VillagerGolem",
        "Witch",
        "WitherBoss",
        "Wolf",
        "Zombie"
    );

    public EntityHealthFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    public Dynamic<?> fixTag(Dynamic<?> param0) {
        Optional<Number> var0 = param0.get("HealF").asNumber();
        Optional<Number> var1 = param0.get("Health").asNumber();
        float var2;
        if (var0.isPresent()) {
            var2 = var0.get().floatValue();
            param0 = param0.remove("HealF");
        } else {
            if (!var1.isPresent()) {
                return param0;
            }

            var2 = var1.get().floatValue();
        }

        return param0.set("Health", param0.createFloat(var2));
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "EntityHealthFix", this.getInputSchema().getType(References.ENTITY), param0 -> param0.update(DSL.remainderFinder(), this::fixTag)
        );
    }
}
