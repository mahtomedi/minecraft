package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class EntityItemFrameDirectionFix extends NamedEntityFix {
    public EntityItemFrameDirectionFix(Schema param0, boolean param1) {
        super(param0, param1, "EntityItemFrameDirectionFix", References.ENTITY, "minecraft:item_frame");
    }

    public Dynamic<?> fixTag(Dynamic<?> param0) {
        return param0.set("Facing", param0.createByte(direction2dTo3d(param0.get("Facing").asByte((byte)0))));
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), this::fixTag);
    }

    private static byte direction2dTo3d(byte param0) {
        switch(param0) {
            case 0:
                return 3;
            case 1:
                return 4;
            case 2:
            default:
                return 2;
            case 3:
                return 5;
        }
    }
}
