package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import java.util.stream.Stream;

public class PoiTypeRenameFix extends AbstractPoiSectionFix {
    private final Function<String, String> renamer;

    public PoiTypeRenameFix(Schema param0, String param1, Function<String, String> param2) {
        super(param0, param1);
        this.renamer = param2;
    }

    @Override
    protected <T> Stream<Dynamic<T>> processRecords(Stream<Dynamic<T>> param0) {
        return param0.map(
            param0x -> param0x.update(
                    "type", param0xx -> DataFixUtils.orElse(param0xx.asString().map(this.renamer).map(param0xx::createString).result(), param0xx)
                )
        );
    }
}
