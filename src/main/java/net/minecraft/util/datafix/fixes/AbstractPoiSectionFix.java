package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class AbstractPoiSectionFix extends DataFix {
    private final String name;

    public AbstractPoiSectionFix(Schema param0, String param1) {
        super(param0, false);
        this.name = param1;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> var0 = DSL.named(References.POI_CHUNK.typeName(), DSL.remainderType());
        if (!Objects.equals(var0, this.getInputSchema().getType(References.POI_CHUNK))) {
            throw new IllegalStateException("Poi type is not what was expected.");
        } else {
            return this.fixTypeEverywhere(this.name, var0, param0 -> param0x -> param0x.mapSecond(this::cap));
        }
    }

    private <T> Dynamic<T> cap(Dynamic<T> param0) {
        return param0.update("Sections", param0x -> param0x.updateMapValues(param0xx -> param0xx.mapSecond(this::processSection)));
    }

    private Dynamic<?> processSection(Dynamic<?> param0) {
        return param0.update("Records", this::processSectionRecords);
    }

    private <T> Dynamic<T> processSectionRecords(Dynamic<T> param0x) {
        return DataFixUtils.orElse(param0x.asStreamOpt().result().map(param1 -> param0x.createList(this.processRecords(param1))), param0x);
    }

    protected abstract <T> Stream<Dynamic<T>> processRecords(Stream<Dynamic<T>> var1);
}
