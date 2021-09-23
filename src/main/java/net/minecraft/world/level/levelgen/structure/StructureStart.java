package net.minecraft.world.level.levelgen.structure;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public final class StructureStart<C extends FeatureConfiguration> {
    public static final String INVALID_START_ID = "INVALID";
    public static final StructureStart<?> INVALID_START = new StructureStart(null, new ChunkPos(0, 0), 0, new PiecesContainer(List.of()));
    private final StructureFeature<C> feature;
    private final PiecesContainer pieceContainer;
    private final ChunkPos chunkPos;
    private int references;
    @Nullable
    private volatile BoundingBox cachedBoundingBox;

    public StructureStart(StructureFeature<C> param0, ChunkPos param1, int param2, PiecesContainer param3) {
        this.feature = param0;
        this.chunkPos = param1;
        this.references = param2;
        this.pieceContainer = param3;
    }

    public BoundingBox getBoundingBox() {
        BoundingBox var0 = this.cachedBoundingBox;
        if (var0 == null) {
            var0 = this.feature.adjustBoundingBox(this.pieceContainer.calculateBoundingBox());
            this.cachedBoundingBox = var0;
        }

        return var0;
    }

    public void placeInChunk(WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5) {
        List<StructurePiece> var0 = this.pieceContainer.pieces();
        if (!var0.isEmpty()) {
            BoundingBox var1 = var0.get(0).boundingBox;
            BlockPos var2 = var1.getCenter();
            BlockPos var3 = new BlockPos(var2.getX(), var1.minY(), var2.getZ());

            for(StructurePiece var4 : var0) {
                if (var4.getBoundingBox().intersects(param4)) {
                    var4.postProcess(param0, param1, param2, param3, param4, param5, var3);
                }
            }

            this.feature.getPostPlacementProcessor().afterPlace(param0, param1, param2, param3, param4, param5, this.pieceContainer);
        }
    }

    public CompoundTag createTag(StructurePieceSerializationContext param0, ChunkPos param1) {
        CompoundTag var0 = new CompoundTag();
        if (this.isValid()) {
            var0.putString("id", Registry.STRUCTURE_FEATURE.getKey(this.getFeature()).toString());
            var0.putInt("ChunkX", param1.x);
            var0.putInt("ChunkZ", param1.z);
            var0.putInt("references", this.references);
            var0.put("Children", this.pieceContainer.save(param0));
            return var0;
        } else {
            var0.putString("id", "INVALID");
            return var0;
        }
    }

    public boolean isValid() {
        return !this.pieceContainer.isEmpty();
    }

    public ChunkPos getChunkPos() {
        return this.chunkPos;
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

    public List<StructurePiece> getPieces() {
        return this.pieceContainer.pieces();
    }
}
