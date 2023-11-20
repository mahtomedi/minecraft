package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;

public class ServerPackCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("serverpack")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("push")
                        .then(
                            Commands.argument("url", StringArgumentType.string())
                                .then(
                                    Commands.argument("uuid", UuidArgument.uuid())
                                        .then(
                                            Commands.argument("hash", StringArgumentType.word())
                                                .executes(
                                                    param0x -> pushPack(
                                                            param0x.getSource(),
                                                            StringArgumentType.getString(param0x, "url"),
                                                            Optional.of(UuidArgument.getUuid(param0x, "uuid")),
                                                            Optional.of(StringArgumentType.getString(param0x, "hash"))
                                                        )
                                                )
                                        )
                                        .executes(
                                            param0x -> pushPack(
                                                    param0x.getSource(),
                                                    StringArgumentType.getString(param0x, "url"),
                                                    Optional.of(UuidArgument.getUuid(param0x, "uuid")),
                                                    Optional.empty()
                                                )
                                        )
                                )
                                .executes(
                                    param0x -> pushPack(param0x.getSource(), StringArgumentType.getString(param0x, "url"), Optional.empty(), Optional.empty())
                                )
                        )
                )
                .then(
                    Commands.literal("pop")
                        .then(
                            Commands.argument("uuid", UuidArgument.uuid())
                                .executes(param0x -> popPack(param0x.getSource(), UuidArgument.getUuid(param0x, "uuid")))
                        )
                )
        );
    }

    private static void sendToAllConnections(CommandSourceStack param0, Packet<?> param1) {
        param0.getServer().getConnection().getConnections().forEach(param1x -> param1x.send(param1));
    }

    private static int pushPack(CommandSourceStack param0, String param1, Optional<UUID> param2, Optional<String> param3) {
        UUID var0 = param2.orElseGet(() -> UUID.nameUUIDFromBytes(param1.getBytes(StandardCharsets.UTF_8)));
        String var1 = param3.orElse("");
        ClientboundResourcePackPushPacket var2 = new ClientboundResourcePackPushPacket(var0, param1, var1, false, null);
        sendToAllConnections(param0, var2);
        return 0;
    }

    private static int popPack(CommandSourceStack param0, UUID param1) {
        ClientboundResourcePackPopPacket var0 = new ClientboundResourcePackPopPacket(Optional.of(param1));
        sendToAllConnections(param0, var0);
        return 0;
    }
}
