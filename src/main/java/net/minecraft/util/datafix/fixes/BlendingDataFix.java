package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.SectionPos;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlendingDataFix extends DataFix {
    private final String name;
    private static final Set<String> STATUSES_TO_SKIP_BLENDING = Set.of(
        "minecraft:empty", "minecraft:structure_starts", "minecraft:structure_references", "minecraft:biomes"
    );

    public BlendingDataFix(Schema param0, String param1) {
        super(param0, false);
        this.name = param1;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getOutputSchema().getType(References.CHUNK);
        return this.fixTypeEverywhereTyped(this.name, var0, param0 -> param0.update(DSL.remainderFinder(), BlendingDataFix::updateChunkTag));
    }

    private static Dynamic<?> updateChunkTag(Dynamic<?> param0) {
        param0 = param0.remove("blending_data");
        Optional<? extends Dynamic<?>> var0 = param0.get("Status").result();
        if (var0.isPresent()) {
            String var1 = NamespacedSchema.ensureNamespaced(var0.get().asString("empty"));
            Optional<? extends Dynamic<?>> var2 = param0.get("below_zero_retrogen").result();
            if (!STATUSES_TO_SKIP_BLENDING.contains(var1)) {
                param0 = updateBlendingData(param0, 384, -64);
            } else if (var2.isPresent()) {
                Dynamic<?> var3 = var2.get();
                String var4 = NamespacedSchema.ensureNamespaced(var3.get("target_status").asString("empty"));
                if (!STATUSES_TO_SKIP_BLENDING.contains(var4)) {
                    param0 = updateBlendingData(param0, 256, 0);
                }
            }
        }

        return param0;
    }

    private static Dynamic<?> updateBlendingData(Dynamic<?> param0, int param1, int param2) {
        return param0.set(
            "blending_data",
            param0.createMap(
                Map.of(
                    param0.createString("min_section"),
                    param0.createInt(SectionPos.blockToSectionCoord(param2)),
                    param0.createString("max_section"),
                    param0.createInt(SectionPos.blockToSectionCoord(param2 + param1))
                )
            )
        );
    }
}
