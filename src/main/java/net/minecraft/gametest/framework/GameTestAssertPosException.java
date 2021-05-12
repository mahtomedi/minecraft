package net.minecraft.gametest.framework;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public class GameTestAssertPosException extends GameTestAssertException {
    private final BlockPos absolutePos;
    private final BlockPos relativePos;
    private final long tick;

    public GameTestAssertPosException(String param0, BlockPos param1, BlockPos param2, long param3) {
        super(param0);
        this.absolutePos = param1;
        this.relativePos = param2;
        this.tick = param3;
    }

    @Override
    public String getMessage() {
        String var0 = this.absolutePos.getX()
            + ","
            + this.absolutePos.getY()
            + ","
            + this.absolutePos.getZ()
            + " (relative: "
            + this.relativePos.getX()
            + ","
            + this.relativePos.getY()
            + ","
            + this.relativePos.getZ()
            + ")";
        return super.getMessage() + " at " + var0 + " (t=" + this.tick + ")";
    }

    @Nullable
    public String getMessageToShowAtBlock() {
        return super.getMessage();
    }

    @Nullable
    public BlockPos getRelativePos() {
        return this.relativePos;
    }

    @Nullable
    public BlockPos getAbsolutePos() {
        return this.absolutePos;
    }
}
