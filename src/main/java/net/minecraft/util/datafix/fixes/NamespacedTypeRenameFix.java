package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class NamespacedTypeRenameFix extends DataFix {
    private final String name;
    private final TypeReference type;
    private final UnaryOperator<String> renamer;

    public NamespacedTypeRenameFix(Schema param0, String param1, TypeReference param2, UnaryOperator<String> param3) {
        super(param0, false);
        this.name = param1;
        this.type = param2;
        this.renamer = param3;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, String>> var0 = DSL.named(this.type.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(var0, this.getInputSchema().getType(this.type))) {
            throw new IllegalStateException("\"" + this.type.typeName() + "\" is not what was expected.");
        } else {
            return this.fixTypeEverywhere(this.name, var0, param0 -> param0x -> param0x.mapSecond(this.renamer));
        }
    }
}
