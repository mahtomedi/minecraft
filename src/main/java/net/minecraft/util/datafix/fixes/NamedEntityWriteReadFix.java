package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.Util;

public abstract class NamedEntityWriteReadFix extends DataFix {
    private final String name;
    private final String entityName;
    private final TypeReference type;

    public NamedEntityWriteReadFix(Schema param0, boolean param1, String param2, TypeReference param3, String param4) {
        super(param0, param1);
        this.name = param2;
        this.type = param3;
        this.entityName = param4;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(this.type);
        Type<?> var1 = this.getInputSchema().getChoiceType(this.type, this.entityName);
        Type<?> var2 = this.getOutputSchema().getType(this.type);
        Type<?> var3 = this.getOutputSchema().getChoiceType(this.type, this.entityName);
        OpticFinder<?> var4 = DSL.namedChoice(this.entityName, var1);
        return this.fixTypeEverywhereTyped(
            this.name, var0, var2, param2 -> param2.updateTyped(var4, var3, param1x -> Util.writeAndReadTypedOrThrow(param1x, var3, this::fix))
        );
    }

    protected abstract <T> Dynamic<T> fix(Dynamic<T> var1);
}
