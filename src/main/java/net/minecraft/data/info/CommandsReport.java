package net.minecraft.data.info;

import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.flag.FeatureFlags;

public class CommandsReport implements DataProvider {
    private final PackOutput output;

    public CommandsReport(PackOutput param0) {
        this.output = param0;
    }

    @Override
    public void run(CachedOutput param0) throws IOException {
        Path var0 = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("commands.json");
        CommandDispatcher<CommandSourceStack> var1 = new Commands(
                Commands.CommandSelection.ALL, new CommandBuildContext(BuiltinRegistries.createAccess(), FeatureFlags.REGISTRY.allFlags())
            )
            .getDispatcher();
        DataProvider.saveStable(param0, ArgumentUtils.serializeNodeToJson(var1, var1.getRoot()), var0);
    }

    @Override
    public String getName() {
        return "Command Syntax";
    }
}
