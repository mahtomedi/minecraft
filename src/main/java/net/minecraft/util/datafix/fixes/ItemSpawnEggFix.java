package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemSpawnEggFix extends DataFix {
    private static final String[] ID_TO_ENTITY = DataFixUtils.make(new String[256], param0 -> {
        param0[1] = "Item";
        param0[2] = "XPOrb";
        param0[7] = "ThrownEgg";
        param0[8] = "LeashKnot";
        param0[9] = "Painting";
        param0[10] = "Arrow";
        param0[11] = "Snowball";
        param0[12] = "Fireball";
        param0[13] = "SmallFireball";
        param0[14] = "ThrownEnderpearl";
        param0[15] = "EyeOfEnderSignal";
        param0[16] = "ThrownPotion";
        param0[17] = "ThrownExpBottle";
        param0[18] = "ItemFrame";
        param0[19] = "WitherSkull";
        param0[20] = "PrimedTnt";
        param0[21] = "FallingSand";
        param0[22] = "FireworksRocketEntity";
        param0[23] = "TippedArrow";
        param0[24] = "SpectralArrow";
        param0[25] = "ShulkerBullet";
        param0[26] = "DragonFireball";
        param0[30] = "ArmorStand";
        param0[41] = "Boat";
        param0[42] = "MinecartRideable";
        param0[43] = "MinecartChest";
        param0[44] = "MinecartFurnace";
        param0[45] = "MinecartTNT";
        param0[46] = "MinecartHopper";
        param0[47] = "MinecartSpawner";
        param0[40] = "MinecartCommandBlock";
        param0[48] = "Mob";
        param0[49] = "Monster";
        param0[50] = "Creeper";
        param0[51] = "Skeleton";
        param0[52] = "Spider";
        param0[53] = "Giant";
        param0[54] = "Zombie";
        param0[55] = "Slime";
        param0[56] = "Ghast";
        param0[57] = "PigZombie";
        param0[58] = "Enderman";
        param0[59] = "CaveSpider";
        param0[60] = "Silverfish";
        param0[61] = "Blaze";
        param0[62] = "LavaSlime";
        param0[63] = "EnderDragon";
        param0[64] = "WitherBoss";
        param0[65] = "Bat";
        param0[66] = "Witch";
        param0[67] = "Endermite";
        param0[68] = "Guardian";
        param0[69] = "Shulker";
        param0[90] = "Pig";
        param0[91] = "Sheep";
        param0[92] = "Cow";
        param0[93] = "Chicken";
        param0[94] = "Squid";
        param0[95] = "Wolf";
        param0[96] = "MushroomCow";
        param0[97] = "SnowMan";
        param0[98] = "Ozelot";
        param0[99] = "VillagerGolem";
        param0[100] = "EntityHorse";
        param0[101] = "Rabbit";
        param0[120] = "Villager";
        param0[200] = "EnderCrystal";
    });

    public ItemSpawnEggFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Schema var0 = this.getInputSchema();
        Type<?> var1 = var0.getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> var2 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<String> var3 = DSL.fieldFinder("id", DSL.string());
        OpticFinder<?> var4 = var1.findField("tag");
        OpticFinder<?> var5 = var4.type().findField("EntityTag");
        OpticFinder<?> var6 = DSL.typeFinder(var0.getTypeRaw(References.ENTITY));
        Type<?> var7 = this.getOutputSchema().getTypeRaw(References.ENTITY);
        return this.fixTypeEverywhereTyped(
            "ItemSpawnEggFix",
            var1,
            param6 -> {
                Optional<Pair<String, String>> var0x = param6.getOptional(var2);
                if (var0x.isPresent() && Objects.equals(var0x.get().getSecond(), "minecraft:spawn_egg")) {
                    Dynamic<?> var1x = param6.get(DSL.remainderFinder());
                    short var2x = var1x.get("Damage").asShort((short)0);
                    Optional<? extends Typed<?>> var3x = param6.getOptionalTyped(var4);
                    Optional<? extends Typed<?>> var4x = var3x.flatMap(param1x -> param1x.getOptionalTyped(var5));
                    Optional<? extends Typed<?>> var5x = var4x.flatMap(param1x -> param1x.getOptionalTyped(var6));
                    Optional<String> var6x = var5x.flatMap(param1x -> param1x.getOptional(var3));
                    Typed<?> var7x = param6;
                    String var8x = ID_TO_ENTITY[var2x & 255];
                    if (var8x != null && (var6x.isEmpty() || !Objects.equals(var6x.get(), var8x))) {
                        Typed<?> var9 = param6.getOrCreateTyped(var4);
                        Typed<?> var10 = var9.getOrCreateTyped(var5);
                        Typed<?> var11 = var10.getOrCreateTyped(var6);
                        Dynamic<?> var12 = var1x;
                        Typed<?> var13 = var11.write()
                            .flatMap(param3x -> var7.readTyped(param3x.set("id", var12.createString(var8x))))
                            .result()
                            .orElseThrow(() -> new IllegalStateException("Could not parse new entity"))
                            .getFirst();
                        var7x = param6.set(var4, var9.set(var5, var10.set(var6, var13)));
                    }
    
                    if (var2x != 0) {
                        var1x = var1x.set("Damage", var1x.createShort((short)0));
                        var7x = var7x.set(DSL.remainderFinder(), var1x);
                    }
    
                    return var7x;
                } else {
                    return param6;
                }
            }
        );
    }
}
