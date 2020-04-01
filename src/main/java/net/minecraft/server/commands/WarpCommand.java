package net.minecraft.server.commands;

import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Comparator;
import java.util.stream.IntStream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimHash;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;

public class WarpCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("warp")
                .then(
                    Commands.argument("target", StringArgumentType.greedyString())
                        .executes(param0x -> wrap(param0x.getSource(), StringArgumentType.getString(param0x, "target")))
                )
        );
    }

    private static int wrap(CommandSourceStack param0, String param1) throws CommandSyntaxException {
        DimensionType var0 = Registry.DIMENSION_TYPE.byId(DimHash.getHash(param1));
        ServerLevel var1 = param0.getServer().getLevel(var0);
        LevelChunk var2 = var1.getChunk(0, 0);
        BlockPos var3 = IntStream.range(0, 15).boxed().flatMap(param1x -> IntStream.range(0, 15).mapToObj(param2 -> {
                int var0x = var2.getHeight(Heightmap.Types.MOTION_BLOCKING, param1x, param2);
                return new BlockPos(param1x, var0x, param2);
            })).filter(param0x -> param0x.getY() > 0).max(Comparator.comparing(Vec3i::getY)).orElse(new BlockPos(0, 256, 0));
        TeleportCommand.performTeleport(
            param0,
            param0.getEntityOrException(),
            var1,
            (double)var3.getX(),
            (double)(var3.getY() + 1),
            (double)var3.getZ(),
            ImmutableSet.of(),
            0.0F,
            0.0F,
            null
        );
        return 0;
    }
}
