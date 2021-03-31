package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;

public class DebugPathCommand {
    private static final SimpleCommandExceptionType ERROR_NOT_MOB = new SimpleCommandExceptionType(new TextComponent("Source is not a mob"));
    private static final SimpleCommandExceptionType ERROR_NO_PATH = new SimpleCommandExceptionType(new TextComponent("Path not found"));
    private static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(new TextComponent("Target not reached"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("debugpath")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("to", BlockPosArgument.blockPos())
                        .executes(param0x -> fillBlocks(param0x.getSource(), BlockPosArgument.getLoadedBlockPos(param0x, "to")))
                )
        );
    }

    private static int fillBlocks(CommandSourceStack param0, BlockPos param1) throws CommandSyntaxException {
        Entity var0 = param0.getEntity();
        if (!(var0 instanceof Mob)) {
            throw ERROR_NOT_MOB.create();
        } else {
            Mob var1 = (Mob)var0;
            PathNavigation var2 = new GroundPathNavigation(var1, param0.getLevel());
            Path var3 = var2.createPath(param1, 0);
            DebugPackets.sendPathFindingPacket(param0.getLevel(), var1, var3, var2.getMaxDistanceToWaypoint());
            if (var3 == null) {
                throw ERROR_NO_PATH.create();
            } else if (!var3.canReach()) {
                throw ERROR_NOT_COMPLETE.create();
            } else {
                param0.sendSuccess(new TextComponent("Made path"), true);
                return 1;
            }
        }
    }
}
