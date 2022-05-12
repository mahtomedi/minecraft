package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

public class LocateCommand {
    private static final DynamicCommandExceptionType ERROR_STRUCTURE_NOT_FOUND = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.locate.structure.not_found", param0)
    );
    private static final DynamicCommandExceptionType ERROR_STRUCTURE_INVALID = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.locate.structure.invalid", param0)
    );
    private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.locate.biome.not_found", param0)
    );
    private static final DynamicCommandExceptionType ERROR_BIOME_INVALID = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.locate.biome.invalid", param0)
    );
    private static final DynamicCommandExceptionType ERROR_POI_NOT_FOUND = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.locate.poi.not_found", param0)
    );
    private static final DynamicCommandExceptionType ERROR_POI_INVALID = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.locate.poi.invalid", param0)
    );
    private static final int MAX_STRUCTURE_SEARCH_RADIUS = 100;
    private static final int MAX_BIOME_SEARCH_RADIUS = 6400;
    private static final int BIOME_SAMPLE_RESOLUTION_HORIZONTAL = 32;
    private static final int BIOME_SAMPLE_RESOLUTION_VERTICAL = 64;
    private static final int POI_SEARCH_RADIUS = 256;

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("locate")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("structure")
                        .then(
                            Commands.argument("structure", ResourceOrTagLocationArgument.resourceOrTag(Registry.STRUCTURE_REGISTRY))
                                .executes(
                                    param0x -> locateStructure(
                                            param0x.getSource(),
                                            ResourceOrTagLocationArgument.getRegistryType(
                                                param0x, "structure", Registry.STRUCTURE_REGISTRY, ERROR_STRUCTURE_INVALID
                                            )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("biome")
                        .then(
                            Commands.argument("biome", ResourceOrTagLocationArgument.resourceOrTag(Registry.BIOME_REGISTRY))
                                .executes(
                                    param0x -> locateBiome(
                                            param0x.getSource(),
                                            ResourceOrTagLocationArgument.getRegistryType(param0x, "biome", Registry.BIOME_REGISTRY, ERROR_BIOME_INVALID)
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("poi")
                        .then(
                            Commands.argument("poi", ResourceOrTagLocationArgument.resourceOrTag(Registry.POINT_OF_INTEREST_TYPE_REGISTRY))
                                .executes(
                                    param0x -> locatePoi(
                                            param0x.getSource(),
                                            ResourceOrTagLocationArgument.getRegistryType(
                                                param0x, "poi", Registry.POINT_OF_INTEREST_TYPE_REGISTRY, ERROR_POI_INVALID
                                            )
                                        )
                                )
                        )
                )
        );
    }

    private static Optional<? extends HolderSet.ListBacked<Structure>> getHolders(
        ResourceOrTagLocationArgument.Result<Structure> param0, Registry<Structure> param1
    ) {
        return param0.unwrap().map(param1x -> param1.getHolder(param1x).map(param0x -> HolderSet.direct(param0x)), param1::getTag);
    }

    private static int locateStructure(CommandSourceStack param0, ResourceOrTagLocationArgument.Result<Structure> param1) throws CommandSyntaxException {
        Registry<Structure> var0 = param0.getLevel().registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
        HolderSet<Structure> var1 = getHolders(param1, var0).orElseThrow(() -> ERROR_STRUCTURE_INVALID.create(param1.asPrintable()));
        BlockPos var2 = new BlockPos(param0.getPosition());
        ServerLevel var3 = param0.getLevel();
        Pair<BlockPos, Holder<Structure>> var4 = var3.getChunkSource().getGenerator().findNearestMapStructure(var3, var1, var2, 100, false);
        if (var4 == null) {
            throw ERROR_STRUCTURE_NOT_FOUND.create(param1.asPrintable());
        } else {
            return showLocateResult(param0, param1, var2, var4, "commands.locate.structure.success", false);
        }
    }

    private static int locateBiome(CommandSourceStack param0, ResourceOrTagLocationArgument.Result<Biome> param1) throws CommandSyntaxException {
        BlockPos var0 = new BlockPos(param0.getPosition());
        Pair<BlockPos, Holder<Biome>> var1 = param0.getLevel().findClosestBiome3d(param1, var0, 6400, 32, 64);
        if (var1 == null) {
            throw ERROR_BIOME_NOT_FOUND.create(param1.asPrintable());
        } else {
            return showLocateResult(param0, param1, var0, var1, "commands.locate.biome.success", true);
        }
    }

    private static int locatePoi(CommandSourceStack param0, ResourceOrTagLocationArgument.Result<PoiType> param1) throws CommandSyntaxException {
        BlockPos var0 = new BlockPos(param0.getPosition());
        ServerLevel var1 = param0.getLevel();
        Optional<Pair<Holder<PoiType>, BlockPos>> var2 = var1.getPoiManager().findClosestWithType(param1, var0, 256, PoiManager.Occupancy.ANY);
        if (var2.isEmpty()) {
            throw ERROR_POI_NOT_FOUND.create(param1.asPrintable());
        } else {
            return showLocateResult(param0, param1, var0, var2.get().swap(), "commands.locate.poi.success", false);
        }
    }

    public static int showLocateResult(
        CommandSourceStack param0,
        ResourceOrTagLocationArgument.Result<?> param1,
        BlockPos param2,
        Pair<BlockPos, ? extends Holder<?>> param3,
        String param4,
        boolean param5
    ) {
        BlockPos var0 = param3.getFirst();
        String var1 = param1.unwrap()
            .map(
                param0x -> param0x.location().toString(),
                param1x -> "#"
                        + param1x.location()
                        + " ("
                        + (String)param3.getSecond().unwrapKey().map(param0x -> param0x.location().toString()).orElse("[unregistered]")
                        + ")"
            );
        int var2 = param5 ? Mth.floor(Mth.sqrt((float)param2.distSqr(var0))) : Mth.floor(dist(param2.getX(), param2.getZ(), var0.getX(), var0.getZ()));
        String var3 = param5 ? String.valueOf(var0.getY()) : "~";
        Component var4 = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", var0.getX(), var3, var0.getZ()))
            .withStyle(
                param2x -> param2x.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + var0.getX() + " " + var3 + " " + var0.getZ()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
            );
        param0.sendSuccess(Component.translatable(param4, var1, var4, var2), false);
        return var2;
    }

    private static float dist(int param0, int param1, int param2, int param3) {
        int var0 = param2 - param0;
        int var1 = param3 - param1;
        return Mth.sqrt((float)(var0 * var0 + var1 * var1));
    }
}
