package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.biome.Biome;

public class LocateBiomeCommand {
    private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.locatebiome.notFound", param0)
    );
    private static final int MAX_SEARCH_RADIUS = 6400;
    private static final int SAMPLE_RESOLUTION_HORIZONTAL = 32;
    private static final int SAMPLE_RESOLUTION_VERTICAL = 64;

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("locatebiome")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("biome", ResourceOrTagLocationArgument.resourceOrTag(Registry.BIOME_REGISTRY))
                        .executes(param0x -> locateBiome(param0x.getSource(), ResourceOrTagLocationArgument.getBiome(param0x, "biome")))
                )
        );
    }

    private static int locateBiome(CommandSourceStack param0, ResourceOrTagLocationArgument.Result<Biome> param1) throws CommandSyntaxException {
        BlockPos var0 = new BlockPos(param0.getPosition());
        Pair<BlockPos, Holder<Biome>> var1 = param0.getLevel().findClosestBiome3d(param1, var0, 6400, 32, 64);
        if (var1 == null) {
            throw ERROR_BIOME_NOT_FOUND.create(param1.asPrintable());
        } else {
            return LocateCommand.showLocateResult(param0, param1, var0, var1, "commands.locatebiome.success", true);
        }
    }
}
