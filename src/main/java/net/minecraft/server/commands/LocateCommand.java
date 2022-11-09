package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
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
    private static final DynamicCommandExceptionType ERROR_POI_NOT_FOUND = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.locate.poi.not_found", param0)
    );
    private static final int MAX_STRUCTURE_SEARCH_RADIUS = 100;
    private static final int MAX_BIOME_SEARCH_RADIUS = 6400;
    private static final int BIOME_SAMPLE_RESOLUTION_HORIZONTAL = 32;
    private static final int BIOME_SAMPLE_RESOLUTION_VERTICAL = 64;
    private static final int POI_SEARCH_RADIUS = 256;

    public static void register(CommandDispatcher<CommandSourceStack> param0, CommandBuildContext param1) {
        param0.register(
            Commands.literal("locate")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("structure")
                        .then(
                            Commands.argument("structure", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.STRUCTURE))
                                .executes(
                                    param0x -> locateStructure(
                                            param0x.getSource(),
                                            ResourceOrTagKeyArgument.getResourceOrTagKey(param0x, "structure", Registries.STRUCTURE, ERROR_STRUCTURE_INVALID)
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("biome")
                        .then(
                            Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(param1, Registries.BIOME))
                                .executes(
                                    param0x -> locateBiome(param0x.getSource(), ResourceOrTagArgument.getResourceOrTag(param0x, "biome", Registries.BIOME))
                                )
                        )
                )
                .then(
                    Commands.literal("poi")
                        .then(
                            Commands.argument("poi", ResourceOrTagArgument.resourceOrTag(param1, Registries.POINT_OF_INTEREST_TYPE))
                                .executes(
                                    param0x -> locatePoi(
                                            param0x.getSource(), ResourceOrTagArgument.getResourceOrTag(param0x, "poi", Registries.POINT_OF_INTEREST_TYPE)
                                        )
                                )
                        )
                )
        );
    }

    private static Optional<? extends HolderSet.ListBacked<Structure>> getHolders(ResourceOrTagKeyArgument.Result<Structure> param0, Registry<Structure> param1) {
        return param0.unwrap().map(param1x -> param1.getHolder(param1x).map(param0x -> HolderSet.direct(param0x)), param1::getTag);
    }

    private static int locateStructure(CommandSourceStack param0, ResourceOrTagKeyArgument.Result<Structure> param1) throws CommandSyntaxException {
        Registry<Structure> var0 = param0.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
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

    private static int locateBiome(CommandSourceStack param0, ResourceOrTagArgument.Result<Biome> param1) throws CommandSyntaxException {
        BlockPos var0 = new BlockPos(param0.getPosition());
        Pair<BlockPos, Holder<Biome>> var1 = param0.getLevel().findClosestBiome3d(param1, var0, 6400, 32, 64);
        if (var1 == null) {
            throw ERROR_BIOME_NOT_FOUND.create(param1.asPrintable());
        } else {
            return showLocateResult(param0, param1, var0, var1, "commands.locate.biome.success", true);
        }
    }

    private static int locatePoi(CommandSourceStack param0, ResourceOrTagArgument.Result<PoiType> param1) throws CommandSyntaxException {
        BlockPos var0 = new BlockPos(param0.getPosition());
        ServerLevel var1 = param0.getLevel();
        Optional<Pair<Holder<PoiType>, BlockPos>> var2 = var1.getPoiManager().findClosestWithType(param1, var0, 256, PoiManager.Occupancy.ANY);
        if (var2.isEmpty()) {
            throw ERROR_POI_NOT_FOUND.create(param1.asPrintable());
        } else {
            return showLocateResult(param0, param1, var0, var2.get().swap(), "commands.locate.poi.success", false);
        }
    }

    private static String getElementName(Pair<BlockPos, ? extends Holder<?>> param0) {
        return param0.getSecond().unwrapKey().map(param0x -> param0x.location().toString()).orElse("[unregistered]");
    }

    public static int showLocateResult(
        CommandSourceStack param0,
        ResourceOrTagArgument.Result<?> param1,
        BlockPos param2,
        Pair<BlockPos, ? extends Holder<?>> param3,
        String param4,
        boolean param5
    ) {
        String var0 = param1.unwrap().map(param1x -> param1.asPrintable(), param2x -> param1.asPrintable() + " (" + getElementName(param3) + ")");
        return showLocateResult(param0, param2, param3, param4, param5, var0);
    }

    public static int showLocateResult(
        CommandSourceStack param0,
        ResourceOrTagKeyArgument.Result<?> param1,
        BlockPos param2,
        Pair<BlockPos, ? extends Holder<?>> param3,
        String param4,
        boolean param5
    ) {
        String var0 = param1.unwrap().map(param0x -> param0x.location().toString(), param1x -> "#" + param1x.location() + " (" + getElementName(param3) + ")");
        return showLocateResult(param0, param2, param3, param4, param5, var0);
    }

    private static int showLocateResult(
        CommandSourceStack param0, BlockPos param1, Pair<BlockPos, ? extends Holder<?>> param2, String param3, boolean param4, String param5
    ) {
        BlockPos var0 = param2.getFirst();
        int var1 = param4 ? Mth.floor(Mth.sqrt((float)param1.distSqr(var0))) : Mth.floor(dist(param1.getX(), param1.getZ(), var0.getX(), var0.getZ()));
        String var2 = param4 ? String.valueOf(var0.getY()) : "~";
        Component var3 = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", var0.getX(), var2, var0.getZ()))
            .withStyle(
                param2x -> param2x.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + var0.getX() + " " + var2 + " " + var0.getZ()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
            );
        param0.sendSuccess(Component.translatable(param3, param5, var3, var1), false);
        return var1;
    }

    private static float dist(int param0, int param1, int param2, int param3) {
        int var0 = param2 - param0;
        int var1 = param3 - param1;
        return Mth.sqrt((float)(var0 * var0 + var1 * var1));
    }
}
