package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StructureStart<C extends FeatureConfiguration> implements StructurePieceAccessor {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String INVALID_START_ID = "INVALID";
    public static final StructureStart<?> INVALID_START = new StructureStart<MineshaftConfiguration>(null, new ChunkPos(0, 0), 0, 0L) {
        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            Biome param4,
            MineshaftConfiguration param5,
            LevelHeightAccessor param6
        ) {
        }

        @Override
        public boolean isValid() {
            return false;
        }
    };
    private final StructureFeature<C> feature;
    protected final List<StructurePiece> pieces = Lists.newArrayList();
    private final ChunkPos chunkPos;
    private int references;
    protected final WorldgenRandom random;
    @Nullable
    private BoundingBox cachedBoundingBox;

    public StructureStart(StructureFeature<C> param0, ChunkPos param1, int param2, long param3) {
        this.feature = param0;
        this.chunkPos = param1;
        this.references = param2;
        this.random = new WorldgenRandom();
        this.random.setLargeFeatureSeed(param3, param1.x, param1.z);
    }

    public abstract void generatePieces(
        RegistryAccess var1, ChunkGenerator var2, StructureManager var3, ChunkPos var4, Biome var5, C var6, LevelHeightAccessor var7
    );

    public BoundingBox getBoundingBox() {
        if (this.cachedBoundingBox == null) {
            synchronized(this.pieces) {
                this.cachedBoundingBox = BoundingBox.encapsulatingBoxes(this.pieces.stream().map(StructurePiece::getBoundingBox)::iterator)
                    .orElseThrow(() -> new IllegalStateException("Unable to calculate boundingbox without pieces"));
            }
        }

        return this.cachedBoundingBox;
    }

    public List<StructurePiece> getPieces() {
        return this.pieces;
    }

    public void placeInChunk(WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5) {
        synchronized(this.pieces) {
            if (!this.pieces.isEmpty()) {
                BoundingBox var0 = this.pieces.get(0).boundingBox;
                BlockPos var1 = var0.getCenter();
                BlockPos var2 = new BlockPos(var1.getX(), var0.minY(), var1.getZ());
                Iterator<StructurePiece> var3 = this.pieces.iterator();

                while(var3.hasNext()) {
                    StructurePiece var4 = var3.next();
                    if (var4.getBoundingBox().intersects(param4) && !var4.postProcess(param0, param1, param2, param3, param4, param5, var2)) {
                        var3.remove();
                    }
                }

            }
        }
    }

    public CompoundTag createTag(ServerLevel param0, ChunkPos param1) {
        CompoundTag var0 = new CompoundTag();
        if (this.isValid()) {
            var0.putString("id", Registry.STRUCTURE_FEATURE.getKey(this.getFeature()).toString());
            var0.putInt("ChunkX", param1.x);
            var0.putInt("ChunkZ", param1.z);
            var0.putInt("references", this.references);
            ListTag var1 = new ListTag();
            synchronized(this.pieces) {
                for(StructurePiece var2 : this.pieces) {
                    var1.add(var2.createTag(param0));
                }
            }

            var0.put("Children", var1);
            return var0;
        } else {
            var0.putString("id", "INVALID");
            return var0;
        }
    }

    protected void moveBelowSeaLevel(int param0, int param1, Random param2, int param3) {
        int var0 = param0 - param3;
        BoundingBox var1 = this.getBoundingBox();
        int var2 = var1.getYSpan() + param1 + 1;
        if (var2 < var0) {
            var2 += param2.nextInt(var0 - var2);
        }

        int var3 = var2 - var1.maxY();
        this.offsetPiecesVertically(var3);
    }

    protected void moveInsideHeights(Random param0, int param1, int param2) {
        BoundingBox var0 = this.getBoundingBox();
        int var1 = param2 - param1 + 1 - var0.getYSpan();
        int var2;
        if (var1 > 1) {
            var2 = param1 + param0.nextInt(var1);
        } else {
            var2 = param1;
        }

        int var4 = var2 - var0.minY();
        this.offsetPiecesVertically(var4);
    }

    protected void offsetPiecesVertically(int param0) {
        for(StructurePiece var0 : this.pieces) {
            var0.move(0, param0, 0);
        }

        this.invalidateCache();
    }

    private void invalidateCache() {
        this.cachedBoundingBox = null;
    }

    public boolean isValid() {
        return !this.pieces.isEmpty();
    }

    public ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    public BlockPos getLocatePos() {
        return new BlockPos(this.chunkPos.getMinBlockX(), 0, this.chunkPos.getMinBlockZ());
    }

    public boolean canBeReferenced() {
        return this.references < this.getMaxReferences();
    }

    public void addReference() {
        ++this.references;
    }

    public int getReferences() {
        return this.references;
    }

    protected int getMaxReferences() {
        return 1;
    }

    public StructureFeature<?> getFeature() {
        return this.feature;
    }

    @Override
    public void addPiece(StructurePiece param0) {
        this.pieces.add(param0);
        this.invalidateCache();
    }

    @Nullable
    @Override
    public StructurePiece findCollisionPiece(BoundingBox param0) {
        return findCollisionPiece(this.pieces, param0);
    }

    public void clearPieces() {
        this.pieces.clear();
        this.invalidateCache();
    }

    public boolean hasNoPieces() {
        return this.pieces.isEmpty();
    }

    @Nullable
    public static StructurePiece findCollisionPiece(List<StructurePiece> param0, BoundingBox param1) {
        for(StructurePiece var0 : param0) {
            if (var0.getBoundingBox().intersects(param1)) {
                return var0;
            }
        }

        return null;
    }

    protected boolean isInsidePiece(BlockPos param0) {
        for(StructurePiece var0 : this.pieces) {
            if (var0.getBoundingBox().isInside(param0)) {
                return true;
            }
        }

        return false;
    }
}
