package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2842 extends NamespacedSchema {
    public V2842(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        super.registerTypes(param0, param1, param2);
        param0.registerType(
            false,
            References.CHUNK,
            () -> DSL.optionalFields(
                    "entities",
                    DSL.list(References.ENTITY_TREE.in(param0)),
                    "block_entities",
                    DSL.list(References.BLOCK_ENTITY.in(param0)),
                    "block_ticks",
                    DSL.list(DSL.fields("i", References.BLOCK_NAME.in(param0))),
                    "sections",
                    DSL.list(
                        DSL.optionalFields(
                            "biomes",
                            DSL.optionalFields("palette", DSL.list(References.BIOME.in(param0))),
                            "block_states",
                            DSL.optionalFields("palette", DSL.list(References.BLOCK_STATE.in(param0)))
                        )
                    ),
                    "structures",
                    DSL.optionalFields("starts", DSL.compoundList(References.STRUCTURE_FEATURE.in(param0)))
                )
        );
    }
}
