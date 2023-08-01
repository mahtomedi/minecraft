package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class DropInvalidSignDataFix extends NamedEntityFix {
    private static final String EMPTY_COMPONENT = Component.Serializer.toJson(CommonComponents.EMPTY);
    private static final String[] FIELDS_TO_DROP = new String[]{
        "Text1", "Text2", "Text3", "Text4", "FilteredText1", "FilteredText2", "FilteredText3", "FilteredText4", "Color", "GlowingText"
    };

    public DropInvalidSignDataFix(Schema param0, String param1, String param2) {
        super(param0, false, param1, References.BLOCK_ENTITY, param2);
    }

    private static <T> Dynamic<T> fix(Dynamic<T> param0) {
        param0 = param0.update("front_text", DropInvalidSignDataFix::fixText);
        param0 = param0.update("back_text", DropInvalidSignDataFix::fixText);

        for(String var0 : FIELDS_TO_DROP) {
            param0 = param0.remove(var0);
        }

        return param0;
    }

    private static <T> Dynamic<T> fixText(Dynamic<T> param0x) {
        boolean var0x = param0x.get("_filtered_correct").asBoolean(false);
        if (var0x) {
            return param0x.remove("_filtered_correct");
        } else {
            Optional<Stream<Dynamic<T>>> var1 = param0x.get("filtered_messages").asStreamOpt().result();
            if (var1.isEmpty()) {
                return param0x;
            } else {
                Dynamic<T> var2 = param0x.createString(EMPTY_COMPONENT);
                List<Dynamic<T>> var3 = param0x.get("messages").asStreamOpt().result().orElse(Stream.of()).toList();
                List<Dynamic<T>> var4 = Streams.mapWithIndex(var1.get(), (param2, param3) -> {
                    Dynamic<T> var0xx = param3 < (long)var3.size() ? var3.get((int)param3) : var2;
                    return param2.equals(var2) ? var0xx : param2;
                }).toList();
                return var4.stream().allMatch(param1 -> param1.equals(var2))
                    ? param0x.remove("filtered_messages")
                    : param0x.set("filtered_messages", param0x.createList(var4.stream()));
            }
        }
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        return param0.update(DSL.remainderFinder(), DropInvalidSignDataFix::fix);
    }
}
