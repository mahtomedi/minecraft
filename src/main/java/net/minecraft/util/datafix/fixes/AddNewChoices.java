package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import java.util.Locale;

public class AddNewChoices extends DataFix {
    private final String name;
    private final TypeReference type;

    public AddNewChoices(Schema param0, String param1, TypeReference param2) {
        super(param0, true);
        this.name = param1;
        this.type = param2;
    }

    @Override
    public TypeRewriteRule makeRule() {
        TaggedChoiceType<?> var0 = this.getInputSchema().findChoiceType(this.type);
        TaggedChoiceType<?> var1 = this.getOutputSchema().findChoiceType(this.type);
        return this.cap(this.name, var0, var1);
    }

    protected final <K> TypeRewriteRule cap(String param0, TaggedChoiceType<K> param1, TaggedChoiceType<?> param2) {
        if (param1.getKeyType() != param2.getKeyType()) {
            throw new IllegalStateException("Could not inject: key type is not the same");
        } else {
            return this.fixTypeEverywhere(param0, param1, param2, param1x -> param1xx -> {
                    if (!param2.hasType(param1xx.getFirst())) {
                        throw new IllegalArgumentException(String.format(Locale.ROOT, "Unknown type %s in %s ", param1xx.getFirst(), this.type));
                    } else {
                        return param1xx;
                    }
                });
        }
    }
}
