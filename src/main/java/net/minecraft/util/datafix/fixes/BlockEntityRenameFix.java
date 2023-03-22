package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import java.util.function.UnaryOperator;

public class BlockEntityRenameFix extends DataFix {
    private final String name;
    private final UnaryOperator<String> nameChangeLookup;

    private BlockEntityRenameFix(Schema param0, String param1, UnaryOperator<String> param2) {
        super(param0, true);
        this.name = param1;
        this.nameChangeLookup = param2;
    }

    @Override
    public TypeRewriteRule makeRule() {
        TaggedChoiceType<String> var0 = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        TaggedChoiceType<String> var1 = this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY);
        return this.fixTypeEverywhere(this.name, var0, var1, param0 -> param0x -> param0x.mapFirst(this.nameChangeLookup));
    }

    public static DataFix create(Schema param0, String param1, UnaryOperator<String> param2) {
        return new BlockEntityRenameFix(param0, param1, param2);
    }
}
