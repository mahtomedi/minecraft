package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EntityRidingToPassengersFix extends DataFix {
    public EntityRidingToPassengersFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Schema var0 = this.getInputSchema();
        Schema var1 = this.getOutputSchema();
        Type<?> var2 = var0.getTypeRaw(References.ENTITY_TREE);
        Type<?> var3 = var1.getTypeRaw(References.ENTITY_TREE);
        Type<?> var4 = var0.getTypeRaw(References.ENTITY);
        return this.cap(var0, var1, var2, var3, var4);
    }

    private <OldEntityTree, NewEntityTree, Entity> TypeRewriteRule cap(
        Schema param0, Schema param1, Type<OldEntityTree> param2, Type<NewEntityTree> param3, Type<Entity> param4
    ) {
        Type<Pair<String, Pair<Either<OldEntityTree, Unit>, Entity>>> var0 = DSL.named(
            References.ENTITY_TREE.typeName(), DSL.and(DSL.optional(DSL.field("Riding", param2)), param4)
        );
        Type<Pair<String, Pair<Either<List<NewEntityTree>, Unit>, Entity>>> var1 = DSL.named(
            References.ENTITY_TREE.typeName(), DSL.and(DSL.optional(DSL.field("Passengers", DSL.list(param3))), param4)
        );
        Type<?> var2 = param0.getType(References.ENTITY_TREE);
        Type<?> var3 = param1.getType(References.ENTITY_TREE);
        if (!Objects.equals(var2, var0)) {
            throw new IllegalStateException("Old entity type is not what was expected.");
        } else if (!var3.equals(var1, true, true)) {
            throw new IllegalStateException("New entity type is not what was expected.");
        } else {
            OpticFinder<Pair<String, Pair<Either<OldEntityTree, Unit>, Entity>>> var4 = DSL.typeFinder(var0);
            OpticFinder<Pair<String, Pair<Either<List<NewEntityTree>, Unit>, Entity>>> var5 = DSL.typeFinder(var1);
            OpticFinder<NewEntityTree> var6 = DSL.typeFinder(param3);
            Type<?> var7 = param0.getType(References.PLAYER);
            Type<?> var8 = param1.getType(References.PLAYER);
            return TypeRewriteRule.seq(
                this.fixTypeEverywhere(
                    "EntityRidingToPassengerFix",
                    var0,
                    var1,
                    param5 -> param6 -> {
                            Optional<Pair<String, Pair<Either<List<NewEntityTree>, Unit>, Entity>>> var0x = Optional.empty();
                            Pair<String, Pair<Either<OldEntityTree, Unit>, Entity>> var1x = param6;
        
                            while(true) {
                                Either<List<NewEntityTree>, Unit> var2x = DataFixUtils.orElse(
                                    var0x.map(
                                        param4x -> {
                                            Typed<NewEntityTree> var0xx = param3.pointTyped(param5)
                                                .orElseThrow(() -> new IllegalStateException("Could not create new entity tree"));
                                            NewEntityTree var1xx = var0xx.set(var5, param4x)
                                                .getOptional(var6)
                                                .orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
                                            return Either.left(ImmutableList.of((NewEntityTree)var1xx));
                                        }
                                    ),
                                    Either.right(DSL.unit())
                                );
                                var0x = Optional.of(Pair.of(References.ENTITY_TREE.typeName(), Pair.of(var2x, var1x.getSecond().getSecond())));
                                Optional<OldEntityTree> var3x = var1x.getSecond().getFirst().left();
                                if (!var3x.isPresent()) {
                                    return var0x.orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
                                }
        
                                var1x = new Typed<>(param2, param5, var3x.get())
                                    .getOptional(var4)
                                    .orElseThrow(() -> new IllegalStateException("Should always have an entity here"));
                            }
                        }
                ),
                this.writeAndRead("player RootVehicle injecter", var7, var8)
            );
        }
    }
}
