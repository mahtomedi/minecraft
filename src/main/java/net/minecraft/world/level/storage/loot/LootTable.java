package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootTable {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final LootTable EMPTY = new LootTable(LootContextParamSets.EMPTY, new LootPool[0], new LootItemFunction[0]);
    public static final LootContextParamSet DEFAULT_PARAM_SET = LootContextParamSets.ALL_PARAMS;
    private final LootContextParamSet paramSet;
    private final LootPool[] pools;
    private final LootItemFunction[] functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

    private LootTable(LootContextParamSet param0, LootPool[] param1, LootItemFunction[] param2) {
        this.paramSet = param0;
        this.pools = param1;
        this.functions = param2;
        this.compositeFunction = LootItemFunctions.compose(param2);
    }

    public static Consumer<ItemStack> createStackSplitter(Consumer<ItemStack> param0) {
        return param1 -> {
            if (param1.getCount() < param1.getMaxStackSize()) {
                param0.accept(param1);
            } else {
                int var0x = param1.getCount();

                while(var0x > 0) {
                    ItemStack var1 = param1.copy();
                    var1.setCount(Math.min(param1.getMaxStackSize(), var0x));
                    var0x -= var1.getCount();
                    param0.accept(var1);
                }
            }

        };
    }

    public void getRandomItemsRaw(LootContext param0, Consumer<ItemStack> param1) {
        if (param0.addVisitedTable(this)) {
            Consumer<ItemStack> var0 = LootItemFunction.decorate(this.compositeFunction, param1, param0);

            for(LootPool var1 : this.pools) {
                var1.addRandomItems(var0, param0);
            }

            param0.removeVisitedTable(this);
        } else {
            LOGGER.warn("Detected infinite loop in loot tables");
        }

    }

    public void getRandomItems(LootContext param0, Consumer<ItemStack> param1) {
        this.getRandomItemsRaw(param0, createStackSplitter(param1));
    }

    public List<ItemStack> getRandomItems(LootContext param0) {
        List<ItemStack> var0 = Lists.newArrayList();
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

    public void fill(Container param0, LootContext param1) {
        List<ItemStack> var0 = this.getRandomItems(param1);
        Random var1 = param1.getRandom();
        List<Integer> var2 = this.getAvailableSlots(param0, var1);
        this.shuffleAndSplitItems(var0, var2.size(), var1);

        for(ItemStack var3 : var0) {
            if (var2.isEmpty()) {
                LOGGER.warn("Tried to over-fill a container");
                return;
            }

            if (var3.isEmpty()) {
                param0.setItem(var2.remove(var2.size() - 1), ItemStack.EMPTY);
            } else {
                param0.setItem(var2.remove(var2.size() - 1), var3);
            }
        }

    }

    private void shuffleAndSplitItems(List<ItemStack> param0, int param1, Random param2) {
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
        Collections.shuffle(param0, param2);
    }

    private List<Integer> getAvailableSlots(Container param0, Random param1) {
        List<Integer> var0 = Lists.newArrayList();

        for(int var1 = 0; var1 < param0.getContainerSize(); ++var1) {
            if (param0.getItem(var1).isEmpty()) {
                var0.add(var1);
            }
        }

        Collections.shuffle(var0, param1);
        return var0;
    }

    public static LootTable.Builder lootTable() {
        return new LootTable.Builder();
    }

    public static class Builder implements FunctionUserBuilder<LootTable.Builder> {
        private final List<LootPool> pools = Lists.newArrayList();
        private final List<LootItemFunction> functions = Lists.newArrayList();
        private LootContextParamSet paramSet = LootTable.DEFAULT_PARAM_SET;

        public LootTable.Builder withPool(LootPool.Builder param0) {
            this.pools.add(param0.build());
            return this;
        }

        public LootTable.Builder setParamSet(LootContextParamSet param0) {
            this.paramSet = param0;
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
            return new LootTable(this.paramSet, this.pools.toArray(new LootPool[0]), this.functions.toArray(new LootItemFunction[0]));
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

            LootItemFunction[] var4 = GsonHelper.getAsObject(var0, "functions", new LootItemFunction[0], param2, LootItemFunction[].class);
            return new LootTable(var2 != null ? var2 : LootContextParamSets.ALL_PARAMS, var1, var4);
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
