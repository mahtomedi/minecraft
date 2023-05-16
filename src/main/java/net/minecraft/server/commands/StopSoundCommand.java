package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

public class StopSoundCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> var0 = Commands.argument("targets", EntityArgument.players())
            .executes(param0x -> stopSound(param0x.getSource(), EntityArgument.getPlayers(param0x, "targets"), null, null))
            .then(
                Commands.literal("*")
                    .then(
                        Commands.argument("sound", ResourceLocationArgument.id())
                            .suggests(SuggestionProviders.AVAILABLE_SOUNDS)
                            .executes(
                                param0x -> stopSound(
                                        param0x.getSource(),
                                        EntityArgument.getPlayers(param0x, "targets"),
                                        null,
                                        ResourceLocationArgument.getId(param0x, "sound")
                                    )
                            )
                    )
            );

        for(SoundSource var1 : SoundSource.values()) {
            var0.then(
                Commands.literal(var1.getName())
                    .executes(param1 -> stopSound(param1.getSource(), EntityArgument.getPlayers(param1, "targets"), var1, null))
                    .then(
                        Commands.argument("sound", ResourceLocationArgument.id())
                            .suggests(SuggestionProviders.AVAILABLE_SOUNDS)
                            .executes(
                                param1 -> stopSound(
                                        param1.getSource(), EntityArgument.getPlayers(param1, "targets"), var1, ResourceLocationArgument.getId(param1, "sound")
                                    )
                            )
                    )
            );
        }

        param0.register(Commands.literal("stopsound").requires(param0x -> param0x.hasPermission(2)).then(var0));
    }

    private static int stopSound(CommandSourceStack param0, Collection<ServerPlayer> param1, @Nullable SoundSource param2, @Nullable ResourceLocation param3) {
        ClientboundStopSoundPacket var0 = new ClientboundStopSoundPacket(param3, param2);

        for(ServerPlayer var1 : param1) {
            var1.connection.send(var0);
        }

        if (param2 != null) {
            if (param3 != null) {
                param0.sendSuccess(() -> Component.translatable("commands.stopsound.success.source.sound", param3, param2.getName()), true);
            } else {
                param0.sendSuccess(() -> Component.translatable("commands.stopsound.success.source.any", param2.getName()), true);
            }
        } else if (param3 != null) {
            param0.sendSuccess(() -> Component.translatable("commands.stopsound.success.sourceless.sound", param3), true);
        } else {
            param0.sendSuccess(() -> Component.translatable("commands.stopsound.success.sourceless.any"), true);
        }

        return param1.size();
    }
}
