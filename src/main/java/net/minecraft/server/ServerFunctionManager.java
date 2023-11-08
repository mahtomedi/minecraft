package net.minecraft.server;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class ServerFunctionManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
    private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
    private final MinecraftServer server;
    private List<CommandFunction<CommandSourceStack>> ticking = ImmutableList.of();
    private boolean postReload;
    private ServerFunctionLibrary library;

    public ServerFunctionManager(MinecraftServer param0, ServerFunctionLibrary param1) {
        this.server = param0;
        this.library = param1;
        this.postReload(param1);
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return this.server.getCommands().getDispatcher();
    }

    public void tick() {
        if (this.server.tickRateManager().runsNormally()) {
            if (this.postReload) {
                this.postReload = false;
                Collection<CommandFunction<CommandSourceStack>> var0 = this.library.getTag(LOAD_FUNCTION_TAG);
                this.executeTagFunctions(var0, LOAD_FUNCTION_TAG);
            }

            this.executeTagFunctions(this.ticking, TICK_FUNCTION_TAG);
        }
    }

    private void executeTagFunctions(Collection<CommandFunction<CommandSourceStack>> param0, ResourceLocation param1) {
        this.server.getProfiler().push(param1::toString);

        for(CommandFunction<CommandSourceStack> var0 : param0) {
            this.execute(var0, this.getGameLoopSender());
        }

        this.server.getProfiler().pop();
    }

    public void execute(CommandFunction<CommandSourceStack> param0, CommandSourceStack param1) {
        ProfilerFiller var0 = this.server.getProfiler();
        var0.push(() -> "function " + param0.id());

        try {
            InstantiatedFunction<CommandSourceStack> var1 = param0.instantiate(null, this.getDispatcher(), param1);
            Commands.executeCommandInContext(param1, param2 -> ExecutionContext.queueInitialFunctionCall(param2, var1, param1, CommandResultCallback.EMPTY));
        } catch (FunctionInstantiationException var9) {
        } catch (Exception var10) {
            LOGGER.warn("Failed to execute function {}", param0.id(), var10);
        } finally {
            var0.pop();
        }

    }

    public void replaceLibrary(ServerFunctionLibrary param0) {
        this.library = param0;
        this.postReload(param0);
    }

    private void postReload(ServerFunctionLibrary param0) {
        this.ticking = ImmutableList.copyOf(param0.getTag(TICK_FUNCTION_TAG));
        this.postReload = true;
    }

    public CommandSourceStack getGameLoopSender() {
        return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
    }

    public Optional<CommandFunction<CommandSourceStack>> get(ResourceLocation param0) {
        return this.library.getFunction(param0);
    }

    public Collection<CommandFunction<CommandSourceStack>> getTag(ResourceLocation param0) {
        return this.library.getTag(param0);
    }

    public Iterable<ResourceLocation> getFunctionNames() {
        return this.library.getFunctions().keySet();
    }

    public Iterable<ResourceLocation> getTagNames() {
        return this.library.getAvailableTags();
    }
}
