package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MacroFunction<T extends ExecutionCommandSource<T>> implements CommandFunction<T> {
    private static final DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("#"), param0 -> {
        param0.setMaximumFractionDigits(15);
        param0.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
    });
    private static final int MAX_CACHE_ENTRIES = 8;
    private final List<String> parameters;
    private final Object2ObjectLinkedOpenHashMap<List<String>, InstantiatedFunction<T>> cache = new Object2ObjectLinkedOpenHashMap<>(8, 0.25F);
    private final ResourceLocation id;
    private final List<MacroFunction.Entry<T>> entries;

    public MacroFunction(ResourceLocation param0, List<MacroFunction.Entry<T>> param1, List<String> param2) {
        this.id = param0;
        this.entries = param1;
        this.parameters = param2;
    }

    @Override
    public ResourceLocation id() {
        return this.id;
    }

    public InstantiatedFunction<T> instantiate(@Nullable CompoundTag param0, CommandDispatcher<T> param1, T param2) throws FunctionInstantiationException {
        if (param0 == null) {
            throw new FunctionInstantiationException(Component.translatable("commands.function.error.missing_arguments", Component.translationArg(this.id())));
        } else {
            List<String> var0 = new ArrayList<>(this.parameters.size());

            for(String var1 : this.parameters) {
                Tag var2 = param0.get(var1);
                if (var2 == null) {
                    throw new FunctionInstantiationException(
                        Component.translatable("commands.function.error.missing_argument", Component.translationArg(this.id()), var1)
                    );
                }

                var0.add(stringify(var2));
            }

            InstantiatedFunction<T> var3 = this.cache.getAndMoveToLast(var0);
            if (var3 != null) {
                return var3;
            } else {
                if (this.cache.size() >= 8) {
                    this.cache.removeFirst();
                }

                InstantiatedFunction<T> var4 = this.substituteAndParse(this.parameters, var0, param1, param2);
                this.cache.put(var0, var4);
                return var4;
            }
        }
    }

    private static String stringify(Tag param0) {
        if (param0 instanceof FloatTag var0) {
            return DECIMAL_FORMAT.format((double)var0.getAsFloat());
        } else if (param0 instanceof DoubleTag var1) {
            return DECIMAL_FORMAT.format(var1.getAsDouble());
        } else if (param0 instanceof ByteTag var2) {
            return String.valueOf(var2.getAsByte());
        } else if (param0 instanceof ShortTag var3) {
            return String.valueOf(var3.getAsShort());
        } else {
            return param0 instanceof LongTag var4 ? String.valueOf(var4.getAsLong()) : param0.getAsString();
        }
    }

    private static void lookupValues(List<String> param0, IntList param1, List<String> param2) {
        param2.clear();
        param1.forEach(param2x -> param2.add(param0.get(param2x)));
    }

    private InstantiatedFunction<T> substituteAndParse(List<String> param0, List<String> param1, CommandDispatcher<T> param2, T param3) throws FunctionInstantiationException {
        List<UnboundEntryAction<T>> var0 = new ArrayList<>(this.entries.size());
        List<String> var1 = new ArrayList<>(param1.size());

        for(MacroFunction.Entry<T> var2 : this.entries) {
            lookupValues(param1, var2.parameters(), var1);
            var0.add(var2.instantiate(var1, param2, param3, this.id));
        }

        return new PlainTextFunction<>(this.id().withPath(param1x -> param1x + "/" + param0.hashCode()), var0);
    }

    interface Entry<T> {
        IntList parameters();

        UnboundEntryAction<T> instantiate(List<String> var1, CommandDispatcher<T> var2, T var3, ResourceLocation var4) throws FunctionInstantiationException;
    }

    static class MacroEntry<T extends ExecutionCommandSource<T>> implements MacroFunction.Entry<T> {
        private final StringTemplate template;
        private final IntList parameters;

        public MacroEntry(StringTemplate param0, IntList param1) {
            this.template = param0;
            this.parameters = param1;
        }

        @Override
        public IntList parameters() {
            return this.parameters;
        }

        public UnboundEntryAction<T> instantiate(List<String> param0, CommandDispatcher<T> param1, T param2, ResourceLocation param3) throws FunctionInstantiationException {
            String var0 = this.template.substitute(param0);

            try {
                return CommandFunction.parseCommand(param1, param2, new StringReader(var0));
            } catch (CommandSyntaxException var7) {
                throw new FunctionInstantiationException(
                    Component.translatable("commands.function.error.parse", Component.translationArg(param3), var0, var7.getMessage())
                );
            }
        }
    }

    static class PlainTextEntry<T> implements MacroFunction.Entry<T> {
        private final UnboundEntryAction<T> compiledAction;

        public PlainTextEntry(UnboundEntryAction<T> param0) {
            this.compiledAction = param0;
        }

        @Override
        public IntList parameters() {
            return IntLists.emptyList();
        }

        @Override
        public UnboundEntryAction<T> instantiate(List<String> param0, CommandDispatcher<T> param1, T param2, ResourceLocation param3) {
            return this.compiledAction;
        }
    }
}
