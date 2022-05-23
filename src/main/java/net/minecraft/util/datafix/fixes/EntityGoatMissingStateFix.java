package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class EntityGoatMissingStateFix extends NamedEntityFix {
    public EntityGoatMissingStateFix(Schema param0) {
        super(param0, false, "EntityGoatMissingStateFix", References.ENTITY, "minecraft:goat");
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(
            DSL.remainderFinder(), param0x -> param0x.set("HasLeftHorn", param0x.createBoolean(true)).set("HasRightHorn", param0x.createBoolean(true))
        );
    }
}
