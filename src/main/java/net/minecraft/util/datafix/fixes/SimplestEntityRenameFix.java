package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.datafixers.util.Pair;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class SimplestEntityRenameFix extends DataFix {
    private final String name;

    public SimplestEntityRenameFix(String param0, Schema param1, boolean param2) {
        super(param1, param2);
        this.name = param0;
    }

    @Override
    public TypeRewriteRule makeRule() {
        TaggedChoiceType<String> var0 = this.getInputSchema().findChoiceType(References.ENTITY);
        TaggedChoiceType<String> var1 = this.getOutputSchema().findChoiceType(References.ENTITY);
        Type<Pair<String, String>> var2 = DSL.named(References.ENTITY_NAME.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(this.getOutputSchema().getType(References.ENTITY_NAME), var2)) {
            throw new IllegalStateException("Entity name type is not what was expected.");
        } else {
            return TypeRewriteRule.seq(this.fixTypeEverywhere(this.name, var0, var1, param2 -> param2x -> param2x.mapFirst(param2xx -> {
                        String var0x = this.rename(param2xx);
                        Type<?> var1x = var0.types().get(param2xx);
                        Type<?> var2x = var1.types().get(var0x);
                        if (!var2x.equals(var1x, true, true)) {
                            throw new IllegalStateException(String.format(Locale.ROOT, "Dynamic type check failed: %s not equal to %s", var2x, var1x));
                        } else {
                            return var0x;
                        }
                    })), this.fixTypeEverywhere(this.name + " for entity name", var2, param0 -> param0x -> param0x.mapSecond(this::rename)));
        }
    }

    protected abstract String rename(String var1);
}
