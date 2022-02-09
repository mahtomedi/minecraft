package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
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
        BlockPos var0 = new BlockPos(param0.getPosition());
        BlockPos var1 = param0.getLevel().findNearestBiome(ResourceKey.create(Registry.BIOME_REGISTRY, param1.id()), var0, 6400, 8);
        String var2 = param1.id().toString();
        if (var1 == null) {
            throw ERROR_BIOME_NOT_FOUND.create(var2);
        } else {
            return LocateCommand.showLocateResult(param0, var2, var0, var1, "commands.locatebiome.success");
        }
    }
}
