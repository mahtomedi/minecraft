package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;

public abstract class PoiTypeRename extends DataFix {
    public PoiTypeRename(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> var0 = DSL.named(References.POI_CHUNK.typeName(), DSL.remainderType());
        if (!Objects.equals(var0, this.getInputSchema().getType(References.POI_CHUNK))) {
            throw new IllegalStateException("Poi type is not what was expected.");
        } else {
            return this.fixTypeEverywhere("POI rename", var0, param0 -> param0x -> param0x.mapSecond(this::cap));
        }
    }

    private <T> Dynamic<T> cap(Dynamic<T> param0) {
        return param0.update(
            "Sections",
            param0x -> param0x.updateMapValues(
                    param0xx -> param0xx.mapSecond(
                            param0xxx -> param0xxx.update("Records", param0xxxx -> DataFixUtils.orElse(this.renameRecords(param0xxxx), param0xxxx))
                        )
                )
        );
    }

    private <T> Optional<Dynamic<T>> renameRecords(Dynamic<T> param0) {
        return param0.asStreamOpt()
            .map(
                param1 -> param0.createList(
                        param1.map(
                            param0x -> param0x.update(
                                    "type", param0xx -> DataFixUtils.orElse(param0xx.asString().map(this::rename).map(param0xx::createString), param0xx)
                                )
                        )
                    )
            );
    }

    protected abstract String rename(String var1);
}
