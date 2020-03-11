package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2501 extends NamespacedSchema {
    public V2501(int param0, Schema param1) {
        super(param0, param1);
    }

    private static void registerFurnace(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(
            param1,
            param2,
            () -> DSL.optionalFields(
                    "Items",
                    DSL.list(References.ITEM_STACK.in(param0)),
                    "RecipesUsed",
                    DSL.compoundList(References.RECIPE.in(param0), DSL.constType(DSL.intType()))
                )
        );
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerBlockEntities(param0);
        registerFurnace(param0, var0, "minecraft:furnace");
        registerFurnace(param0, var0, "minecraft:smoker");
        registerFurnace(param0, var0, "minecraft:blast_furnace");
        return var0;
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        super.registerTypes(param0, param1, param2);
        Map<String, Supplier<TypeTemplate>> var0 = ImmutableMap.<String, Supplier<TypeTemplate>>builder()
            .put("default", DSL::remainder)
            .put("largeBiomes", DSL::remainder)
            .put("amplified", DSL::remainder)
            .put("customized", DSL::remainder)
            .put("debug_all_block_states", DSL::remainder)
            .put("default_1_1", DSL::remainder)
            .put(
                "flat",
                () -> DSL.optionalFields(
                        "biome", References.BIOME.in(param0), "layers", DSL.list(DSL.optionalFields("block", References.BLOCK_NAME.in(param0)))
                    )
            )
            .put(
                "buffet",
                () -> DSL.optionalFields(
                        "biome_source",
                        DSL.optionalFields("options", DSL.optionalFields("biomes", DSL.list(References.BIOME.in(param0)))),
                        "chunk_generator",
                        DSL.optionalFields(
                            "options", DSL.optionalFields("default_block", References.BLOCK_NAME.in(param0), "default_fluid", References.BLOCK_NAME.in(param0))
                        )
                    )
            )
            .build();
        param0.registerType(false, References.CHUNK_GENERATOR_SETTINGS, () -> DSL.taggedChoiceLazy("levelType", DSL.string(), var0));
    }
}
