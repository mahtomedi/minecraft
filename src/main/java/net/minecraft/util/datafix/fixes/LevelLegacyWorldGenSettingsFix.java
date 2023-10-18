package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;

public class LevelLegacyWorldGenSettingsFix extends DataFix {
    private static final String WORLD_GEN_SETTINGS = "WorldGenSettings";
    private static final List<String> OLD_SETTINGS_KEYS = List.of(
        "RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest"
    );

    public LevelLegacyWorldGenSettingsFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "LevelLegacyWorldGenSettingsFix", this.getInputSchema().getType(References.LEVEL), param0 -> param0.update(DSL.remainderFinder(), param0x -> {
                    Dynamic<?> var0x = param0x.get("WorldGenSettings").orElseEmptyMap();
    
                    for(String var1 : OLD_SETTINGS_KEYS) {
                        Optional<? extends Dynamic<?>> var2 = param0x.get(var1).result();
                        if (var2.isPresent()) {
                            param0x = param0x.remove(var1);
                            var0x = var0x.set(var1, var2.get());
                        }
                    }
    
                    return param0x.set("WorldGenSettings", var0x);
                })
        );
    }
}
