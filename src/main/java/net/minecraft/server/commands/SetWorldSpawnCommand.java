package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSpawnPositionPacket;

public class SetWorldSpawnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("setworldspawn")
                .requires(param0x -> param0x.hasPermission(2))
                .executes(param0x -> setSpawn(param0x.getSource(), new BlockPos(param0x.getSource().getPosition())))
                .then(
                    Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(param0x -> setSpawn(param0x.getSource(), BlockPosArgument.getOrLoadBlockPos(param0x, "pos")))
                )
        );
    }

    private static int setSpawn(CommandSourceStack param0, BlockPos param1) {
        param0.getLevel().setSpawnPos(param1);
        param0.getServer().getPlayerList().broadcastAll(new ClientboundSetSpawnPositionPacket(param1));
        param0.sendSuccess(new TranslatableComponent("commands.setworldspawn.success", param1.getX(), param1.getY(), param1.getZ()), true);
        return 1;
    }
}
