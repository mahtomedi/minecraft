package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;
import java.util.function.IntFunction;

public class EntityVariantFix extends NamedEntityFix {
    private final String fieldName;
    private final IntFunction<String> idConversions;

    public EntityVariantFix(Schema param0, String param1, TypeReference param2, String param3, String param4, IntFunction<String> param5) {
        super(param0, false, param1, param2, param3);
        this.fieldName = param4;
        this.idConversions = param5;
    }

    private static <T> Dynamic<T> updateAndRename(Dynamic<T> param0, String param1, String param2, Function<Dynamic<T>, Dynamic<T>> param3) {
        return param0.map(param4 -> {
            DynamicOps<T> var0x = param0.getOps();
            Function<T, T> var1x = param2x -> param3.apply(new Dynamic<>(var0x, (T)param2x)).getValue();
            return var0x.get(param4, param1).map(param4x -> (T)var0x.set(param4, param2, var1x.apply(param4x))).result().orElse(param4);
        });
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(
            DSL.remainderFinder(),
            param0x -> updateAndRename(
                    param0x,
                    this.fieldName,
                    "variant",
                    param0xx -> DataFixUtils.orElse(
                            param0xx.asNumber().map(param1 -> param0xx.createString(this.idConversions.apply(param1.intValue()))).result(), param0xx
                        )
                )
        );
    }
}
