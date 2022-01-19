package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.biome.Biome;

public class LocateBiomeCommand {
    private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.locatebiome.notFound", param0)
    );
    private static final int MAX_SEARCH_RADIUS = 6400;
    private static final int SEARCH_STEP = 8;

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("locatebiome")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("biome", ResourceLocationArgument.id())
                        .suggests(SuggestionProviders.AVAILABLE_BIOMES)
                        .executes(param0x -> locateBiome(param0x.getSource(), ResourceLocationArgument.getBiome(param0x, "biome")))
                )
        );
    }

    private static int locateBiome(CommandSourceStack param0, ResourceLocationArgument.LocatedResource<Biome> param1) throws CommandSyntaxException {
        Biome var0 = param1.resource();
        BlockPos var1 = new BlockPos(param0.getPosition());
        BlockPos var2 = param0.getLevel().findNearestBiome(var0, var1, 6400, 8);
        String var3 = param1.id().toString();
        if (var2 == null) {
            throw ERROR_BIOME_NOT_FOUND.create(var3);
        } else {
            return LocateCommand.showLocateResult(param0, var3, var1, var2, "commands.locatebiome.success");
        }
    }
}
