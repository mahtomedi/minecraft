package net.minecraft.world.level.levelgen.structure.pieces;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;

public class StructurePiecesBuilder implements StructurePieceAccessor {
    private final List<StructurePiece> pieces = Lists.newArrayList();

    @Override
    public void addPiece(StructurePiece param0) {
        this.pieces.add(param0);
    }

    @Nullable
    @Override
    public StructurePiece findCollisionPiece(BoundingBox param0) {
        return StructurePiece.findCollisionPiece(this.pieces, param0);
    }

    @Deprecated
    public void offsetPiecesVertically(int param0) {
        for(StructurePiece var0 : this.pieces) {
            var0.move(0, param0, 0);
        }

    }

    @Deprecated
    public void moveBelowSeaLevel(int param0, int param1, Random param2, int param3) {
        int var0 = param0 - param3;
        BoundingBox var1 = this.getBoundingBox();
        int var2 = var1.getYSpan() + param1 + 1;
        if (var2 < var0) {
            var2 += param2.nextInt(var0 - var2);
        }

        int var3 = var2 - var1.maxY();
        this.offsetPiecesVertically(var3);
    }

    public void moveInsideHeights(Random param0, int param1, int param2) {
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

    public PiecesContainer build() {
        return new PiecesContainer(this.pieces);
    }

    public void clear() {
        this.pieces.clear();
    }

    public boolean isEmpty() {
        return this.pieces.isEmpty();
    }

    public BoundingBox getBoundingBox() {
        return StructurePiece.createBoundingBox(this.pieces.stream());
    }
}
