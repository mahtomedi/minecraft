package net.minecraft.server.commands.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class EntityDataAccessor implements DataAccessor {
    private static final SimpleCommandExceptionType ERROR_NO_PLAYERS = new SimpleCommandExceptionType(new TranslatableComponent("commands.data.entity.invalid"));
    public static final Function<String, DataCommands.DataProvider> PROVIDER = param0 -> new DataCommands.DataProvider() {
            @Override
            public DataAccessor access(CommandContext<CommandSourceStack> param0x) throws CommandSyntaxException {
                return new EntityDataAccessor(EntityArgument.getEntity(param0, param0));
            }

            @Override
            public ArgumentBuilder<CommandSourceStack, ?> wrap(
                ArgumentBuilder<CommandSourceStack, ?> param0x, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> param1
            ) {
                return param0.then(
                    Commands.literal("entity").then((ArgumentBuilder<CommandSourceStack, ?>)param1.apply(Commands.argument(param0, EntityArgument.entity())))
                );
            }
        };
    private final Entity entity;

    public EntityDataAccessor(Entity param0) {
        this.entity = param0;
    }

    @Override
    public void setData(CompoundTag param0) throws CommandSyntaxException {
        if (this.entity instanceof Player) {
            throw ERROR_NO_PLAYERS.create();
        } else {
            UUID var0 = this.entity.getUUID();
            this.entity.load(param0);
            this.entity.setUUID(var0);
        }
    }

    @Override
    public CompoundTag getData() {
        return NbtPredicate.getEntityTagToCompare(this.entity);
    }

    @Override
    public Component getModifiedSuccess() {
        return new TranslatableComponent("commands.data.entity.modified", this.entity.getDisplayName());
    }

    @Override
    public Component getPrintSuccess(Tag param0) {
        return new TranslatableComponent("commands.data.entity.query", this.entity.getDisplayName(), param0.getPrettyDisplay());
    }

    @Override
    public Component getPrintSuccess(NbtPathArgument.NbtPath param0, double param1, int param2) {
        return new TranslatableComponent("commands.data.entity.get", param0, this.entity.getDisplayName(), String.format(Locale.ROOT, "%.2f", param1), param2);
    }
}
