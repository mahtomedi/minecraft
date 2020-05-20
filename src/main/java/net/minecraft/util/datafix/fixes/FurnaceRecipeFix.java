package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
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
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;

public class FurnaceRecipeFix extends DataFix {
    public FurnaceRecipeFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.cap(this.getOutputSchema().getTypeRaw(References.RECIPE));
    }

    private <R> TypeRewriteRule cap(Type<R> param0) {
        Type<Pair<Either<Pair<List<Pair<R, Integer>>, Dynamic<?>>, Unit>, Dynamic<?>>> var0 = DSL.and(
            DSL.optional(DSL.field("RecipesUsed", DSL.and(DSL.compoundList(param0, DSL.intType()), DSL.remainderType()))), DSL.remainderType()
        );
        OpticFinder<?> var1 = DSL.namedChoice("minecraft:furnace", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace"));
        OpticFinder<?> var2 = DSL.namedChoice(
            "minecraft:blast_furnace", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace")
        );
        OpticFinder<?> var3 = DSL.namedChoice("minecraft:smoker", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker"));
        Type<?> var4 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace");
        Type<?> var5 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace");
        Type<?> var6 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker");
        Type<?> var7 = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type<?> var8 = this.getOutputSchema().getType(References.BLOCK_ENTITY);
        return this.fixTypeEverywhereTyped(
            "FurnaceRecipesFix",
            var7,
            var8,
            param8 -> param8.updateTyped(var1, var4, param2x -> this.updateFurnaceContents(param0, var0, param2x))
                    .updateTyped(var2, var5, param2x -> this.updateFurnaceContents(param0, var0, param2x))
                    .updateTyped(var3, var6, param2x -> this.updateFurnaceContents(param0, var0, param2x))
        );
    }

    private <R> Typed<?> updateFurnaceContents(
        Type<R> param0, Type<Pair<Either<Pair<List<Pair<R, Integer>>, Dynamic<?>>, Unit>, Dynamic<?>>> param1, Typed<?> param2
    ) {
        Dynamic<?> var0 = param2.getOrCreate(DSL.remainderFinder());
        int var1 = var0.get("RecipesUsedSize").asInt(0);
        var0 = var0.remove("RecipesUsedSize");
        List<Pair<R, Integer>> var2 = Lists.newArrayList();

        for(int var3 = 0; var3 < var1; ++var3) {
            String var4 = "RecipeLocation" + var3;
            String var5 = "RecipeAmount" + var3;
            Optional<? extends Dynamic<?>> var6 = var0.get(var4).result();
            int var7 = var0.get(var5).asInt(0);
            if (var7 > 0) {
                var6.ifPresent(param3 -> {
                    Optional<? extends Pair<R, ? extends Dynamic<?>>> var0x = param0.read(param3).result();
                    var0x.ifPresent(param2x -> var2.add(Pair.of(param2x.getFirst(), var7)));
                });
            }

            var0 = var0.remove(var4).remove(var5);
        }

        return param2.set(DSL.remainderFinder(), param1, Pair.of(Either.left(Pair.of(var2, var0.emptyMap())), var0));
    }
}
