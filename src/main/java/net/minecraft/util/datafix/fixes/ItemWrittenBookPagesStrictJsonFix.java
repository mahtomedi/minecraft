package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;

public class ItemWrittenBookPagesStrictJsonFix extends DataFix {
    public ItemWrittenBookPagesStrictJsonFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    public Dynamic<?> fixTag(Dynamic<?> param0) {
        return param0.update(
            "pages",
            param1 -> DataFixUtils.orElse(
                    param1.asStreamOpt()
                        .map(
                            param0x -> param0x.map(
                                    (Function<? super Dynamic<?>, ? extends Dynamic<?>>)(param0xx -> {
                                        if (!param0xx.asString().result().isPresent()) {
                                            return param0xx;
                                        } else {
                                            String var0x = param0xx.asString("");
                                            Component var1x = null;
                                            if (!"null".equals(var0x) && !StringUtils.isEmpty(var0x)) {
                                                if (var0x.charAt(0) == '"' && var0x.charAt(var0x.length() - 1) == '"'
                                                    || var0x.charAt(0) == '{' && var0x.charAt(var0x.length() - 1) == '}') {
                                                    try {
                                                        var1x = GsonHelper.fromJson(BlockEntitySignTextStrictJsonFix.GSON, var0x, Component.class, true);
                                                        if (var1x == null) {
                                                            var1x = TextComponent.EMPTY;
                                                        }
                                                    } catch (Exception var6) {
                                                    }
                    
                                                    if (var1x == null) {
                                                        try {
                                                            var1x = Component.Serializer.fromJson(var0x);
                                                        } catch (Exception var5) {
                                                        }
                                                    }
                    
                                                    if (var1x == null) {
                                                        try {
                                                            var1x = Component.Serializer.fromJsonLenient(var0x);
                                                        } catch (Exception var4) {
                                                        }
                                                    }
                    
                                                    if (var1x == null) {
                                                        var1x = new TextComponent(var0x);
                                                    }
                                                } else {
                                                    var1x = new TextComponent(var0x);
                                                }
                                            } else {
                                                var1x = TextComponent.EMPTY;
                                            }
                    
                                            return param0xx.createString(Component.Serializer.toJson(var1x));
                                        }
                                    })
                                )
                        )
                        .map(param0::createList)
                        .result(),
                    param0.emptyList()
                )
        );
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<?> var1 = var0.findField("tag");
        return this.fixTypeEverywhereTyped(
            "ItemWrittenBookPagesStrictJsonFix", var0, param1 -> param1.updateTyped(var1, param0x -> param0x.update(DSL.remainderFinder(), this::fixTag))
        );
    }
}
