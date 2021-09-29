package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.function.Function;

public class WorldGenSettingsDisallowOldCustomWorldsFix extends DataFix {
    public WorldGenSettingsDisallowOldCustomWorldsFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.WORLD_GEN_SETTINGS);
        OpticFinder<?> var1 = var0.findField("dimensions");
        return this.fixTypeEverywhereTyped(
            "WorldGenSettingsDisallowOldCustomWorldsFix",
            var0,
            param1 -> param1.updateTyped(
                    var1,
                    param0x -> {
                        param0x.write()
                            .map(
                                param0xx -> param0xx.getMapValues()
                                        .map((Function<? super Map<Dynamic<?>, Dynamic<?>>, ? extends Map<Dynamic<?>, Dynamic<?>>>)(param0xxx -> {
                                            param0xxx.forEach((param0xxxx, param1x) -> {
                                                if (param1x.get("type").asString().result().isEmpty()) {
                                                    throw new IllegalStateException("Unable load old custom worlds.");
                                                }
                                            });
                                            return param0xxx;
                                        }))
                            );
                        return param0x;
                    }
                )
        );
    }
}
