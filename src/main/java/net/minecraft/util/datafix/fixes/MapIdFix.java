package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Optional;

public class MapIdFix extends DataFix {
    public MapIdFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.SAVED_DATA);
        OpticFinder<?> var1 = var0.findField("data");
        return this.fixTypeEverywhereTyped(
            "Map id fix",
            var0,
            param1 -> {
                Optional<? extends Typed<?>> var0x = param1.getOptionalTyped(var1);
                return var0x.isPresent()
                    ? param1
                    : param1.update(DSL.remainderFinder(), param0x -> param0x.emptyMap().merge(param0x.createString("data"), param0x));
            }
        );
    }
}
