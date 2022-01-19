package net.minecraft.world.level.levelgen.structure.pieces;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.slf4j.Logger;

public record PiecesContainer(List<StructurePiece> pieces) {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation JIGSAW_RENAME = new ResourceLocation("jigsaw");
    private static final Map<ResourceLocation, ResourceLocation> RENAMES = ImmutableMap.<ResourceLocation, ResourceLocation>builder()
        .put(new ResourceLocation("nvi"), JIGSAW_RENAME)
        .put(new ResourceLocation("pcp"), JIGSAW_RENAME)
        .put(new ResourceLocation("bastionremnant"), JIGSAW_RENAME)
        .put(new ResourceLocation("runtime"), JIGSAW_RENAME)
        .build();

    public PiecesContainer(List<StructurePiece> param0) {
        this.pieces = List.copyOf(param0);
    }

    public boolean isEmpty() {
        return this.pieces.isEmpty();
    }

    public boolean isInsidePiece(BlockPos param0) {
        for(StructurePiece var0 : this.pieces) {
            if (var0.getBoundingBox().isInside(param0)) {
                return true;
            }
        }

        return false;
    }

    public Tag save(StructurePieceSerializationContext param0) {
        ListTag var0 = new ListTag();

        for(StructurePiece var1 : this.pieces) {
            var0.add(var1.createTag(param0));
        }

        return var0;
    }

    public static PiecesContainer load(ListTag param0, StructurePieceSerializationContext param1) {
        List<StructurePiece> var0 = Lists.newArrayList();

        for(int var1 = 0; var1 < param0.size(); ++var1) {
            CompoundTag var2 = param0.getCompound(var1);
            String var3 = var2.getString("id").toLowerCase(Locale.ROOT);
            ResourceLocation var4 = new ResourceLocation(var3);
            ResourceLocation var5 = RENAMES.getOrDefault(var4, var4);
            StructurePieceType var6 = Registry.STRUCTURE_PIECE.get(var5);
            if (var6 == null) {
                LOGGER.error("Unknown structure piece id: {}", var5);
            } else {
                try {
                    StructurePiece var7 = var6.load(param1, var2);
                    var0.add(var7);
                } catch (Exception var10) {
                    LOGGER.error("Exception loading structure piece with id {}", var5, var10);
                }
            }
        }

        return new PiecesContainer(var0);
    }

    public BoundingBox calculateBoundingBox() {
        return StructurePiece.createBoundingBox(this.pieces.stream());
    }
}
