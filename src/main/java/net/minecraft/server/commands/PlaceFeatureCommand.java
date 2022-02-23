package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class PlaceFeatureCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.placefeature.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("placefeature")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("feature", ResourceKeyArgument.key(Registry.CONFIGURED_FEATURE_REGISTRY))
                        .executes(
                            param0x -> placeFeature(
                                    param0x.getSource(),
                                    ResourceKeyArgument.getConfiguredFeature(param0x, "feature"),
                                    new BlockPos(param0x.getSource().getPosition())
                                )
                        )
                        .then(
                            Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(
                                    param0x -> placeFeature(
                                            param0x.getSource(),
                                            ResourceKeyArgument.getConfiguredFeature(param0x, "feature"),
                                            BlockPosArgument.getLoadedBlockPos(param0x, "pos")
                                        )
                                )
                        )
                )
        );
    }

    public static int placeFeature(CommandSourceStack param0, Holder<ConfiguredFeature<?, ?>> param1, BlockPos param2) throws CommandSyntaxException {
        ServerLevel var0 = param0.getLevel();
        ConfiguredFeature<?, ?> var1 = param1.value();
        if (!var1.place(var0, var0.getChunkSource().getGenerator(), var0.getRandom(), param2)) {
            throw ERROR_FAILED.create();
        } else {
            String var2 = param1.unwrapKey().map(param0x -> param0x.location().toString()).orElse("[unregistered]");
            param0.sendSuccess(new TranslatableComponent("commands.placefeature.success", var2, param2.getX(), param2.getY(), param2.getZ()), true);
            return 1;
        }
    }
}
