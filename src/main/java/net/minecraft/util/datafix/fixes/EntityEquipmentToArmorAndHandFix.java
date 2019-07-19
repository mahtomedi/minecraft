package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class EntityEquipmentToArmorAndHandFix extends DataFix {
    public EntityEquipmentToArmorAndHandFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.cap(this.getInputSchema().getTypeRaw(References.ITEM_STACK));
    }

    private <IS> TypeRewriteRule cap(Type<IS> param0) {
        Type<Pair<Either<List<IS>, Unit>, Dynamic<?>>> var0 = DSL.and(DSL.optional(DSL.field("Equipment", DSL.list(param0))), DSL.remainderType());
        Type<Pair<Either<List<IS>, Unit>, Pair<Either<List<IS>, Unit>, Dynamic<?>>>> var1 = DSL.and(
            DSL.optional(DSL.field("ArmorItems", DSL.list(param0))), DSL.optional(DSL.field("HandItems", DSL.list(param0))), DSL.remainderType()
        );
        OpticFinder<Pair<Either<List<IS>, Unit>, Dynamic<?>>> var2 = DSL.typeFinder(var0);
        OpticFinder<List<IS>> var3 = DSL.fieldFinder("Equipment", DSL.list(param0));
        return this.fixTypeEverywhereTyped(
            "EntityEquipmentToArmorAndHandFix",
            this.getInputSchema().getType(References.ENTITY),
            this.getOutputSchema().getType(References.ENTITY),
            param4 -> {
                Either<List<IS>, Unit> var0x = Either.right(DSL.unit());
                Either<List<IS>, Unit> var1x = Either.right(DSL.unit());
                Dynamic<?> var2x = param4.getOrCreate(DSL.remainderFinder());
                Optional<List<IS>> var3x = param4.getOptional(var3);
                if (var3x.isPresent()) {
                    List<IS> var4 = var3x.get();
                    IS var5 = param0.read(var2x.emptyMap())
                        .getSecond()
                        .orElseThrow(() -> new IllegalStateException("Could not parse newly created empty itemstack."));
                    if (!var4.isEmpty()) {
                        var0x = Either.left(Lists.newArrayList(var4.get(0), var5));
                    }
    
                    if (var4.size() > 1) {
                        List<IS> var6 = Lists.newArrayList(var5, var5, var5, var5);
    
                        for(int var7 = 1; var7 < Math.min(var4.size(), 5); ++var7) {
                            var6.set(var7 - 1, var4.get(var7));
                        }
    
                        var1x = Either.left(var6);
                    }
                }
    
                Dynamic<?> var8 = var2x;
                Optional<? extends Stream<? extends Dynamic<?>>> var9 = var2x.get("DropChances").asStreamOpt();
                if (var9.isPresent()) {
                    Iterator<? extends Dynamic<?>> var10 = Stream.concat(var9.get(), Stream.generate(() -> var8.createInt(0))).iterator();
                    float var11 = var10.next().asFloat(0.0F);
                    if (!var2x.get("HandDropChances").get().isPresent()) {
                        Dynamic<?> var12 = var2x.emptyMap().merge(var2x.createFloat(var11)).merge(var2x.createFloat(0.0F));
                        var2x = var2x.set("HandDropChances", var12);
                    }
    
                    if (!var2x.get("ArmorDropChances").get().isPresent()) {
                        Dynamic<?> var13 = var2x.emptyMap()
                            .merge(var2x.createFloat(var10.next().asFloat(0.0F)))
                            .merge(var2x.createFloat(var10.next().asFloat(0.0F)))
                            .merge(var2x.createFloat(var10.next().asFloat(0.0F)))
                            .merge(var2x.createFloat(var10.next().asFloat(0.0F)));
                        var2x = var2x.set("ArmorDropChances", var13);
                    }
    
                    var2x = var2x.remove("DropChances");
                }
    
                return param4.set(var2, var1, Pair.of(var0x, Pair.of(var1x, var2x)));
            }
        );
    }
}
