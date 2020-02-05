package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class LocateBiomeCommand {
    public static final DynamicCommandExceptionType ERROR_INVALID_BIOME = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.locatebiome.invalid", param0)
    );
    private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.locatebiome.notFound", param0)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("locatebiome")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("biome", ResourceLocationArgument.id())
                        .suggests(SuggestionProviders.AVAILABLE_BIOMES)
                        .executes(param0x -> locateBiome(param0x.getSource(), getBiome(param0x, "biome")))
                )
        );
    }

    private static int locateBiome(CommandSourceStack param0, Biome param1) throws CommandSyntaxException {
        BlockPos var0 = new BlockPos(param0.getPosition());
        BlockPos var1 = param0.getLevel().findNearestBiome(param1, var0, 6400, 8);
        if (var1 == null) {
            throw ERROR_BIOME_NOT_FOUND.create(param1.getName().getString());
        } else {
            return LocateCommand.showLocateResult(param0, param1.getName().getString(), var0, var1, "commands.locatebiome.success");
        }
    }

    private static Biome getBiome(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ResourceLocation var0 = param0.getArgument(param1, ResourceLocation.class);
        return Registry.BIOME.getOptional(var0).orElseThrow(() -> ERROR_INVALID_BIOME.create(var0));
    }
}
