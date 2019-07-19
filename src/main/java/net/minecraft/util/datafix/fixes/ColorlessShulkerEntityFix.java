package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class ColorlessShulkerEntityFix extends NamedEntityFix {
    public ColorlessShulkerEntityFix(Schema param0, boolean param1) {
        super(param0, param1, "Colorless shulker entity fix", References.ENTITY, "minecraft:shulker");
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(
            DSL.remainderFinder(), param0x -> param0x.get("Color").asInt(0) == 10 ? param0x.set("Color", param0x.createByte((byte)16)) : param0x
        );
    }
}
