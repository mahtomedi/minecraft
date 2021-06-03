package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import java.util.Map;
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
        return TypeRewriteRule.seq(this.createStatRule(), this.createCriteriaRule());
    }

    private TypeRewriteRule createCriteriaRule() {
        Type<?> var0 = this.getOutputSchema().getType(References.OBJECTIVE);
        Type<?> var1 = this.getInputSchema().getType(References.OBJECTIVE);
        OpticFinder<?> var2 = var1.findField("CriteriaType");
        TaggedChoiceType<?> var3 = var2.type().findChoiceType("type", -1).orElseThrow(() -> new IllegalStateException("Can't find choice type for criteria"));
        Type<?> var4 = var3.types().get("minecraft:custom");
        if (var4 == null) {
            throw new IllegalStateException("Failed to find custom criterion type variant");
        } else {
            OpticFinder<?> var5 = DSL.namedChoice("minecraft:custom", var4);
            OpticFinder<String> var6 = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
            return this.fixTypeEverywhereTyped(
                this.name,
                var1,
                var0,
                param3 -> param3.updateTyped(
                        var2, param2x -> param2x.updateTyped(var5, param1x -> param1x.update(var6, param0x -> this.renames.getOrDefault(param0x, param0x)))
                    )
            );
        }
    }

    private TypeRewriteRule createStatRule() {
        Type<?> var0 = this.getOutputSchema().getType(References.STATS);
        Type<?> var1 = this.getInputSchema().getType(References.STATS);
        OpticFinder<?> var2 = var1.findField("stats");
        OpticFinder<?> var3 = var2.type().findField("minecraft:custom");
        OpticFinder<String> var4 = NamespacedSchema.namespacedString().finder();
        return this.fixTypeEverywhereTyped(
            this.name,
            var1,
            var0,
            param3 -> param3.updateTyped(
                    var2, param2x -> param2x.updateTyped(var3, param1x -> param1x.update(var4, param0x -> this.renames.getOrDefault(param0x, param0x)))
                )
        );
    }
}
