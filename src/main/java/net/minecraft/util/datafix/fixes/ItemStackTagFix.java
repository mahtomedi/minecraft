package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class ItemStackTagFix extends DataFix {
    private final String name;
    private final Predicate<String> idFilter;

    public ItemStackTagFix(Schema param0, String param1, Predicate<String> param2) {
        super(param0, false);
        this.name = param1;
        this.idFilter = param2;
    }

    @Override
    public final TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> var1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> var2 = var0.findField("tag");
        return this.fixTypeEverywhereTyped(
            this.name,
            var0,
            param2 -> {
                Optional<Pair<String, String>> var0x = param2.getOptional(var1);
                return var0x.isPresent() && this.idFilter.test(var0x.get().getSecond())
                    ? param2.updateTyped(var2, param0x -> param0x.update(DSL.remainderFinder(), this::fixItemStackTag))
                    : param2;
            }
        );
    }

    protected abstract <T> Dynamic<T> fixItemStackTag(Dynamic<T> var1);
}
