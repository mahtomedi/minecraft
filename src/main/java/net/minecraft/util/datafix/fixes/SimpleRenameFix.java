package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class SimpleRenameFix extends DataFix {
    private final String fixerName;
    private final Map<String, String> nameMapping;
    private final TypeReference typeReference;

    public SimpleRenameFix(Schema param0, TypeReference param1, Map<String, String> param2) {
        this(param0, param1, param1.typeName() + "-renames at version: " + param0.getVersionKey(), param2);
    }

    public SimpleRenameFix(Schema param0, TypeReference param1, String param2, Map<String, String> param3) {
        super(param0, false);
        this.nameMapping = param3;
        this.fixerName = param2;
        this.typeReference = param1;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, String>> var0 = DSL.named(this.typeReference.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(var0, this.getInputSchema().getType(this.typeReference))) {
            throw new IllegalStateException("\"" + this.typeReference.typeName() + "\" type is not what was expected.");
        } else {
            return this.fixTypeEverywhere(
                this.fixerName, var0, param0 -> param0x -> param0x.mapSecond(param0xx -> this.nameMapping.getOrDefault(param0xx, param0xx))
            );
        }
    }
}
