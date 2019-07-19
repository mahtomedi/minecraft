package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1466 extends NamespacedSchema {
    public V1466(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        super.registerTypes(param0, param1, param2);
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
                        DSL.list(DSL.fields("i", References.BLOCK_NAME.in(param0))),
                        "Sections",
                        DSL.list(DSL.optionalFields("Palette", DSL.list(References.BLOCK_STATE.in(param0)))),
                        "Structures",
                        DSL.optionalFields("Starts", DSL.compoundList(References.STRUCTURE_FEATURE.in(param0)))
                    )
                )
        );
        param0.registerType(
            false,
            References.STRUCTURE_FEATURE,
            () -> DSL.optionalFields(
                    "Children",
                    DSL.list(
                        DSL.optionalFields(
                            "CA",
                            References.BLOCK_STATE.in(param0),
                            "CB",
                            References.BLOCK_STATE.in(param0),
                            "CC",
                            References.BLOCK_STATE.in(param0),
                            "CD",
                            References.BLOCK_STATE.in(param0)
                        )
                    ),
                    "biome",
                    References.BIOME.in(param0)
                )
        );
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerBlockEntities(param0);
        var0.put("DUMMY", DSL::remainder);
        return var0;
    }
}
