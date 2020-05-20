package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;

public class ItemStackEnchantmentNamesFix extends DataFix {
    private static final Int2ObjectMap<String> MAP = DataFixUtils.make(new Int2ObjectOpenHashMap<>(), param0 -> {
        param0.put(0, "minecraft:protection");
        param0.put(1, "minecraft:fire_protection");
        param0.put(2, "minecraft:feather_falling");
        param0.put(3, "minecraft:blast_protection");
        param0.put(4, "minecraft:projectile_protection");
        param0.put(5, "minecraft:respiration");
        param0.put(6, "minecraft:aqua_affinity");
        param0.put(7, "minecraft:thorns");
        param0.put(8, "minecraft:depth_strider");
        param0.put(9, "minecraft:frost_walker");
        param0.put(10, "minecraft:binding_curse");
        param0.put(16, "minecraft:sharpness");
        param0.put(17, "minecraft:smite");
        param0.put(18, "minecraft:bane_of_arthropods");
        param0.put(19, "minecraft:knockback");
        param0.put(20, "minecraft:fire_aspect");
        param0.put(21, "minecraft:looting");
        param0.put(22, "minecraft:sweeping");
        param0.put(32, "minecraft:efficiency");
        param0.put(33, "minecraft:silk_touch");
        param0.put(34, "minecraft:unbreaking");
        param0.put(35, "minecraft:fortune");
        param0.put(48, "minecraft:power");
        param0.put(49, "minecraft:punch");
        param0.put(50, "minecraft:flame");
        param0.put(51, "minecraft:infinity");
        param0.put(61, "minecraft:luck_of_the_sea");
        param0.put(62, "minecraft:lure");
        param0.put(65, "minecraft:loyalty");
        param0.put(66, "minecraft:impaling");
        param0.put(67, "minecraft:riptide");
        param0.put(68, "minecraft:channeling");
        param0.put(70, "minecraft:mending");
        param0.put(71, "minecraft:vanishing_curse");
    });

    public ItemStackEnchantmentNamesFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<?> var1 = var0.findField("tag");
        return this.fixTypeEverywhereTyped(
            "ItemStackEnchantmentFix", var0, param1 -> param1.updateTyped(var1, param0x -> param0x.update(DSL.remainderFinder(), this::fixTag))
        );
    }

    private Dynamic<?> fixTag(Dynamic<?> param0) {
        Optional<? extends Dynamic<?>> var0 = param0.get("ench")
            .asStreamOpt()
            .map(param0x -> param0x.map(param0xx -> param0xx.set("id", param0xx.createString(MAP.getOrDefault(param0xx.get("id").asInt(0), "null")))))
            .map(param0::createList)
            .result();
        if (var0.isPresent()) {
            param0 = param0.remove("ench").set("Enchantments", var0.get());
        }

        return param0.update(
            "StoredEnchantments",
            param0x -> DataFixUtils.orElse(
                    param0x.asStreamOpt()
                        .map(
                            param0xx -> param0xx.map(
                                    param0xxx -> param0xxx.set("id", param0xxx.createString(MAP.getOrDefault(param0xxx.get("id").asInt(0), "null")))
                                )
                        )
                        .map(param0x::createList)
                        .result(),
                    param0x
                )
        );
    }
}
