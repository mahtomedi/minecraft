package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class FilteredSignsFix extends NamedEntityFix {
    public FilteredSignsFix(Schema param0) {
        super(param0, false, "Remove filtered text from signs", References.BLOCK_ENTITY, "minecraft:sign");
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(
            DSL.remainderFinder(), param0x -> param0x.remove("FilteredText1").remove("FilteredText2").remove("FilteredText3").remove("FilteredText4")
        );
    }
}
