package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import java.util.Locale;

public abstract class EntityRenameFix extends DataFix {
    protected final String name;

    public EntityRenameFix(String param0, Schema param1, boolean param2) {
        super(param1, param2);
        this.name = param0;
    }

    @Override
    public TypeRewriteRule makeRule() {
        TaggedChoiceType<String> var0 = this.getInputSchema().findChoiceType(References.ENTITY);
        TaggedChoiceType<String> var1 = this.getOutputSchema().findChoiceType(References.ENTITY);
        return this.fixTypeEverywhere(
            this.name,
            var0,
            var1,
            param2 -> param3 -> {
                    String var0x = param3.getFirst();
                    Type<?> var1x = var0.types().get(var0x);
                    Pair<String, Typed<?>> var2x = this.fix(var0x, this.getEntity(param3.getSecond(), param2, var1x));
                    Type<?> var3x = var1.types().get(var2x.getFirst());
                    if (!var3x.equals(((Typed)var2x.getSecond()).getType(), true, true)) {
                        throw new IllegalStateException(
                            String.format(Locale.ROOT, "Dynamic type check failed: %s not equal to %s", var3x, ((Typed)var2x.getSecond()).getType())
                        );
                    } else {
                        return Pair.of((String)var2x.getFirst(), ((Typed)var2x.getSecond()).getValue());
                    }
                }
        );
    }

    private <A> Typed<A> getEntity(Object param0, DynamicOps<?> param1, Type<A> param2) {
        return new Typed<>(param2, param1, (A)param0);
    }

    protected abstract Pair<String, Typed<?>> fix(String var1, Typed<?> var2);
}
