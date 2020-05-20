package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.stream.Stream;

public class SavedDataVillageCropFix extends DataFix {
    public SavedDataVillageCropFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.writeFixAndRead(
            "SavedDataVillageCropFix",
            this.getInputSchema().getType(References.STRUCTURE_FEATURE),
            this.getOutputSchema().getType(References.STRUCTURE_FEATURE),
            this::fixTag
        );
    }

    private <T> Dynamic<T> fixTag(Dynamic<T> param0) {
        return param0.update("Children", SavedDataVillageCropFix::updateChildren);
    }

    private static <T> Dynamic<T> updateChildren(Dynamic<T> param0x) {
        return param0x.asStreamOpt().map(SavedDataVillageCropFix::updateChildren).map(param0x::createList).result().orElse(param0x);
    }

    private static Stream<? extends Dynamic<?>> updateChildren(Stream<? extends Dynamic<?>> param0x) {
        return param0x.map(param0xx -> {
            String var0x = param0xx.get("id").asString("");
            if ("ViF".equals(var0x)) {
                return updateSingleField(param0xx);
            } else {
                return "ViDF".equals(var0x) ? updateDoubleField(param0xx) : param0xx;
            }
        });
    }

    private static <T> Dynamic<T> updateSingleField(Dynamic<T> param0) {
        param0 = updateCrop(param0, "CA");
        return updateCrop(param0, "CB");
    }

    private static <T> Dynamic<T> updateDoubleField(Dynamic<T> param0) {
        param0 = updateCrop(param0, "CA");
        param0 = updateCrop(param0, "CB");
        param0 = updateCrop(param0, "CC");
        return updateCrop(param0, "CD");
    }

    private static <T> Dynamic<T> updateCrop(Dynamic<T> param0, String param1) {
        return param0.get(param1).asNumber().result().isPresent() ? param0.set(param1, BlockStateData.getTag(param0.get(param1).asInt(0) << 4)) : param0;
    }
}
