package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TrappedChestBlockEntityFix extends DataFix {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int SIZE = 4096;
    private static final short SIZE_BITS = 12;

    public TrappedChestBlockEntityFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> var0 = this.getOutputSchema().getType(References.CHUNK);
        Type<?> var1 = var0.findFieldType("Level");
        Type<?> var2 = var1.findFieldType("TileEntities");
        if (!(var2 instanceof ListType)) {
            throw new IllegalStateException("Tile entity type is not a list type.");
        } else {
            ListType<?> var3 = (ListType)var2;
            OpticFinder<? extends List<?>> var4 = DSL.fieldFinder("TileEntities", var3);
            Type<?> var5 = this.getInputSchema().getType(References.CHUNK);
            OpticFinder<?> var6 = var5.findField("Level");
            OpticFinder<?> var7 = var6.type().findField("Sections");
            Type<?> var8 = var7.type();
            if (!(var8 instanceof ListType)) {
                throw new IllegalStateException("Expecting sections to be a list.");
            } else {
                Type<?> var9 = ((ListType)var8).getElement();
                OpticFinder<?> var10 = DSL.typeFinder(var9);
                return TypeRewriteRule.seq(
                    new AddNewChoices(this.getOutputSchema(), "AddTrappedChestFix", References.BLOCK_ENTITY).makeRule(),
                    this.fixTypeEverywhereTyped(
                        "Trapped Chest fix",
                        var5,
                        param4 -> param4.updateTyped(
                                var6,
                                param3x -> {
                                    Optional<? extends Typed<?>> var0x = param3x.getOptionalTyped(var7);
                                    if (!var0x.isPresent()) {
                                        return param3x;
                                    } else {
                                        List<? extends Typed<?>> var1x = var0x.get().getAllTyped(var10);
                                        IntSet var2x = new IntOpenHashSet();
            
                                        for(Typed<?> var3x : var1x) {
                                            TrappedChestBlockEntityFix.TrappedChestSection var4x = new TrappedChestBlockEntityFix.TrappedChestSection(
                                                var3x, this.getInputSchema()
                                            );
                                            if (!var4x.isSkippable()) {
                                                for(int var5x = 0; var5x < 4096; ++var5x) {
                                                    int var6x = var4x.getBlock(var5x);
                                                    if (var4x.isTrappedChest(var6x)) {
                                                        var2x.add(var4x.getIndex() << 12 | var5x);
                                                    }
                                                }
                                            }
                                        }
            
                                        Dynamic<?> var7x = param3x.get(DSL.remainderFinder());
                                        int var8x = var7x.get("xPos").asInt(0);
                                        int var9x = var7x.get("zPos").asInt(0);
                                        TaggedChoiceType<String> var10x = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
                                        return param3x.updateTyped(
                                            var4,
                                            param4x -> param4x.updateTyped(
                                                    var10x.finder(),
                                                    param4xx -> {
                                                        Dynamic<?> var0xx = param4xx.getOrCreate(DSL.remainderFinder());
                                                        int var1xx = var0xx.get("x").asInt(0) - (var8x << 4);
                                                        int var2xx = var0xx.get("y").asInt(0);
                                                        int var3x = var0xx.get("z").asInt(0) - (var9x << 4);
                                                        return var2x.contains(LeavesFix.getIndex(var1xx, var2xx, var3x))
                                                            ? param4xx.update(var10x.finder(), param0x -> param0x.mapFirst(param0xx -> {
                                                                    if (!Objects.equals(param0xx, "minecraft:chest")) {
                                                                        LOGGER.warn("Block Entity was expected to be a chest");
                                                                    }
                        
                                                                    return "minecraft:trapped_chest";
                                                                }))
                                                            : param4xx;
                                                    }
                                                )
                                        );
                                    }
                                }
                            )
                    )
                );
            }
        }
    }

    public static final class TrappedChestSection extends LeavesFix.Section {
        @Nullable
        private IntSet chestIds;

        public TrappedChestSection(Typed<?> param0, Schema param1) {
            super(param0, param1);
        }

        @Override
        protected boolean skippable() {
            this.chestIds = new IntOpenHashSet();

            for(int var0 = 0; var0 < this.palette.size(); ++var0) {
                Dynamic<?> var1 = this.palette.get(var0);
                String var2 = var1.get("Name").asString("");
                if (Objects.equals(var2, "minecraft:trapped_chest")) {
                    this.chestIds.add(var0);
                }
            }

            return this.chestIds.isEmpty();
        }

        public boolean isTrappedChest(int param0) {
            return this.chestIds.contains(param0);
        }
    }
}
