package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

public class KillCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("kill")
                .requires(param0x -> param0x.hasPermission(2))
                .executes(param0x -> kill(param0x.getSource(), ImmutableList.of(param0x.getSource().getEntityOrException())))
                .then(
                    Commands.argument("targets", EntityArgument.entities())
                        .executes(param0x -> kill(param0x.getSource(), EntityArgument.getEntities(param0x, "targets")))
                )
        );
    }

    private static int kill(CommandSourceStack param0, Collection<? extends Entity> param1) {
        for(Entity var0 : param1) {
            var0.kill();
        }

        if (param1.size() == 1) {
            param0.sendSuccess(new TranslatableComponent("commands.kill.success.single", param1.iterator().next().getDisplayName()), true);
        } else {
            param0.sendSuccess(new TranslatableComponent("commands.kill.success.multiple", param1.size()), true);
        }

        return param1.size();
    }
}
