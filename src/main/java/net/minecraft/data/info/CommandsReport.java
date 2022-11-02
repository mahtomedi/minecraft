package net.minecraft.data.info;

import com.mojang.brigadier.CommandDispatcher;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class CommandsReport implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public CommandsReport(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1) {
        this.output = param0;
        this.registries = param1;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput param0) {
        Path var0 = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("commands.json");
        return this.registries.thenCompose(param2 -> {
            CommandDispatcher<CommandSourceStack> var0x = new Commands(Commands.CommandSelection.ALL, Commands.createValidationContext(param2)).getDispatcher();
            return DataProvider.saveStable(param0, ArgumentUtils.serializeNodeToJson(var0x, var0x.getRoot()), var0);
        });
    }

    @Override
    public final String getName() {
        return "Command Syntax";
    }
}
