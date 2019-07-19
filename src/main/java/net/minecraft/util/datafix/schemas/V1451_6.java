package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1451_6 extends NamespacedSchema {
    public V1451_6(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        super.registerTypes(param0, param1, param2);
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
                            DSL.compoundList(DSL.constType(DSL.namespacedString()), DSL.constType(DSL.intType()))
                        )
                    )
                )
        );
    }
}
