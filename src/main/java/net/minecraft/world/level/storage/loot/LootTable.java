package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

public class LootTable {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation DEFAULT_RANDOM_SEQUENCE = new ResourceLocation("default");
    public static final LootTable EMPTY = new LootTable(LootContextParamSets.EMPTY, DEFAULT_RANDOM_SEQUENCE, new LootPool[0], new LootItemFunction[0]);
    public static final LootContextParamSet DEFAULT_PARAM_SET = LootContextParamSets.ALL_PARAMS;
    final LootContextParamSet paramSet;
    final ResourceLocation randomSequence;
    final LootPool[] pools;
    final LootItemFunction[] functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

    LootTable(LootContextParamSet param0, ResourceLocation param1, LootPool[] param2, LootItemFunction[] param3) {
        this.paramSet = param0;
        this.randomSequence = param1;
        this.pools = param2;
        this.functions = param3;
        this.compositeFunction = LootItemFunctions.compose(param3);
    }

    public static Consumer<ItemStack> createStackSplitter(ServerLevel param0, Consumer<ItemStack> param1) {
        return param2 -> {
            if (param2.isItemEnabled(param0.enabledFeatures())) {
                if (param2.getCount() < param2.getMaxStackSize()) {
                    param1.accept(param2);
                } else {
                    int var0x = param2.getCount();

                    while(var0x > 0) {
                        ItemStack var1x = param2.copyWithCount(Math.min(param2.getMaxStackSize(), var0x));
                        var0x -= var1x.getCount();
                        param1.accept(var1x);
                    }
                }

            }
        };
    }

    public void getRandomItemsRaw(LootParams param0, Consumer<ItemStack> param1) {
        this.getRandomItemsRaw(new LootContext.Builder(param0).create(this.randomSequence), param1);
    }

    public void getRandomItemsRaw(LootContext param0, Consumer<ItemStack> param1) {
        LootContext.VisitedEntry<?> var0 = LootContext.createVisitedEntry(this);
        if (param0.pushVisitedElement(var0)) {
            Consumer<ItemStack> var1 = LootItemFunction.decorate(this.compositeFunction, param1, param0);

            for(LootPool var2 : this.pools) {
                var2.addRandomItems(var1, param0);
            }

            param0.popVisitedElement(var0);
        } else {
            LOGGER.warn("Detected infinite loop in loot tables");
        }

    }

    public void getRandomItems(LootParams param0, long param1, Consumer<ItemStack> param2) {
        this.getRandomItemsRaw(
            new LootContext.Builder(param0).withOptionalRandomSeed(param1).create(this.randomSequence), createStackSplitter(param0.getLevel(), param2)
        );
    }

    public void getRandomItems(LootParams param0, Consumer<ItemStack> param1) {
        this.getRandomItemsRaw(param0, createStackSplitter(param0.getLevel(), param1));
    }

    public void getRandomItems(LootContext param0, Consumer<ItemStack> param1) {
        this.getRandomItemsRaw(param0, createStackSplitter(param0.getLevel(), param1));
    }

    public ObjectArrayList<ItemStack> getRandomItems(LootParams param0, long param1) {
        return this.getRandomItems(new LootContext.Builder(param0).withOptionalRandomSeed(param1).create(this.randomSequence));
    }

    public ObjectArrayList<ItemStack> getRandomItems(LootParams param0) {
        return this.getRandomItems(new LootContext.Builder(param0).create(this.randomSequence));
    }

    private ObjectArrayList<ItemStack> getRandomItems(LootContext param0) {
        ObjectArrayList<ItemStack> var0 = new ObjectArrayList<>();
        this.getRandomItems(param0, var0::add);
        return var0;
    }

    public LootContextParamSet getParamSet() {
        return this.paramSet;
    }

    public void validate(ValidationContext param0) {
        for(int var0 = 0; var0 < this.pools.length; ++var0) {
            this.pools[var0].validate(param0.forChild(".pools[" + var0 + "]"));
        }

        for(int var1 = 0; var1 < this.functions.length; ++var1) {
            this.functions[var1].validate(param0.forChild(".functions[" + var1 + "]"));
        }

    }

    public void fill(Container param0, LootParams param1, long param2) {
        LootContext var0 = new LootContext.Builder(param1).withOptionalRandomSeed(param2).create(this.randomSequence);
        ObjectArrayList<ItemStack> var1 = this.getRandomItems(var0);
        RandomSource var2 = var0.getRandom();
        List<Integer> var3 = this.getAvailableSlots(param0, var2);
        this.shuffleAndSplitItems(var1, var3.size(), var2);

        for(ItemStack var4 : var1) {
            if (var3.isEmpty()) {
                LOGGER.warn("Tried to over-fill a container");
                return;
            }

            if (var4.isEmpty()) {
                param0.setItem(var3.remove(var3.size() - 1), ItemStack.EMPTY);
            } else {
                param0.setItem(var3.remove(var3.size() - 1), var4);
            }
        }

    }

    private void shuffleAndSplitItems(ObjectArrayList<ItemStack> param0, int param1, RandomSource param2) {
        List<ItemStack> var0 = Lists.newArrayList();
        Iterator<ItemStack> var1 = param0.iterator();

        while(var1.hasNext()) {
            ItemStack var2 = var1.next();
            if (var2.isEmpty()) {
                var1.remove();
            } else if (var2.getCount() > 1) {
                var0.add(var2);
                var1.remove();
            }
        }

        while(param1 - param0.size() - var0.size() > 0 && !var0.isEmpty()) {
            ItemStack var3 = var0.remove(Mth.nextInt(param2, 0, var0.size() - 1));
            int var4 = Mth.nextInt(param2, 1, var3.getCount() / 2);
            ItemStack var5 = var3.split(var4);
            if (var3.getCount() > 1 && param2.nextBoolean()) {
                var0.add(var3);
            } else {
                param0.add(var3);
            }

            if (var5.getCount() > 1 && param2.nextBoolean()) {
                var0.add(var5);
            } else {
                param0.add(var5);
            }
        }

        param0.addAll(var0);
        Util.shuffle(param0, param2);
    }

    private List<Integer> getAvailableSlots(Container param0, RandomSource param1) {
        ObjectArrayList<Integer> var0 = new ObjectArrayList<>();

        for(int var1 = 0; var1 < param0.getContainerSize(); ++var1) {
            if (param0.getItem(var1).isEmpty()) {
                var0.add(var1);
            }
        }

        Util.shuffle(var0, param1);
        return var0;
    }

    public static LootTable.Builder lootTable() {
        return new LootTable.Builder();
    }

    public static class Builder implements FunctionUserBuilder<LootTable.Builder> {
        private final List<LootPool> pools = Lists.newArrayList();
        private final List<LootItemFunction> functions = Lists.newArrayList();
        private LootContextParamSet paramSet = LootTable.DEFAULT_PARAM_SET;
        private ResourceLocation randomSequence = LootTable.DEFAULT_RANDOM_SEQUENCE;

        public LootTable.Builder withPool(LootPool.Builder param0) {
            this.pools.add(param0.build());
            return this;
        }

        public LootTable.Builder setParamSet(LootContextParamSet param0) {
            this.paramSet = param0;
            return this;
        }

        public LootTable.Builder setRandomSequence(ResourceLocation param0) {
            this.randomSequence = param0;
            return this;
        }

        public LootTable.Builder apply(LootItemFunction.Builder param0) {
            this.functions.add(param0.build());
            return this;
        }

        public LootTable.Builder unwrap() {
            return this;
        }

        public LootTable build() {
            return new LootTable(this.paramSet, this.randomSequence, this.pools.toArray(new LootPool[0]), this.functions.toArray(new LootItemFunction[0]));
        }
    }

    public static class Serializer implements JsonDeserializer<LootTable>, JsonSerializer<LootTable> {
        public LootTable deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "loot table");
            LootPool[] var1 = GsonHelper.getAsObject(var0, "pools", new LootPool[0], param2, LootPool[].class);
            LootContextParamSet var2 = null;
            if (var0.has("type")) {
                String var3 = GsonHelper.getAsString(var0, "type");
                var2 = LootContextParamSets.get(new ResourceLocation(var3));
            }

            ResourceLocation var5;
            if (var0.has("random_sequence")) {
                String var4 = GsonHelper.getAsString(var0, "random_sequence");
                var5 = new ResourceLocation(var4);
            } else {
                var5 = LootTable.DEFAULT_RANDOM_SEQUENCE;
            }

            LootItemFunction[] var7 = GsonHelper.getAsObject(var0, "functions", new LootItemFunction[0], param2, LootItemFunction[].class);
            return new LootTable(var2 != null ? var2 : LootContextParamSets.ALL_PARAMS, var5, var1, var7);
        }

        public JsonElement serialize(LootTable param0, Type param1, JsonSerializationContext param2) {
            JsonObject var0 = new JsonObject();
            if (param0.paramSet != LootTable.DEFAULT_PARAM_SET) {
                ResourceLocation var1 = LootContextParamSets.getKey(param0.paramSet);
                if (var1 != null) {
                    var0.addProperty("type", var1.toString());
                } else {
                    LootTable.LOGGER.warn("Failed to find id for param set {}", param0.paramSet);
                }
            }

            var0.addProperty("random_sequence", param0.randomSequence.toString());
            if (param0.pools.length > 0) {
                var0.add("pools", param2.serialize(param0.pools));
            }

            if (!ArrayUtils.isEmpty((Object[])param0.functions)) {
                var0.add("functions", param2.serialize(param0.functions));
            }

            return var0;
        }
    }
}
