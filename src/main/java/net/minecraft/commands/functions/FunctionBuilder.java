package net.minecraft.commands.functions;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.resources.ResourceLocation;

class FunctionBuilder<T extends ExecutionCommandSource<T>> {
    @Nullable
    private List<UnboundEntryAction<T>> plainEntries = new ArrayList<>();
    @Nullable
    private List<MacroFunction.Entry<T>> macroEntries;
    private final List<String> macroArguments = new ArrayList<>();

    public void addCommand(UnboundEntryAction<T> param0) {
        if (this.macroEntries != null) {
            this.macroEntries.add(new MacroFunction.PlainTextEntry<>(param0));
        } else {
            this.plainEntries.add(param0);
        }

    }

    private int getArgumentIndex(String param0) {
        int var0 = this.macroArguments.indexOf(param0);
        if (var0 == -1) {
            var0 = this.macroArguments.size();
            this.macroArguments.add(param0);
        }

        return var0;
    }

    private IntList convertToIndices(List<String> param0) {
        IntArrayList var0 = new IntArrayList(param0.size());

        for(String var1 : param0) {
            var0.add(this.getArgumentIndex(var1));
        }

        return var0;
    }

    public void addMacro(String param0, int param1) {
        StringTemplate var0 = StringTemplate.fromString(param0, param1);
        if (this.plainEntries != null) {
            this.macroEntries = new ArrayList<>(this.plainEntries.size() + 1);

            for(UnboundEntryAction<T> var1 : this.plainEntries) {
                this.macroEntries.add(new MacroFunction.PlainTextEntry<>(var1));
            }

            this.plainEntries = null;
        }

        this.macroEntries.add(new MacroFunction.MacroEntry<>(var0, this.convertToIndices(var0.variables())));
    }

    public CommandFunction<T> build(ResourceLocation param0) {
        return (CommandFunction<T>)(this.macroEntries != null
            ? new MacroFunction<>(param0, this.macroEntries, this.macroArguments)
            : new PlainTextFunction<>(param0, this.plainEntries));
    }
}
