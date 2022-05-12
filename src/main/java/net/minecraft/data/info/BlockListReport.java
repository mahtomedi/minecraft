package net.minecraft.data.info;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockListReport implements DataProvider {
    private final DataGenerator generator;

    public BlockListReport(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(CachedOutput param0) throws IOException {
        JsonObject var0 = new JsonObject();

        for(Block var1 : Registry.BLOCK) {
            ResourceLocation var2 = Registry.BLOCK.getKey(var1);
            JsonObject var3 = new JsonObject();
            StateDefinition<Block, BlockState> var4 = var1.getStateDefinition();
            if (!var4.getProperties().isEmpty()) {
                JsonObject var5 = new JsonObject();

                for(Property<?> var6 : var4.getProperties()) {
                    JsonArray var7 = new JsonArray();

                    for(Comparable<?> var8 : var6.getPossibleValues()) {
                        var7.add(Util.getPropertyName(var6, var8));
                    }

                    var5.add(var6.getName(), var7);
                }

                var3.add("properties", var5);
            }

            JsonArray var9 = new JsonArray();

            for(BlockState var10 : var4.getPossibleStates()) {
                JsonObject var11 = new JsonObject();
                JsonObject var12 = new JsonObject();

                for(Property<?> var13 : var4.getProperties()) {
                    var12.addProperty(var13.getName(), Util.getPropertyName(var13, var10.getValue(var13)));
                }

                if (var12.size() > 0) {
                    var11.add("properties", var12);
                }

                var11.addProperty("id", Block.getId(var10));
                if (var10 == var1.defaultBlockState()) {
                    var11.addProperty("default", true);
                }

                var9.add(var11);
            }

            var3.add("states", var9);
            var0.add(var2.toString(), var3);
        }

        Path var14 = this.generator.getOutputFolder(DataGenerator.Target.REPORTS).resolve("blocks.json");
        DataProvider.saveStable(param0, var0, var14);
    }

    @Override
    public String getName() {
        return "Block List";
    }
}
