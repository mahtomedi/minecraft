package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;

public class CommandsReport implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public CommandsReport(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(HashCache param0) throws IOException {
        Path var0 = this.generator.getOutputFolder().resolve("reports/commands.json");
        CommandDispatcher<CommandSourceStack> var1 = new Commands(Commands.CommandSelection.ALL).getDispatcher();
        DataProvider.save(GSON, param0, ArgumentTypes.serializeNodeToJson(var1, var1.getRoot()), var0);
    }

    @Override
    public String getName() {
        return "Command Syntax";
    }
}
