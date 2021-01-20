package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class CauldronRenameFix extends DataFix {
    public CauldronRenameFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    private static Dynamic<?> fix(Dynamic<?> param0) {
        Optional<String> var0 = param0.get("Name").asString().result();
        if (var0.equals(Optional.of("minecraft:cauldron"))) {
            Dynamic<?> var1 = param0.get("Properties").orElseEmptyMap();
            return var1.get("level").asString("0").equals("0")
                ? param0.remove("Properties")
                : param0.set("Name", param0.createString("minecraft:water_cauldron"));
        } else {
            return param0;
        }
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "cauldron_rename_fix",
            this.getInputSchema().getType(References.BLOCK_STATE),
            param0 -> param0.update(DSL.remainderFinder(), CauldronRenameFix::fix)
        );
    }
}
