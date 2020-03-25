package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class JigsawPropertiesFix extends NamedEntityFix {
    public JigsawPropertiesFix(Schema param0, boolean param1) {
        super(param0, param1, "JigsawPropertiesFix", References.BLOCK_ENTITY, "minecraft:jigsaw");
    }

    private static Dynamic<?> fixTag(Dynamic<?> param0) {
        String var0 = param0.get("attachement_type").asString("minecraft:empty");
        String var1 = param0.get("target_pool").asString("minecraft:empty");
        return param0.set("name", param0.createString(var0))
            .set("target", param0.createString(var0))
            .remove("attachement_type")
            .set("pool", param0.createString(var1))
            .remove("target_pool");
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), JigsawPropertiesFix::fixTag);
    }
}
