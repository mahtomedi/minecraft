package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class SwimStatsRenameFix extends DataFix {
    public SwimStatsRenameFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getOutputSchema().getType(References.STATS);
        Type<?> var1 = this.getInputSchema().getType(References.STATS);
        OpticFinder<?> var2 = var1.findField("stats");
        OpticFinder<?> var3 = var2.type().findField("minecraft:custom");
        OpticFinder<String> var4 = NamespacedSchema.namespacedString().finder();
        return this.fixTypeEverywhereTyped(
            "SwimStatsRenameFix",
            var1,
            var0,
            param3 -> param3.updateTyped(var2, param2x -> param2x.updateTyped(var3, param1x -> param1x.update(var4, param0x -> {
                            if (param0x.equals("minecraft:swim_one_cm")) {
                                return "minecraft:walk_on_water_one_cm";
                            } else {
                                return param0x.equals("minecraft:dive_one_cm") ? "minecraft:walk_under_water_one_cm" : param0x;
                            }
                        })))
        );
    }
}
