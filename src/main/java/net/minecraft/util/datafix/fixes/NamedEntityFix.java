package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;

public abstract class NamedEntityFix extends DataFix {
    private final String name;
    private final String entityName;
    private final TypeReference type;

    public NamedEntityFix(Schema param0, boolean param1, String param2, TypeReference param3, String param4) {
        super(param0, param1);
        this.name = param2;
        this.type = param3;
        this.entityName = param4;
    }

    @Override
    public TypeRewriteRule makeRule() {
        OpticFinder<?> var0 = DSL.namedChoice(this.entityName, this.getInputSchema().getChoiceType(this.type, this.entityName));
        return this.fixTypeEverywhereTyped(
            this.name,
            this.getInputSchema().getType(this.type),
            this.getOutputSchema().getType(this.type),
            param1 -> param1.updateTyped(var0, this.getOutputSchema().getChoiceType(this.type, this.entityName), this::fix)
        );
    }

    protected abstract Typed<?> fix(Typed<?> var1);
}
