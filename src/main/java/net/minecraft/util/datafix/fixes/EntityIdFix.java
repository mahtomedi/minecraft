package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import java.util.Map;

public class EntityIdFix extends DataFix {
    private static final Map<String, String> ID_MAP = DataFixUtils.make(Maps.newHashMap(), param0 -> {
        param0.put("AreaEffectCloud", "minecraft:area_effect_cloud");
        param0.put("ArmorStand", "minecraft:armor_stand");
        param0.put("Arrow", "minecraft:arrow");
        param0.put("Bat", "minecraft:bat");
        param0.put("Blaze", "minecraft:blaze");
        param0.put("Boat", "minecraft:boat");
        param0.put("CaveSpider", "minecraft:cave_spider");
        param0.put("Chicken", "minecraft:chicken");
        param0.put("Cow", "minecraft:cow");
        param0.put("Creeper", "minecraft:creeper");
        param0.put("Donkey", "minecraft:donkey");
        param0.put("DragonFireball", "minecraft:dragon_fireball");
        param0.put("ElderGuardian", "minecraft:elder_guardian");
        param0.put("EnderCrystal", "minecraft:ender_crystal");
        param0.put("EnderDragon", "minecraft:ender_dragon");
        param0.put("Enderman", "minecraft:enderman");
        param0.put("Endermite", "minecraft:endermite");
        param0.put("EyeOfEnderSignal", "minecraft:eye_of_ender_signal");
        param0.put("FallingSand", "minecraft:falling_block");
        param0.put("Fireball", "minecraft:fireball");
        param0.put("FireworksRocketEntity", "minecraft:fireworks_rocket");
        param0.put("Ghast", "minecraft:ghast");
        param0.put("Giant", "minecraft:giant");
        param0.put("Guardian", "minecraft:guardian");
        param0.put("Horse", "minecraft:horse");
        param0.put("Husk", "minecraft:husk");
        param0.put("Item", "minecraft:item");
        param0.put("ItemFrame", "minecraft:item_frame");
        param0.put("LavaSlime", "minecraft:magma_cube");
        param0.put("LeashKnot", "minecraft:leash_knot");
        param0.put("MinecartChest", "minecraft:chest_minecart");
        param0.put("MinecartCommandBlock", "minecraft:commandblock_minecart");
        param0.put("MinecartFurnace", "minecraft:furnace_minecart");
        param0.put("MinecartHopper", "minecraft:hopper_minecart");
        param0.put("MinecartRideable", "minecraft:minecart");
        param0.put("MinecartSpawner", "minecraft:spawner_minecart");
        param0.put("MinecartTNT", "minecraft:tnt_minecart");
        param0.put("Mule", "minecraft:mule");
        param0.put("MushroomCow", "minecraft:mooshroom");
        param0.put("Ozelot", "minecraft:ocelot");
        param0.put("Painting", "minecraft:painting");
        param0.put("Pig", "minecraft:pig");
        param0.put("PigZombie", "minecraft:zombie_pigman");
        param0.put("PolarBear", "minecraft:polar_bear");
        param0.put("PrimedTnt", "minecraft:tnt");
        param0.put("Rabbit", "minecraft:rabbit");
        param0.put("Sheep", "minecraft:sheep");
        param0.put("Shulker", "minecraft:shulker");
        param0.put("ShulkerBullet", "minecraft:shulker_bullet");
        param0.put("Silverfish", "minecraft:silverfish");
        param0.put("Skeleton", "minecraft:skeleton");
        param0.put("SkeletonHorse", "minecraft:skeleton_horse");
        param0.put("Slime", "minecraft:slime");
        param0.put("SmallFireball", "minecraft:small_fireball");
        param0.put("SnowMan", "minecraft:snowman");
        param0.put("Snowball", "minecraft:snowball");
        param0.put("SpectralArrow", "minecraft:spectral_arrow");
        param0.put("Spider", "minecraft:spider");
        param0.put("Squid", "minecraft:squid");
        param0.put("Stray", "minecraft:stray");
        param0.put("ThrownEgg", "minecraft:egg");
        param0.put("ThrownEnderpearl", "minecraft:ender_pearl");
        param0.put("ThrownExpBottle", "minecraft:xp_bottle");
        param0.put("ThrownPotion", "minecraft:potion");
        param0.put("Villager", "minecraft:villager");
        param0.put("VillagerGolem", "minecraft:villager_golem");
        param0.put("Witch", "minecraft:witch");
        param0.put("WitherBoss", "minecraft:wither");
        param0.put("WitherSkeleton", "minecraft:wither_skeleton");
        param0.put("WitherSkull", "minecraft:wither_skull");
        param0.put("Wolf", "minecraft:wolf");
        param0.put("XPOrb", "minecraft:xp_orb");
        param0.put("Zombie", "minecraft:zombie");
        param0.put("ZombieHorse", "minecraft:zombie_horse");
        param0.put("ZombieVillager", "minecraft:zombie_villager");
    });

    public EntityIdFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        TaggedChoiceType<String> var0 = this.getInputSchema().findChoiceType(References.ENTITY);
        TaggedChoiceType<String> var1 = this.getOutputSchema().findChoiceType(References.ENTITY);
        Type<?> var2 = this.getInputSchema().getType(References.ITEM_STACK);
        Type<?> var3 = this.getOutputSchema().getType(References.ITEM_STACK);
        return TypeRewriteRule.seq(
            this.convertUnchecked("item stack entity name hook converter", var2, var3),
            this.fixTypeEverywhere("EntityIdFix", var0, var1, param0 -> param0x -> param0x.mapFirst(param0xx -> ID_MAP.getOrDefault(param0xx, param0xx)))
        );
    }
}
