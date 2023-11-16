package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

public class FillBiomeCommand {
    public static final SimpleCommandExceptionType ERROR_NOT_LOADED = new SimpleCommandExceptionType(Component.translatable("argument.pos.unloaded"));
    private static final Dynamic2CommandExceptionType ERROR_VOLUME_TOO_LARGE = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatableEscape("commands.fillbiome.toobig", param0, param1)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0, CommandBuildContext param1) {
        param0.register(
            Commands.literal("fillbiome")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("from", BlockPosArgument.blockPos())
                        .then(
                            Commands.argument("to", BlockPosArgument.blockPos())
                                .then(
                                    Commands.argument("biome", ResourceArgument.resource(param1, Registries.BIOME))
                                        .executes(
                                            param0x -> fill(
                                                    param0x.getSource(),
                                                    BlockPosArgument.getLoadedBlockPos(param0x, "from"),
                                                    BlockPosArgument.getLoadedBlockPos(param0x, "to"),
                                                    ResourceArgument.getResource(param0x, "biome", Registries.BIOME),
                                                    param0xx -> true
                                                )
                                        )
                                        .then(
                                            Commands.literal("replace")
                                                .then(
                                                    Commands.argument("filter", ResourceOrTagArgument.resourceOrTag(param1, Registries.BIOME))
                                                        .executes(
                                                            param0x -> fill(
                                                                    param0x.getSource(),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "from"),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "to"),
                                                                    ResourceArgument.getResource(param0x, "biome", Registries.BIOME),
                                                                    ResourceOrTagArgument.getResourceOrTag(param0x, "filter", Registries.BIOME)::test
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int quantize(int param0) {
        return QuartPos.toBlock(QuartPos.fromBlock(param0));
    }

    private static BlockPos quantize(BlockPos param0) {
        return new BlockPos(quantize(param0.getX()), quantize(param0.getY()), quantize(param0.getZ()));
    }

    private static BiomeResolver makeResolver(MutableInt param0, ChunkAccess param1, BoundingBox param2, Holder<Biome> param3, Predicate<Holder<Biome>> param4) {
        return (param5, param6, param7, param8) -> {
            int var0x = QuartPos.toBlock(param5);
            int var1x = QuartPos.toBlock(param6);
            int var2x = QuartPos.toBlock(param7);
            Holder<Biome> var3x = param1.getNoiseBiome(param5, param6, param7);
            if (param2.isInside(var0x, var1x, var2x) && param4.test(var3x)) {
                param0.increment();
                return param3;
            } else {
                return var3x;
            }
        };
    }

    public static Either<Integer, CommandSyntaxException> fill(ServerLevel param0, BlockPos param1, BlockPos param2, Holder<Biome> param3) {
        return fill(param0, param1, param2, param3, param0x -> true, param0x -> {
        });
    }

    public static Either<Integer, CommandSyntaxException> fill(
        ServerLevel param0, BlockPos param1, BlockPos param2, Holder<Biome> param3, Predicate<Holder<Biome>> param4, Consumer<Supplier<Component>> param5
    ) {
        BlockPos var0 = quantize(param1);
        BlockPos var1 = quantize(param2);
        BoundingBox var2 = BoundingBox.fromCorners(var0, var1);
        int var3 = var2.getXSpan() * var2.getYSpan() * var2.getZSpan();
        int var4 = param0.getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);
        if (var3 > var4) {
            return Either.right(ERROR_VOLUME_TOO_LARGE.create(var4, var3));
        } else {
            List<ChunkAccess> var5 = new ArrayList<>();

            for(int var6 = SectionPos.blockToSectionCoord(var2.minZ()); var6 <= SectionPos.blockToSectionCoord(var2.maxZ()); ++var6) {
                for(int var7 = SectionPos.blockToSectionCoord(var2.minX()); var7 <= SectionPos.blockToSectionCoord(var2.maxX()); ++var7) {
                    ChunkAccess var8 = param0.getChunk(var7, var6, ChunkStatus.FULL, false);
                    if (var8 == null) {
                        return Either.right(ERROR_NOT_LOADED.create());
                    }

                    var5.add(var8);
                }
            }

            MutableInt var9 = new MutableInt(0);

            for(ChunkAccess var10 : var5) {
                var10.fillBiomesFromNoise(makeResolver(var9, var10, var2, param3, param4), param0.getChunkSource().randomState().sampler());
                var10.setUnsaved(true);
            }

            param0.getChunkSource().chunkMap.resendBiomesForChunks(var5);
            param5.accept(
                () -> Component.translatable(
                        "commands.fillbiome.success.count", var9.getValue(), var2.minX(), var2.minY(), var2.minZ(), var2.maxX(), var2.maxY(), var2.maxZ()
                    )
            );
            return Either.left(var9.getValue());
        }
    }

    private static int fill(CommandSourceStack param0, BlockPos param1, BlockPos param2, Holder.Reference<Biome> param3, Predicate<Holder<Biome>> param4) throws CommandSyntaxException {
        Either<Integer, CommandSyntaxException> var0 = fill(param0.getLevel(), param1, param2, param3, param4, param1x -> param0.sendSuccess(param1x, true));
        Optional<CommandSyntaxException> var1 = var0.right();
        if (var1.isPresent()) {
            throw (CommandSyntaxException)var1.get();
        } else {
            return var0.left().get();
        }
    }
}
