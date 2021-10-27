package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;

public class ChunkRenamesFix extends DataFix {
    public ChunkRenamesFix(Schema param0) {
        super(param0, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> var1 = var0.findField("Level");
        OpticFinder<?> var2 = var1.type().findField("Structures");
        Type<?> var3 = this.getOutputSchema().getType(References.CHUNK);
        Type<?> var4 = var3.findFieldType("structures");
        return this.fixTypeEverywhereTyped("Chunk Renames; purge Level-tag", var0, var3, param3 -> {
            Typed<?> var0x = param3.getTyped(var1);
            Typed<?> var1x = appendChunkName(var0x);
            var1x = var1x.set(DSL.remainderFinder(), mergeRemainders(param3, var0x.get(DSL.remainderFinder())));
            var1x = renameField(var1x, "TileEntities", "block_entities");
            var1x = renameField(var1x, "TileTicks", "block_ticks");
            var1x = renameField(var1x, "Entities", "entities");
            var1x = renameField(var1x, "Sections", "sections");
            var1x = var1x.updateTyped(var2, var4, param0x -> renameField(param0x, "Starts", "starts"));
            return renameField(var1x, "Structures", "structures");
        });
    }

    private static Typed<?> renameField(Typed<?> param0, String param1, String param2) {
        return renameFieldHelper(param0, param1, param2, param0.getType().findFieldType(param1));
    }

    private static <A> Typed<?> renameFieldHelper(Typed<?> param0, String param1, String param2, Type<A> param3) {
        Type<Either<A, Unit>> var0 = DSL.optional(DSL.field(param1, param3));
        Type<Either<A, Unit>> var1 = DSL.optional(DSL.field(param2, param3));
        return param0.update(var0.finder(), var1, Function.identity());
    }

    private static <A> Typed<Pair<String, A>> appendChunkName(Typed<A> param0) {
        return new Typed<>(DSL.named("chunk", param0.getType()), param0.getOps(), Pair.of("chunk", param0.getValue()));
    }

    private static <T> Dynamic<T> mergeRemainders(Typed<?> param0, Dynamic<T> param1) {
        DynamicOps<T> var0 = param1.getOps();
        Dynamic<T> var1 = param0.get(DSL.remainderFinder()).convert(var0);
        DataResult<T> var2 = var0.getMap(param1.getValue()).flatMap(param2 -> var0.mergeToMap(var1.getValue(), param2));
        return var2.result().map(param1x -> new Dynamic<>(var0, param1x)).orElse(param1);
    }
}
