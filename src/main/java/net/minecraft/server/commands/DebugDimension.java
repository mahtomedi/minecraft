package net.minecraft.server.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.dimension.Dimension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugDimension {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(Commands.literal("debugdim").executes(param0x -> debugDim(param0x.getSource())));
    }

    private static int debugDim(CommandSourceStack param0) {
        Dimension var0 = param0.getLevel().getDimension();
        File var1 = param0.getLevel().getLevelStorage().getFolder();
        File var2 = new File(var1, "debug");
        var2.mkdirs();
        Dynamic<JsonElement> var3 = var0.serialize(JsonOps.INSTANCE);
        int var4 = Registry.DIMENSION_TYPE.getId(var0.getType());
        File var5 = new File(var2, "dim-" + var4 + ".json");

        try (Writer var6 = Files.newBufferedWriter(var5.toPath())) {
            GSON.toJson(var3.getValue(), var6);
        } catch (IOException var20) {
            LOGGER.warn("Failed to save file {}", var5.getAbsolutePath(), var20);
        }

        var0.getKnownBiomes().forEach(param1 -> {
            int var0x = Registry.BIOME.getId(param1);
            Dynamic<JsonElement> var1x = param1.serialize(JsonOps.INSTANCE);
            File var2x = new File(var2, "biome-" + var0x + ".json");

            try (Writer var3x = Files.newBufferedWriter(var2x.toPath())) {
                GSON.toJson(var1x.getValue(), var3x);
            } catch (IOException var18) {
                LOGGER.warn("Failed to save file {}", var2x.getAbsolutePath(), var18);
            }

        });
        param0.sendSuccess(new TextComponent("Saved to file: " + var2), false);
        return 0;
    }
}
