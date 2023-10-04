package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class BlockEntitySignDoubleSidedEditableTextFix extends NamedEntityFix {
    public static final String FILTERED_CORRECT = "_filtered_correct";
    private static final String DEFAULT_COLOR = "black";

    public BlockEntitySignDoubleSidedEditableTextFix(Schema param0, String param1, String param2) {
        super(param0, false, param1, References.BLOCK_ENTITY, param2);
    }

    private static <T> Dynamic<T> fixTag(Dynamic<T> param0) {
        return param0.set("front_text", fixFrontTextTag(param0)).set("back_text", createDefaultText(param0)).set("is_waxed", param0.createBoolean(false));
    }

    private static <T> Dynamic<T> fixFrontTextTag(Dynamic<T> param0) {
        Dynamic<T> var0 = ComponentDataFixUtils.createEmptyComponent(param0.getOps());
        List<Dynamic<T>> var1 = getLines(param0, "Text").map(param1 -> param1.orElse(var0)).toList();
        Dynamic<T> var2 = param0.emptyMap()
            .set("messages", param0.createList(var1.stream()))
            .set("color", param0.get("Color").result().orElse(param0.createString("black")))
            .set("has_glowing_text", param0.get("GlowingText").result().orElse(param0.createBoolean(false)))
            .set("_filtered_correct", param0.createBoolean(true));
        List<Optional<Dynamic<T>>> var3 = getLines(param0, "FilteredText").toList();
        if (var3.stream().anyMatch(Optional::isPresent)) {
            var2 = var2.set("filtered_messages", param0.createList(Streams.mapWithIndex(var3.stream(), (param1, param2) -> {
                Dynamic<T> var0x = var1.get((int)param2);
                return param1.orElse(var0x);
            })));
        }

        return var2;
    }

    private static <T> Stream<Optional<Dynamic<T>>> getLines(Dynamic<T> param0, String param1) {
        return Stream.of(
            param0.get(param1 + "1").result(), param0.get(param1 + "2").result(), param0.get(param1 + "3").result(), param0.get(param1 + "4").result()
        );
    }

    private static <T> Dynamic<T> createDefaultText(Dynamic<T> param0) {
        return param0.emptyMap()
            .set("messages", createEmptyLines(param0))
            .set("color", param0.createString("black"))
            .set("has_glowing_text", param0.createBoolean(false));
    }

    private static <T> Dynamic<T> createEmptyLines(Dynamic<T> param0) {
        Dynamic<T> var0 = ComponentDataFixUtils.createEmptyComponent(param0.getOps());
        return param0.createList(Stream.of(var0, var0, var0, var0));
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), BlockEntitySignDoubleSidedEditableTextFix::fixTag);
    }
}
