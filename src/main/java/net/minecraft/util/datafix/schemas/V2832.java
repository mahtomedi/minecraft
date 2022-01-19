package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2832 extends NamespacedSchema {
    public V2832(int param0, Schema param1) {
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
                        DSL.list(DSL.or(References.BLOCK_ENTITY.in(param0), DSL.remainder())),
                        "TileTicks",
                        DSL.list(DSL.fields("i", References.BLOCK_NAME.in(param0))),
                        "Sections",
                        DSL.list(
                            DSL.optionalFields(
                                "biomes",
                                DSL.optionalFields("palette", DSL.list(References.BIOME.in(param0))),
                                "block_states",
                                DSL.optionalFields("palette", DSL.list(References.BLOCK_STATE.in(param0)))
                            )
                        ),
                        "Structures",
                        DSL.optionalFields("Starts", DSL.compoundList(References.STRUCTURE_FEATURE.in(param0)))
                    )
                )
        );
        param0.registerType(
            false,
            References.WORLD_GEN_SETTINGS,
            () -> DSL.fields(
                    "dimensions",
                    DSL.compoundList(
                        DSL.constType(namespacedString()),
                        DSL.fields(
                            "generator",
                            DSL.taggedChoiceLazy(
                                "type",
                                DSL.string(),
                                ImmutableMap.of(
                                    "minecraft:debug",
                                    DSL::remainder,
                                    "minecraft:flat",
                                    () -> DSL.optionalFields(
                                            "settings",
                                            DSL.optionalFields(
                                                "biome",
                                                References.BIOME.in(param0),
                                                "layers",
                                                DSL.list(DSL.optionalFields("block", References.BLOCK_NAME.in(param0)))
                                            )
                                        ),
                                    "minecraft:noise",
                                    () -> DSL.optionalFields(
                                            "biome_source",
                                            DSL.taggedChoiceLazy(
                                                "type",
                                                DSL.string(),
                                                ImmutableMap.of(
                                                    "minecraft:fixed",
                                                    () -> DSL.fields("biome", References.BIOME.in(param0)),
                                                    "minecraft:multi_noise",
                                                    () -> DSL.or(
                                                            DSL.fields("preset", namespacedString().template()),
                                                            DSL.list(DSL.fields("biome", References.BIOME.in(param0)))
                                                        ),
                                                    "minecraft:checkerboard",
                                                    () -> DSL.fields("biomes", DSL.list(References.BIOME.in(param0))),
                                                    "minecraft:the_end",
                                                    DSL::remainder
                                                )
                                            ),
                                            "settings",
                                            DSL.or(
                                                DSL.constType(DSL.string()),
                                                DSL.optionalFields(
                                                    "default_block", References.BLOCK_NAME.in(param0), "default_fluid", References.BLOCK_NAME.in(param0)
                                                )
                                            )
                                        )
                                )
                            )
                        )
                    )
                )
        );
    }
}
