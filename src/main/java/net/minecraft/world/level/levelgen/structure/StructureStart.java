package net.minecraft.world.level.levelgen.structure;

import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import org.slf4j.Logger;

public final class StructureStart {
    public static final String INVALID_START_ID = "INVALID";
    public static final StructureStart INVALID_START = new StructureStart(null, new ChunkPos(0, 0), 0, new PiecesContainer(List.of()));
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Structure structure;
    private final PiecesContainer pieceContainer;
    private final ChunkPos chunkPos;
    private int references;
    @Nullable
    private volatile BoundingBox cachedBoundingBox;

    public StructureStart(Structure param0, ChunkPos param1, int param2, PiecesContainer param3) {
        this.structure = param0;
        this.chunkPos = param1;
        this.references = param2;
        this.pieceContainer = param3;
    }

    @Nullable
    public static StructureStart loadStaticStart(StructurePieceSerializationContext param0, CompoundTag param1, long param2) {
        String var0 = param1.getString("id");
        if ("INVALID".equals(var0)) {
            return INVALID_START;
        } else {
            Registry<Structure> var1 = param0.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
            Structure var2 = var1.get(new ResourceLocation(var0));
            if (var2 == null) {
                LOGGER.error("Unknown stucture id: {}", var0);
                return null;
            } else {
                ChunkPos var3 = new ChunkPos(param1.getInt("ChunkX"), param1.getInt("ChunkZ"));
                int var4 = param1.getInt("references");
                ListTag var5 = param1.getList("Children", 10);

                try {
                    PiecesContainer var6 = PiecesContainer.load(var5, param0);
                    if (var2 instanceof OceanMonumentStructure) {
                        var6 = OceanMonumentStructure.regeneratePiecesAfterLoad(var3, param2, var6);
                    }

                    return new StructureStart(var2, var3, var4, var6);
                } catch (Exception var11) {
                    LOGGER.error("Failed Start with id {}", var0, var11);
                    return null;
                }
            }
        }
    }

    public BoundingBox getBoundingBox() {
        BoundingBox var0 = this.cachedBoundingBox;
        if (var0 == null) {
            var0 = this.structure.adjustBoundingBox(this.pieceContainer.calculateBoundingBox());
            this.cachedBoundingBox = var0;
        }

        return var0;
    }

    public void placeInChunk(WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5) {
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

            this.structure.afterPlace(param0, param1, param2, param3, param4, param5, this.pieceContainer);
        }
    }

    public CompoundTag createTag(StructurePieceSerializationContext param0, ChunkPos param1) {
        CompoundTag var0 = new CompoundTag();
        if (this.isValid()) {
            var0.putString("id", param0.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY).getKey(this.structure).toString());
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

    public Structure getStructure() {
        return this.structure;
    }

    public List<StructurePiece> getPieces() {
        return this.pieceContainer.pieces();
    }
}
