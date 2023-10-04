package net.minecraft.server.commands.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.CommandStorage;

public class StorageDataAccessor implements DataAccessor {
    static final SuggestionProvider<CommandSourceStack> SUGGEST_STORAGE = (param0, param1) -> SharedSuggestionProvider.suggestResource(
            getGlobalTags(param0).keys(), param1
        );
    public static final Function<String, DataCommands.DataProvider> PROVIDER = param0 -> new DataCommands.DataProvider() {
            @Override
            public DataAccessor access(CommandContext<CommandSourceStack> param0x) {
                return new StorageDataAccessor(StorageDataAccessor.getGlobalTags(param0), ResourceLocationArgument.getId(param0, param0));
            }

            @Override
            public ArgumentBuilder<CommandSourceStack, ?> wrap(
                ArgumentBuilder<CommandSourceStack, ?> param0x, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> param1
            ) {
                return param0.then(
                    Commands.literal("storage")
                        .then(param1.apply(Commands.argument(param0, ResourceLocationArgument.id()).suggests(StorageDataAccessor.SUGGEST_STORAGE)))
                );
            }
        };
    private final CommandStorage storage;
    private final ResourceLocation id;

    static CommandStorage getGlobalTags(CommandContext<CommandSourceStack> param0) {
        return param0.getSource().getServer().getCommandStorage();
    }

    StorageDataAccessor(CommandStorage param0, ResourceLocation param1) {
        this.storage = param0;
        this.id = param1;
    }

    @Override
    public void setData(CompoundTag param0) {
        this.storage.set(this.id, param0);
    }

    @Override
    public CompoundTag getData() {
        return this.storage.get(this.id);
    }

    @Override
    public Component getModifiedSuccess() {
        return Component.translatable("commands.data.storage.modified", Component.translationArg(this.id));
    }

    @Override
    public Component getPrintSuccess(Tag param0) {
        return Component.translatable("commands.data.storage.query", Component.translationArg(this.id), NbtUtils.toPrettyComponent(param0));
    }

    @Override
    public Component getPrintSuccess(NbtPathArgument.NbtPath param0, double param1, int param2) {
        return Component.translatable(
            "commands.data.storage.get", param0.asString(), Component.translationArg(this.id), String.format(Locale.ROOT, "%.2f", param1), param2
        );
    }
}
