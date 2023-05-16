package net.minecraft.server.commands.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class DataCommands {
    private static final SimpleCommandExceptionType ERROR_MERGE_UNCHANGED = new SimpleCommandExceptionType(Component.translatable("commands.data.merge.failed"));
    private static final DynamicCommandExceptionType ERROR_GET_NOT_NUMBER = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.data.get.invalid", param0)
    );
    private static final DynamicCommandExceptionType ERROR_GET_NON_EXISTENT = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.data.get.unknown", param0)
    );
    private static final SimpleCommandExceptionType ERROR_MULTIPLE_TAGS = new SimpleCommandExceptionType(Component.translatable("commands.data.get.multiple"));
    private static final DynamicCommandExceptionType ERROR_EXPECTED_OBJECT = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.data.modify.expected_object", param0)
    );
    private static final DynamicCommandExceptionType ERROR_EXPECTED_VALUE = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.data.modify.expected_value", param0)
    );
    private static final Dynamic2CommandExceptionType ERROR_INVALID_SUBSTRING = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("commands.data.modify.invalid_substring", param0, param1)
    );
    public static final List<Function<String, DataCommands.DataProvider>> ALL_PROVIDERS = ImmutableList.of(
        EntityDataAccessor.PROVIDER, BlockDataAccessor.PROVIDER, StorageDataAccessor.PROVIDER
    );
    public static final List<DataCommands.DataProvider> TARGET_PROVIDERS = ALL_PROVIDERS.stream()
        .map(param0 -> param0.apply("target"))
        .collect(ImmutableList.toImmutableList());
    public static final List<DataCommands.DataProvider> SOURCE_PROVIDERS = ALL_PROVIDERS.stream()
        .map(param0 -> param0.apply("source"))
        .collect(ImmutableList.toImmutableList());

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralArgumentBuilder<CommandSourceStack> var0 = Commands.literal("data").requires(param0x -> param0x.hasPermission(2));

        for(DataCommands.DataProvider var1 : TARGET_PROVIDERS) {
            var0.then(
                    var1.wrap(
                        Commands.literal("merge"),
                        param1 -> param1.then(
                                Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                    .executes(
                                        param1x -> mergeData(param1x.getSource(), var1.access(param1x), CompoundTagArgument.getCompoundTag(param1x, "nbt"))
                                    )
                            )
                    )
                )
                .then(
                    var1.wrap(
                        Commands.literal("get"),
                        param1 -> param1.executes(param1x -> getData(param1x.getSource(), var1.access(param1x)))
                                .then(
                                    Commands.argument("path", NbtPathArgument.nbtPath())
                                        .executes(param1x -> getData(param1x.getSource(), var1.access(param1x), NbtPathArgument.getPath(param1x, "path")))
                                        .then(
                                            Commands.argument("scale", DoubleArgumentType.doubleArg())
                                                .executes(
                                                    param1x -> getNumeric(
                                                            param1x.getSource(),
                                                            var1.access(param1x),
                                                            NbtPathArgument.getPath(param1x, "path"),
                                                            DoubleArgumentType.getDouble(param1x, "scale")
                                                        )
                                                )
                                        )
                                )
                    )
                )
                .then(
                    var1.wrap(
                        Commands.literal("remove"),
                        param1 -> param1.then(
                                Commands.argument("path", NbtPathArgument.nbtPath())
                                    .executes(param1x -> removeData(param1x.getSource(), var1.access(param1x), NbtPathArgument.getPath(param1x, "path")))
                            )
                    )
                )
                .then(
                    decorateModification(
                        (param0x, param1) -> param0x.then(
                                    Commands.literal("insert")
                                        .then(
                                            Commands.argument("index", IntegerArgumentType.integer())
                                                .then(
                                                    param1.create(
                                                        (param0xx, param1x, param2, param3) -> param2.insert(
                                                                IntegerArgumentType.getInteger(param0xx, "index"), param1x, param3
                                                            )
                                                    )
                                                )
                                        )
                                )
                                .then(Commands.literal("prepend").then(param1.create((param0xx, param1x, param2, param3) -> param2.insert(0, param1x, param3))))
                                .then(Commands.literal("append").then(param1.create((param0xx, param1x, param2, param3) -> param2.insert(-1, param1x, param3))))
                                .then(
                                    Commands.literal("set")
                                        .then(param1.create((param0xx, param1x, param2, param3) -> param2.set(param1x, Iterables.getLast(param3))))
                                )
                                .then(Commands.literal("merge").then(param1.create((param0xx, param1x, param2, param3) -> {
                                    CompoundTag var0x = new CompoundTag();
                
                                    for(Tag var1x : param3) {
                                        if (NbtPathArgument.NbtPath.isTooDeep(var1x, 0)) {
                                            throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
                                        }
                
                                        if (!(var1x instanceof CompoundTag)) {
                                            throw ERROR_EXPECTED_OBJECT.create(var1x);
                                        }
                
                                        CompoundTag var2 = (CompoundTag)var1x;
                                        var0x.merge(var2);
                                    }
                
                                    Collection<Tag> var3 = param2.getOrCreate(param1x, CompoundTag::new);
                                    int var4 = 0;
                
                                    for(Tag var5 : var3) {
                                        if (!(var5 instanceof CompoundTag)) {
                                            throw ERROR_EXPECTED_OBJECT.create(var5);
                                        }
                
                                        CompoundTag var6 = (CompoundTag)var5;
                                        CompoundTag var8 = var6.copy();
                                        var6.merge(var0x);
                                        var4 += var8.equals(var6) ? 0 : 1;
                                    }
                
                                    return var4;
                                })))
                    )
                );
        }

        param0.register(var0);
    }

    private static String getAsText(Tag param0) throws CommandSyntaxException {
        if (param0.getType().isValue()) {
            return param0.getAsString();
        } else {
            throw ERROR_EXPECTED_VALUE.create(param0);
        }
    }

    private static List<Tag> stringifyTagList(List<Tag> param0, DataCommands.StringProcessor param1) throws CommandSyntaxException {
        List<Tag> var0 = new ArrayList<>(param0.size());

        for(Tag var1 : param0) {
            String var2 = getAsText(var1);
            var0.add(StringTag.valueOf(param1.process(var2)));
        }

        return var0;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> decorateModification(
        BiConsumer<ArgumentBuilder<CommandSourceStack, ?>, DataCommands.DataManipulatorDecorator> param0
    ) {
        LiteralArgumentBuilder<CommandSourceStack> var0 = Commands.literal("modify");

        for(DataCommands.DataProvider var1 : TARGET_PROVIDERS) {
            var1.wrap(
                var0,
                param2 -> {
                    ArgumentBuilder<CommandSourceStack, ?> var0x = Commands.argument("targetPath", NbtPathArgument.nbtPath());
    
                    for(DataCommands.DataProvider var1x : SOURCE_PROVIDERS) {
                        param0.accept(
                            var0x,
                            param2x -> var1x.wrap(
                                    Commands.literal("from"),
                                    param3 -> param3.executes(param3x -> manipulateData(param3x, var1, param2x, getSingletonSource(param3x, var1x)))
                                            .then(
                                                Commands.argument("sourcePath", NbtPathArgument.nbtPath())
                                                    .executes(param3x -> manipulateData(param3x, var1, param2x, resolveSourcePath(param3x, var1x)))
                                            )
                                )
                        );
                        param0.accept(
                            var0x,
                            param2x -> var1x.wrap(
                                    Commands.literal("string"),
                                    param3 -> param3.executes(
                                                param3x -> manipulateData(
                                                        param3x, var1, param2x, stringifyTagList(getSingletonSource(param3x, var1x), param0x -> param0x)
                                                    )
                                            )
                                            .then(
                                                Commands.argument("sourcePath", NbtPathArgument.nbtPath())
                                                    .executes(
                                                        param3x -> manipulateData(
                                                                param3x, var1, param2x, stringifyTagList(resolveSourcePath(param3x, var1x), param0x -> param0x)
                                                            )
                                                    )
                                                    .then(
                                                        Commands.argument("start", IntegerArgumentType.integer())
                                                            .executes(
                                                                param3x -> manipulateData(
                                                                        param3x,
                                                                        var1,
                                                                        param2x,
                                                                        stringifyTagList(
                                                                            resolveSourcePath(param3x, var1x),
                                                                            param1x -> substring(param1x, IntegerArgumentType.getInteger(param3x, "start"))
                                                                        )
                                                                    )
                                                            )
                                                            .then(
                                                                Commands.argument("end", IntegerArgumentType.integer())
                                                                    .executes(
                                                                        param3x -> manipulateData(
                                                                                param3x,
                                                                                var1,
                                                                                param2x,
                                                                                stringifyTagList(
                                                                                    resolveSourcePath(param3x, var1x),
                                                                                    param1x -> substring(
                                                                                            param1x,
                                                                                            IntegerArgumentType.getInteger(param3x, "start"),
                                                                                            IntegerArgumentType.getInteger(param3x, "end")
                                                                                        )
                                                                                )
                                                                            )
                                                                    )
                                                            )
                                                    )
                                            )
                                )
                        );
                    }
    
                    param0.accept(var0x, param1x -> Commands.literal("value").then(Commands.argument("value", NbtTagArgument.nbtTag()).executes(param2x -> {
                            List<Tag> var0xx = Collections.singletonList(NbtTagArgument.getNbtTag(param2x, "value"));
                            return manipulateData(param2x, var1, param1x, var0xx);
                        })));
                    return param2.then(var0x);
                }
            );
        }

        return var0;
    }

    private static String validatedSubstring(String param0, int param1, int param2) throws CommandSyntaxException {
        if (param1 >= 0 && param2 <= param0.length() && param1 <= param2) {
            return param0.substring(param1, param2);
        } else {
            throw ERROR_INVALID_SUBSTRING.create(param1, param2);
        }
    }

    private static String substring(String param0, int param1, int param2) throws CommandSyntaxException {
        int var0 = param0.length();
        int var1 = getOffset(param1, var0);
        int var2 = getOffset(param2, var0);
        return validatedSubstring(param0, var1, var2);
    }

    private static String substring(String param0, int param1) throws CommandSyntaxException {
        int var0 = param0.length();
        return validatedSubstring(param0, getOffset(param1, var0), var0);
    }

    private static int getOffset(int param0, int param1) {
        return param0 >= 0 ? param0 : param1 + param0;
    }

    private static List<Tag> getSingletonSource(CommandContext<CommandSourceStack> param0, DataCommands.DataProvider param1) throws CommandSyntaxException {
        DataAccessor var0 = param1.access(param0);
        return Collections.singletonList(var0.getData());
    }

    private static List<Tag> resolveSourcePath(CommandContext<CommandSourceStack> param0, DataCommands.DataProvider param1) throws CommandSyntaxException {
        DataAccessor var0 = param1.access(param0);
        NbtPathArgument.NbtPath var1 = NbtPathArgument.getPath(param0, "sourcePath");
        return var1.get(var0.getData());
    }

    private static int manipulateData(
        CommandContext<CommandSourceStack> param0, DataCommands.DataProvider param1, DataCommands.DataManipulator param2, List<Tag> param3
    ) throws CommandSyntaxException {
        DataAccessor var0 = param1.access(param0);
        NbtPathArgument.NbtPath var1 = NbtPathArgument.getPath(param0, "targetPath");
        CompoundTag var2 = var0.getData();
        int var3 = param2.modify(param0, var2, var1, param3);
        if (var3 == 0) {
            throw ERROR_MERGE_UNCHANGED.create();
        } else {
            var0.setData(var2);
            param0.getSource().sendSuccess(() -> var0.getModifiedSuccess(), true);
            return var3;
        }
    }

    private static int removeData(CommandSourceStack param0, DataAccessor param1, NbtPathArgument.NbtPath param2) throws CommandSyntaxException {
        CompoundTag var0 = param1.getData();
        int var1 = param2.remove(var0);
        if (var1 == 0) {
            throw ERROR_MERGE_UNCHANGED.create();
        } else {
            param1.setData(var0);
            param0.sendSuccess(() -> param1.getModifiedSuccess(), true);
            return var1;
        }
    }

    private static Tag getSingleTag(NbtPathArgument.NbtPath param0, DataAccessor param1) throws CommandSyntaxException {
        Collection<Tag> var0 = param0.get(param1.getData());
        Iterator<Tag> var1 = var0.iterator();
        Tag var2 = var1.next();
        if (var1.hasNext()) {
            throw ERROR_MULTIPLE_TAGS.create();
        } else {
            return var2;
        }
    }

    private static int getData(CommandSourceStack param0, DataAccessor param1, NbtPathArgument.NbtPath param2) throws CommandSyntaxException {
        Tag var0 = getSingleTag(param2, param1);
        int var1;
        if (var0 instanceof NumericTag) {
            var1 = Mth.floor(((NumericTag)var0).getAsDouble());
        } else if (var0 instanceof CollectionTag) {
            var1 = ((CollectionTag)var0).size();
        } else if (var0 instanceof CompoundTag) {
            var1 = ((CompoundTag)var0).size();
        } else {
            if (!(var0 instanceof StringTag)) {
                throw ERROR_GET_NON_EXISTENT.create(param2.toString());
            }

            var1 = var0.getAsString().length();
        }

        param0.sendSuccess(() -> param1.getPrintSuccess(var0), false);
        return var1;
    }

    private static int getNumeric(CommandSourceStack param0, DataAccessor param1, NbtPathArgument.NbtPath param2, double param3) throws CommandSyntaxException {
        Tag var0 = getSingleTag(param2, param1);
        if (!(var0 instanceof NumericTag)) {
            throw ERROR_GET_NOT_NUMBER.create(param2.toString());
        } else {
            int var1 = Mth.floor(((NumericTag)var0).getAsDouble() * param3);
            param0.sendSuccess(() -> param1.getPrintSuccess(param2, param3, var1), false);
            return var1;
        }
    }

    private static int getData(CommandSourceStack param0, DataAccessor param1) throws CommandSyntaxException {
        CompoundTag var0 = param1.getData();
        param0.sendSuccess(() -> param1.getPrintSuccess(var0), false);
        return 1;
    }

    private static int mergeData(CommandSourceStack param0, DataAccessor param1, CompoundTag param2) throws CommandSyntaxException {
        CompoundTag var0 = param1.getData();
        if (NbtPathArgument.NbtPath.isTooDeep(param2, 0)) {
            throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
        } else {
            CompoundTag var1 = var0.copy().merge(param2);
            if (var0.equals(var1)) {
                throw ERROR_MERGE_UNCHANGED.create();
            } else {
                param1.setData(var1);
                param0.sendSuccess(() -> param1.getModifiedSuccess(), true);
                return 1;
            }
        }
    }

    @FunctionalInterface
    interface DataManipulator {
        int modify(CommandContext<CommandSourceStack> var1, CompoundTag var2, NbtPathArgument.NbtPath var3, List<Tag> var4) throws CommandSyntaxException;
    }

    @FunctionalInterface
    interface DataManipulatorDecorator {
        ArgumentBuilder<CommandSourceStack, ?> create(DataCommands.DataManipulator var1);
    }

    public interface DataProvider {
        DataAccessor access(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        ArgumentBuilder<CommandSourceStack, ?> wrap(
            ArgumentBuilder<CommandSourceStack, ?> var1, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> var2
        );
    }

    @FunctionalInterface
    interface StringProcessor {
        String process(String var1) throws CommandSyntaxException;
    }
}
