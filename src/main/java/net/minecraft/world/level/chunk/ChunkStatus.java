package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ChunkStatus {
    private static final EnumSet<Heightmap.Types> PRE_FEATURES = EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG);
    private static final EnumSet<Heightmap.Types> POST_FEATURES = EnumSet.of(
        Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE, Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
    );
    private static final ChunkStatus.LoadingTask PASSTHROUGH_LOAD_TASK = (param0, param1, param2, param3, param4, param5) -> {
        if (param5 instanceof ProtoChunk && !param5.getStatus().isOrAfter(param0)) {
            ((ProtoChunk)param5).setStatus(param0);
        }

        return CompletableFuture.completedFuture(Either.left(param5));
    };
    public static final ChunkStatus EMPTY = registerSimple(
        "empty", null, -1, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (param0, param1, param2, param3) -> {
        }
    );
    public static final ChunkStatus STRUCTURE_STARTS = register(
        "structure_starts", EMPTY, 0, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (param0, param1, param2, param3, param4, param5, param6, param7) -> {
            if (!param7.getStatus().isOrAfter(param0)) {
                if (param1.getServer().getWorldData().worldGenSettings().generateFeatures()) {
                    param2.createStructures(param1.structureFeatureManager(), param7, param3, param1.getSeed());
                }
    
                if (param7 instanceof ProtoChunk) {
                    ((ProtoChunk)param7).setStatus(param0);
                }
            }
    
            return CompletableFuture.completedFuture(Either.left(param7));
        }
    );
    public static final ChunkStatus STRUCTURE_REFERENCES = registerSimple(
        "structure_references",
        STRUCTURE_STARTS,
        8,
        PRE_FEATURES,
        ChunkStatus.ChunkType.PROTOCHUNK,
        (param0, param1, param2, param3) -> param1.createReferences(new WorldGenRegion(param0, param2), param0.structureFeatureManager(), param3)
    );
    public static final ChunkStatus BIOMES = registerSimple(
        "biomes", STRUCTURE_REFERENCES, 0, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (param0, param1, param2, param3) -> param1.createBiomes(param3)
    );
    public static final ChunkStatus NOISE = registerSimple(
        "noise",
        BIOMES,
        8,
        PRE_FEATURES,
        ChunkStatus.ChunkType.PROTOCHUNK,
        (param0, param1, param2, param3) -> param1.fillFromNoise(new WorldGenRegion(param0, param2), param0.structureFeatureManager(), param3)
    );
    public static final ChunkStatus SURFACE = registerSimple(
        "surface",
        NOISE,
        0,
        PRE_FEATURES,
        ChunkStatus.ChunkType.PROTOCHUNK,
        (param0, param1, param2, param3) -> param1.buildSurfaceAndBedrock(new WorldGenRegion(param0, param2), param3)
    );
    public static final ChunkStatus CARVERS = registerSimple(
        "carvers",
        SURFACE,
        0,
        PRE_FEATURES,
        ChunkStatus.ChunkType.PROTOCHUNK,
        (param0, param1, param2, param3) -> param1.applyCarvers(param0.getSeed(), param0.getBiomeManager(), param3, GenerationStep.Carving.AIR)
    );
    public static final ChunkStatus LIQUID_CARVERS = registerSimple(
        "liquid_carvers",
        CARVERS,
        0,
        POST_FEATURES,
        ChunkStatus.ChunkType.PROTOCHUNK,
        (param0, param1, param2, param3) -> param1.applyCarvers(param0.getSeed(), param0.getBiomeManager(), param3, GenerationStep.Carving.LIQUID)
    );
    public static final ChunkStatus FEATURES = register(
        "features",
        LIQUID_CARVERS,
        8,
        POST_FEATURES,
        ChunkStatus.ChunkType.PROTOCHUNK,
        (param0, param1, param2, param3, param4, param5, param6, param7) -> {
            ProtoChunk var0 = (ProtoChunk)param7;
            var0.setLightEngine(param4);
            if (!param7.getStatus().isOrAfter(param0)) {
                Heightmap.primeHeightmaps(
                    param7,
                    EnumSet.of(
                        Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE
                    )
                );
                param2.applyBiomeDecoration(new WorldGenRegion(param1, param6), param1.structureFeatureManager());
                var0.setStatus(param0);
            }
    
            return CompletableFuture.completedFuture(Either.left(param7));
        }
    );
    public static final ChunkStatus LIGHT = register(
        "light",
        FEATURES,
        1,
        POST_FEATURES,
        ChunkStatus.ChunkType.PROTOCHUNK,
        (param0, param1, param2, param3, param4, param5, param6, param7) -> lightChunk(param0, param4, param7),
        (param0, param1, param2, param3, param4, param5) -> lightChunk(param0, param3, param5)
    );
    public static final ChunkStatus SPAWN = registerSimple(
        "spawn",
        LIGHT,
        0,
        POST_FEATURES,
        ChunkStatus.ChunkType.PROTOCHUNK,
        (param0, param1, param2, param3) -> param1.spawnOriginalMobs(new WorldGenRegion(param0, param2))
    );
    public static final ChunkStatus HEIGHTMAPS = registerSimple(
        "heightmaps", SPAWN, 0, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (param0, param1, param2, param3) -> {
        }
    );
    public static final ChunkStatus FULL = register(
        "full",
        HEIGHTMAPS,
        0,
        POST_FEATURES,
        ChunkStatus.ChunkType.LEVELCHUNK,
        (param0, param1, param2, param3, param4, param5, param6, param7) -> param5.apply(param7),
        (param0, param1, param2, param3, param4, param5) -> param4.apply(param5)
    );
    private static final List<ChunkStatus> STATUS_BY_RANGE = ImmutableList.of(
        FULL,
        FEATURES,
        LIQUID_CARVERS,
        STRUCTURE_STARTS,
        STRUCTURE_STARTS,
        STRUCTURE_STARTS,
        STRUCTURE_STARTS,
        STRUCTURE_STARTS,
        STRUCTURE_STARTS,
        STRUCTURE_STARTS,
        STRUCTURE_STARTS
    );
    private static final IntList RANGE_BY_STATUS = Util.make(new IntArrayList(getStatusList().size()), param0 -> {
        int var0 = 0;

        for(int var1 = getStatusList().size() - 1; var1 >= 0; --var1) {
            while(var0 + 1 < STATUS_BY_RANGE.size() && var1 <= STATUS_BY_RANGE.get(var0 + 1).getIndex()) {
                ++var0;
            }

            param0.add(0, var0);
        }

    });
    private final String name;
    private final int index;
    private final ChunkStatus parent;
    private final ChunkStatus.GenerationTask generationTask;
    private final ChunkStatus.LoadingTask loadingTask;
    private final int range;
    private final ChunkStatus.ChunkType chunkType;
    private final EnumSet<Heightmap.Types> heightmapsAfter;

    private static CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> lightChunk(
        ChunkStatus param0, ThreadedLevelLightEngine param1, ChunkAccess param2
    ) {
        boolean var0 = isLighted(param0, param2);
        if (!param2.getStatus().isOrAfter(param0)) {
            ((ProtoChunk)param2).setStatus(param0);
        }

        return param1.lightChunk(param2, var0).thenApply(Either::left);
    }

    private static ChunkStatus registerSimple(
        String param0,
        @Nullable ChunkStatus param1,
        int param2,
        EnumSet<Heightmap.Types> param3,
        ChunkStatus.ChunkType param4,
        ChunkStatus.SimpleGenerationTask param5
    ) {
        return register(param0, param1, param2, param3, param4, param5);
    }

    private static ChunkStatus register(
        String param0,
        @Nullable ChunkStatus param1,
        int param2,
        EnumSet<Heightmap.Types> param3,
        ChunkStatus.ChunkType param4,
        ChunkStatus.GenerationTask param5
    ) {
        return register(param0, param1, param2, param3, param4, param5, PASSTHROUGH_LOAD_TASK);
    }

    private static ChunkStatus register(
        String param0,
        @Nullable ChunkStatus param1,
        int param2,
        EnumSet<Heightmap.Types> param3,
        ChunkStatus.ChunkType param4,
        ChunkStatus.GenerationTask param5,
        ChunkStatus.LoadingTask param6
    ) {
        return Registry.register(Registry.CHUNK_STATUS, param0, new ChunkStatus(param0, param1, param2, param3, param4, param5, param6));
    }

    public static List<ChunkStatus> getStatusList() {
        List<ChunkStatus> var0 = Lists.newArrayList();

        ChunkStatus var1;
        for(var1 = FULL; var1.getParent() != var1; var1 = var1.getParent()) {
            var0.add(var1);
        }

        var0.add(var1);
        Collections.reverse(var0);
        return var0;
    }

    private static boolean isLighted(ChunkStatus param0, ChunkAccess param1) {
        return param1.getStatus().isOrAfter(param0) && param1.isLightCorrect();
    }

    public static ChunkStatus getStatus(int param0) {
        if (param0 >= STATUS_BY_RANGE.size()) {
            return EMPTY;
        } else {
            return param0 < 0 ? FULL : STATUS_BY_RANGE.get(param0);
        }
    }

    public static int maxDistance() {
        return STATUS_BY_RANGE.size();
    }

    public static int getDistance(ChunkStatus param0) {
        return RANGE_BY_STATUS.getInt(param0.getIndex());
    }

    ChunkStatus(
        String param0,
        @Nullable ChunkStatus param1,
        int param2,
        EnumSet<Heightmap.Types> param3,
        ChunkStatus.ChunkType param4,
        ChunkStatus.GenerationTask param5,
        ChunkStatus.LoadingTask param6
    ) {
        this.name = param0;
        this.parent = param1 == null ? this : param1;
        this.generationTask = param5;
        this.loadingTask = param6;
        this.range = param2;
        this.chunkType = param4;
        this.heightmapsAfter = param3;
        this.index = param1 == null ? 0 : param1.getIndex() + 1;
    }

    public int getIndex() {
        return this.index;
    }

    public String getName() {
        return this.name;
    }

    public ChunkStatus getParent() {
        return this.parent;
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> generate(
        ServerLevel param0,
        ChunkGenerator param1,
        StructureManager param2,
        ThreadedLevelLightEngine param3,
        Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> param4,
        List<ChunkAccess> param5
    ) {
        return this.generationTask.doWork(this, param0, param1, param2, param3, param4, param5, param5.get(param5.size() / 2));
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> load(
        ServerLevel param0,
        StructureManager param1,
        ThreadedLevelLightEngine param2,
        Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> param3,
        ChunkAccess param4
    ) {
        return this.loadingTask.doWork(this, param0, param1, param2, param3, param4);
    }

    public int getRange() {
        return this.range;
    }

    public ChunkStatus.ChunkType getChunkType() {
        return this.chunkType;
    }

    public static ChunkStatus byName(String param0) {
        return Registry.CHUNK_STATUS.get(ResourceLocation.tryParse(param0));
    }

    public EnumSet<Heightmap.Types> heightmapsAfter() {
        return this.heightmapsAfter;
    }

    public boolean isOrAfter(ChunkStatus param0) {
        return this.getIndex() >= param0.getIndex();
    }

    @Override
    public String toString() {
        return Registry.CHUNK_STATUS.getKey(this).toString();
    }

    public static enum ChunkType {
        PROTOCHUNK,
        LEVELCHUNK;
    }

    interface GenerationTask {
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(
            ChunkStatus var1,
            ServerLevel var2,
            ChunkGenerator var3,
            StructureManager var4,
            ThreadedLevelLightEngine var5,
            Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> var6,
            List<ChunkAccess> var7,
            ChunkAccess var8
        );
    }

    interface LoadingTask {
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(
            ChunkStatus var1,
            ServerLevel var2,
            StructureManager var3,
            ThreadedLevelLightEngine var4,
            Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> var5,
            ChunkAccess var6
        );
    }

    interface SimpleGenerationTask extends ChunkStatus.GenerationTask {
        @Override
        default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(
            ChunkStatus param0,
            ServerLevel param1,
            ChunkGenerator param2,
            StructureManager param3,
            ThreadedLevelLightEngine param4,
            Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> param5,
            List<ChunkAccess> param6,
            ChunkAccess param7
        ) {
            if (!param7.getStatus().isOrAfter(param0)) {
                this.doWork(param1, param2, param6, param7);
                if (param7 instanceof ProtoChunk) {
                    ((ProtoChunk)param7).setStatus(param0);
                }
            }

            return CompletableFuture.completedFuture(Either.left(param7));
        }

        void doWork(ServerLevel var1, ChunkGenerator var2, List<ChunkAccess> var3, ChunkAccess var4);
    }
}
