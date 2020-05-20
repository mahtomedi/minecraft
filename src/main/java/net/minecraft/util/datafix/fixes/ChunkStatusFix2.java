package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Objects;

public class ChunkStatusFix2 extends DataFix {
    private static final Map<String, String> RENAMES_AND_DOWNGRADES = ImmutableMap.<String, String>builder()
        .put("structure_references", "empty")
        .put("biomes", "empty")
        .put("base", "surface")
        .put("carved", "carvers")
        .put("liquid_carved", "liquid_carvers")
        .put("decorated", "features")
        .put("lighted", "light")
        .put("mobs_spawned", "spawn")
        .put("finalized", "heightmaps")
        .put("fullchunk", "full")
        .build();

    public ChunkStatusFix2(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        Type<?> var1 = var0.findFieldType("Level");
        OpticFinder<?> var2 = DSL.fieldFinder("Level", var1);
        return this.fixTypeEverywhereTyped(
            "ChunkStatusFix2", var0, this.getOutputSchema().getType(References.CHUNK), param1 -> param1.updateTyped(var2, param0x -> {
                    Dynamic<?> var0x = param0x.get(DSL.remainderFinder());
                    String var1x = var0x.get("Status").asString("empty");
                    String var2x = RENAMES_AND_DOWNGRADES.getOrDefault(var1x, "empty");
                    return Objects.equals(var1x, var2x) ? param0x : param0x.set(DSL.remainderFinder(), var0x.set("Status", var0x.createString(var2x)));
                })
        );
    }
}
