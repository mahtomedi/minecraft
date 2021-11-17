package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Supplier;

public class SavedDataUUIDFix extends AbstractUUIDFix {
    public SavedDataUUIDFix(Schema param0) {
        super(param0, References.SAVED_DATA);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "SavedDataUUIDFix",
            this.getInputSchema().getType(this.typeReference),
            param0 -> param0.updateTyped(
                    param0.getType().findField("data"),
                    param0x -> param0x.update(
                            DSL.remainderFinder(),
                            param0xx -> param0xx.update(
                                    "Raids",
                                    param0xxx -> param0xxx.createList(
                                            param0xxx.asStream()
                                                .map(
                                                    param0xxxx -> param0xxxx.update(
                                                            "HeroesOfTheVillage",
                                                            param0xxxxx -> param0xxxxx.createList(
                                                                    param0xxxxx.asStream()
                                                                        .map(
                                                                            param0xxxxxx -> createUUIDFromLongs(param0xxxxxx, "UUIDMost", "UUIDLeast")
                                                                                    .orElseGet((Supplier<? extends Dynamic<?>>)(() -> {
                                                                                        LOGGER.warn("HeroesOfTheVillage contained invalid UUIDs.");
                                                                                        return param0xxxxxx;
                                                                                    }))
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }
}
