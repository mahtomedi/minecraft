package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class StatsRenameFix extends DataFix {
    private final String name;
    private final Map<String, String> renames;

    public StatsRenameFix(Schema param0, String param1, Map<String, String> param2) {
        super(param0, false);
        this.name = param1;
        this.renames = param2;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getOutputSchema().getType(References.STATS);
        Type<?> var1 = this.getInputSchema().getType(References.STATS);
        OpticFinder<?> var2 = var1.findField("stats");
        OpticFinder<?> var3 = var2.type().findField("minecraft:custom");
        OpticFinder<String> var4 = NamespacedSchema.namespacedString().finder();
        return this.fixTypeEverywhereTyped(
            this.name, var1, var0, param3 -> param3.updateTyped(var2, param2x -> param2x.updateTyped(var3, param1x -> param1x.update(var4, param0x -> {
                            for(Entry<String, String> var0x : this.renames.entrySet()) {
                                if (param0x.equals(var0x.getKey())) {
                                    return var0x.getValue();
                                }
                            }
    
                            return param0x;
                        })))
        );
    }
}
