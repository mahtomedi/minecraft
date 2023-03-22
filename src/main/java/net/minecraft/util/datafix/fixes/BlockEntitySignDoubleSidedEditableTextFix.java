package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class BlockEntitySignDoubleSidedEditableTextFix extends NamedEntityFix {
    public BlockEntitySignDoubleSidedEditableTextFix(Schema param0, String param1, String param2) {
        super(param0, false, param1, References.BLOCK_ENTITY, param2);
    }

    private static Dynamic<?> fixTag(Dynamic<?> param0) {
        String var0 = "black";
        Dynamic<?> var1 = param0.emptyMap();
        var1 = var1.set("messages", getTextList(param0, "Text"));
        var1 = var1.set("filtered_messages", getTextList(param0, "FilteredText"));
        Optional<? extends Dynamic<?>> var2 = param0.get("Color").result();
        var1 = var1.set("color", var2.isPresent() ? var2.get() : var1.createString("black"));
        Optional<? extends Dynamic<?>> var3 = param0.get("GlowingText").result();
        var1 = var1.set("has_glowing_text", var3.isPresent() ? var3.get() : var1.createBoolean(false));
        Dynamic<?> var4 = param0.emptyMap();
        Dynamic<?> var5 = getEmptyTextList(param0);
        var4 = var4.set("messages", var5);
        var4 = var4.set("filtered_messages", var5);
        var4 = var4.set("color", var4.createString("black"));
        var4 = var4.set("has_glowing_text", var4.createBoolean(false));
        param0 = param0.set("front_text", var1);
        return param0.set("back_text", var4);
    }

    private static <T> Dynamic<T> getTextList(Dynamic<T> param0, String param1) {
        Dynamic<T> var0 = param0.createString(getEmptyComponent());
        return param0.createList(
            Stream.of(
                param0.get(param1 + "1").result().orElse(var0),
                param0.get(param1 + "2").result().orElse(var0),
                param0.get(param1 + "3").result().orElse(var0),
                param0.get(param1 + "4").result().orElse(var0)
            )
        );
    }

    private static <T> Dynamic<T> getEmptyTextList(Dynamic<T> param0) {
        Dynamic<T> var0 = param0.createString(getEmptyComponent());
        return param0.createList(Stream.of(var0, var0, var0, var0));
    }

    private static String getEmptyComponent() {
        return Component.Serializer.toJson(CommonComponents.EMPTY);
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), BlockEntitySignDoubleSidedEditableTextFix::fixTag);
    }
}
