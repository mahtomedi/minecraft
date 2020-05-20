package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityArmorStandSilentFix extends NamedEntityFix {
    public EntityArmorStandSilentFix(Schema param0, boolean param1) {
        super(param0, param1, "EntityArmorStandSilentFix", References.ENTITY, "ArmorStand");
    }

    public Dynamic<?> fixTag(Dynamic<?> param0) {
        return param0.get("Silent").asBoolean(false) && !param0.get("Marker").asBoolean(false) ? param0.remove("Silent") : param0;
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), this::fixTag);
    }
}
