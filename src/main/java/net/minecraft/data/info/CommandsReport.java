package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

public class CommandsReport implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public CommandsReport(DataGenerator param0) {
        this.generator = param0;
    }

    @Override
    public void run(CachedOutput param0) throws IOException {
        Path var0 = this.generator.getOutputFolder().resolve("reports/commands.json");
        CommandDispatcher<CommandSourceStack> var1 = new Commands(Commands.CommandSelection.ALL, new CommandBuildContext(RegistryAccess.BUILTIN.get()))
            .getDispatcher();
        DataProvider.save(GSON, param0, ArgumentUtils.serializeNodeToJson(var1, var1.getRoot()), var0);
    }

    @Override
    public String getName() {
        return "Command Syntax";
    }
}
