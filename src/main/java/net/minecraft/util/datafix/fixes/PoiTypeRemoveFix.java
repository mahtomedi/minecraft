package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PoiTypeRemoveFix extends AbstractPoiSectionFix {
    private final Predicate<String> typesToKeep;

    public PoiTypeRemoveFix(Schema param0, String param1, Predicate<String> param2) {
        super(param0, param1);
        this.typesToKeep = param2.negate();
    }

    @Override
    protected <T> Stream<Dynamic<T>> processRecords(Stream<Dynamic<T>> param0) {
        return param0.filter(this::shouldKeepRecord);
    }

    private <T> boolean shouldKeepRecord(Dynamic<T> param0x) {
        return param0x.get("type").asString().result().filter(this.typesToKeep).isPresent();
    }
}
