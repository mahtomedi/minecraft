package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class ForcePoiRebuild extends DataFix {
    public ForcePoiRebuild(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> var0 = DSL.named(References.POI_CHUNK.typeName(), DSL.remainderType());
        if (!Objects.equals(var0, this.getInputSchema().getType(References.POI_CHUNK))) {
            throw new IllegalStateException("Poi type is not what was expected.");
        } else {
            return this.fixTypeEverywhere("POI rebuild", var0, param0 -> param0x -> param0x.mapSecond(ForcePoiRebuild::cap));
        }
    }

    private static <T> Dynamic<T> cap(Dynamic<T> param0) {
        return param0.update("Sections", param0x -> param0x.updateMapValues(param0xx -> param0xx.mapSecond(param0xxx -> param0xxx.remove("Valid"))));
    }
}
