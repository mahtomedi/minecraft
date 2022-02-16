package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;

public class OverreachingTickFix extends DataFix {
    public OverreachingTickFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> var1 = var0.findField("block_ticks");
        return this.fixTypeEverywhereTyped("Handle ticks saved in the wrong chunk", var0, param1 -> {
            Optional<? extends Typed<?>> var0x = param1.getOptionalTyped(var1);
            Optional<? extends Dynamic<?>> var1x = var0x.isPresent() ? var0x.get().write().result() : Optional.empty();
            return param1.update(DSL.remainderFinder(), param1x -> {
                int var0xx = param1x.get("xPos").asInt(0);
                int var1xx = param1x.get("zPos").asInt(0);
                Optional<? extends Dynamic<?>> var2x = param1x.get("fluid_ticks").get().result();
                param1x = extractOverreachingTicks(param1x, var0xx, var1xx, var1x, "neighbor_block_ticks");
                return extractOverreachingTicks(param1x, var0xx, var1xx, var2x, "neighbor_fluid_ticks");
            });
        });
    }

    private static Dynamic<?> extractOverreachingTicks(Dynamic<?> param0, int param1, int param2, Optional<? extends Dynamic<?>> param3, String param4) {
        if (param3.isPresent()) {
            List<? extends Dynamic<?>> var0 = param3.get().asStream().filter(param2x -> {
                int var0x = param2x.get("x").asInt(0);
                int var1x = param2x.get("z").asInt(0);
                int var2x = Math.abs(param1 - (var0x >> 4));
                int var3x = Math.abs(param2 - (var1x >> 4));
                return (var2x != 0 || var3x != 0) && var2x <= 1 && var3x <= 1;
            }).toList();
            if (!var0.isEmpty()) {
                param0 = param0.set("UpgradeData", param0.get("UpgradeData").orElseEmptyMap().set(param4, param0.createList(var0.stream())));
            }
        }

        return param0;
    }
}
