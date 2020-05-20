package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public abstract class StructureStart<C extends FeatureConfiguration> {
    public static final StructureStart<?> INVALID_START = new StructureStart<MineshaftConfiguration>(
        StructureFeature.MINESHAFT, 0, 0, BoundingBox.getUnknownBox(), 0, 0L
    ) {
        public void generatePieces(ChunkGenerator param0, StructureManager param1, int param2, int param3, Biome param4, MineshaftConfiguration param5) {
        }
    };
    private final StructureFeature<C> feature;
    protected final List<StructurePiece> pieces = Lists.newArrayList();
    protected BoundingBox boundingBox;
    private final int chunkX;
    private final int chunkZ;
    private int references;
    protected final WorldgenRandom random;

    public StructureStart(StructureFeature<C> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
        this.feature = param0;
        this.chunkX = param1;
        this.chunkZ = param2;
        this.references = param4;
        this.random = new WorldgenRandom();
        this.random.setLargeFeatureSeed(param5, param1, param2);
        this.boundingBox = param3;
    }

    public abstract void generatePieces(ChunkGenerator var1, StructureManager var2, int var3, int var4, Biome var5, C var6);

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public List<StructurePiece> getPieces() {
        return this.pieces;
    }

    public void placeInChunk(WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5) {
        synchronized(this.pieces) {
            if (!this.pieces.isEmpty()) {
                BoundingBox var0 = this.pieces.get(0).boundingBox;
                Vec3i var1 = var0.getCenter();
                BlockPos var2 = new BlockPos(var1.getX(), var0.y0, var1.getZ());
                Iterator<StructurePiece> var3 = this.pieces.iterator();

                while(var3.hasNext()) {
                    StructurePiece var4 = var3.next();
                    if (var4.getBoundingBox().intersects(param4) && !var4.postProcess(param0, param1, param2, param3, param4, param5, var2)) {
                        var3.remove();
                    }
                }

                this.calculateBoundingBox();
            }
        }
    }

    protected void calculateBoundingBox() {
        this.boundingBox = BoundingBox.getUnknownBox();

        for(StructurePiece var0 : this.pieces) {
            this.boundingBox.expand(var0.getBoundingBox());
        }

    }

    public CompoundTag createTag(int param0, int param1) {
        CompoundTag var0 = new CompoundTag();
        if (this.isValid()) {
            var0.putString("id", Registry.STRUCTURE_FEATURE.getKey(this.getFeature()).toString());
            var0.putInt("ChunkX", param0);
            var0.putInt("ChunkZ", param1);
            var0.putInt("references", this.references);
            var0.put("BB", this.boundingBox.createTag());
            ListTag var1 = new ListTag();
            synchronized(this.pieces) {
                for(StructurePiece var2 : this.pieces) {
                    var1.add(var2.createTag());
                }
            }

            var0.put("Children", var1);
            return var0;
        } else {
            var0.putString("id", "INVALID");
            return var0;
        }
    }

    protected void moveBelowSeaLevel(int param0, Random param1, int param2) {
        int var0 = param0 - param2;
        int var1 = this.boundingBox.getYSpan() + 1;
        if (var1 < var0) {
            var1 += param1.nextInt(var0 - var1);
        }

        int var2 = var1 - this.boundingBox.y1;
        this.boundingBox.move(0, var2, 0);

        for(StructurePiece var3 : this.pieces) {
            var3.move(0, var2, 0);
        }

    }

    protected void moveInsideHeights(Random param0, int param1, int param2) {
        int var0 = param2 - param1 + 1 - this.boundingBox.getYSpan();
        int var1;
        if (var0 > 1) {
            var1 = param1 + param0.nextInt(var0);
        } else {
            var1 = param1;
        }

        int var3 = var1 - this.boundingBox.y0;
        this.boundingBox.move(0, var3, 0);

        for(StructurePiece var4 : this.pieces) {
            var4.move(0, var3, 0);
        }

    }

    public boolean isValid() {
        return !this.pieces.isEmpty();
    }

    public int getChunkX() {
        return this.chunkX;
    }

    public int getChunkZ() {
        return this.chunkZ;
    }

    public BlockPos getLocatePos() {
        return new BlockPos(this.chunkX << 4, 0, this.chunkZ << 4);
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
}
