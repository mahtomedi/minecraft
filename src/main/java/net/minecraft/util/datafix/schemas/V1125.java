package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1125 extends NamespacedSchema {
    public V1125(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerBlockEntities(param0);
        param0.registerSimple(var0, "minecraft:bed");
        return var0;
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        super.registerTypes(param0, param1, param2);
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
        param0.registerType(false, References.BIOME, () -> DSL.constType(DSL.namespacedString()));
        param0.registerType(false, References.ENTITY_NAME, () -> DSL.constType(DSL.namespacedString()));
    }
}
