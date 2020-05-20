package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ReorganizePoi extends DataFix {
    public ReorganizePoi(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> var0 = DSL.named(References.POI_CHUNK.typeName(), DSL.remainderType());
        if (!Objects.equals(var0, this.getInputSchema().getType(References.POI_CHUNK))) {
            throw new IllegalStateException("Poi type is not what was expected.");
        } else {
            return this.fixTypeEverywhere("POI reorganization", var0, param0 -> param0x -> param0x.mapSecond(ReorganizePoi::cap));
        }
    }

    private static <T> Dynamic<T> cap(Dynamic<T> param0) {
        Map<Dynamic<T>, Dynamic<T>> var0 = Maps.newHashMap();

        for(int var1 = 0; var1 < 16; ++var1) {
            String var2 = String.valueOf(var1);
            Optional<Dynamic<T>> var3 = param0.get(var2).result();
            if (var3.isPresent()) {
                Dynamic<T> var4 = var3.get();
                Dynamic<T> var5 = param0.createMap(ImmutableMap.of(param0.createString("Records"), var4));
                var0.put(param0.createInt(var1), var5);
                param0 = param0.remove(var2);
            }
        }

        return param0.set("Sections", param0.createMap(var0));
    }
}
