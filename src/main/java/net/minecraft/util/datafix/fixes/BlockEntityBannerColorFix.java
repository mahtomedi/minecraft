package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityBannerColorFix extends NamedEntityFix {
    public BlockEntityBannerColorFix(Schema param0, boolean param1) {
        super(param0, param1, "BlockEntityBannerColorFix", References.BLOCK_ENTITY, "minecraft:banner");
    }

    public Dynamic<?> fixTag(Dynamic<?> param0) {
        param0 = param0.update("Base", param0x -> param0x.createInt(15 - param0x.asInt(0)));
        return param0.update(
            "Patterns",
            param0x -> DataFixUtils.orElse(
                    param0x.asStreamOpt()
                        .map(param0xx -> param0xx.map(param0xxx -> param0xxx.update("Color", param0xxxx -> param0xxxx.createInt(15 - param0xxxx.asInt(0)))))
                        .map(param0x::createList)
                        .result(),
                    param0x
                )
        );
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), this::fixTag);
    }
}
