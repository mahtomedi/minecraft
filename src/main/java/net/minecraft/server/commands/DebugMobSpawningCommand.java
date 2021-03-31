package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;

public class DebugMobSpawningCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralArgumentBuilder<CommandSourceStack> var0 = Commands.literal("debugmobspawning").requires(param0x -> param0x.hasPermission(2));

        for(MobCategory var1 : MobCategory.values()) {
            var0.then(
                Commands.literal(var1.getName())
                    .then(
                        Commands.argument("at", BlockPosArgument.blockPos())
                            .executes(param1 -> spawnMobs(param1.getSource(), var1, BlockPosArgument.getLoadedBlockPos(param1, "at")))
                    )
            );
        }

        param0.register(var0);
    }

    private static int spawnMobs(CommandSourceStack param0, MobCategory param1, BlockPos param2) {
        NaturalSpawner.spawnCategoryForPosition(param1, param0.getLevel(), param2);
        return 1;
    }
}
