package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LevelChunkSection {
    private static final Palette<BlockState> GLOBAL_BLOCKSTATE_PALETTE = new GlobalPalette<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState());
    private final int bottomBlockY;
    private short nonEmptyBlockCount;
    private short tickingBlockCount;
    private short tickingFluidCount;
    private final PalettedContainer<BlockState> states;

    public LevelChunkSection(int param0) {
        this(param0, (short)0, (short)0, (short)0);
    }

    public LevelChunkSection(int param0, short param1, short param2, short param3) {
        this.bottomBlockY = param0;
        this.nonEmptyBlockCount = param1;
        this.tickingBlockCount = param2;
        this.tickingFluidCount = param3;
        this.states = new PalettedContainer<>(
            GLOBAL_BLOCKSTATE_PALETTE, Block.BLOCK_STATE_REGISTRY, NbtUtils::readBlockState, NbtUtils::writeBlockState, Blocks.AIR.defaultBlockState()
        );
    }

    public BlockState getBlockState(int param0, int param1, int param2) {
        return this.states.get(param0, param1, param2);
    }

    public FluidState getFluidState(int param0, int param1, int param2) {
        return this.states.get(param0, param1, param2).getFluidState();
    }

    public void acquire() {
        this.states.acquire();
    }

    public void release() {
        this.states.release();
    }

    public BlockState setBlockState(int param0, int param1, int param2, BlockState param3) {
        return this.setBlockState(param0, param1, param2, param3, true);
    }

    public BlockState setBlockState(int param0, int param1, int param2, BlockState param3, boolean param4) {
        BlockState var0;
        if (param4) {
            var0 = this.states.getAndSet(param0, param1, param2, param3);
        } else {
            var0 = this.states.getAndSetUnchecked(param0, param1, param2, param3);
        }

        FluidState var2 = var0.getFluidState();
        FluidState var3 = param3.getFluidState();
        if (!var0.isAir()) {
            --this.nonEmptyBlockCount;
            if (var0.isRandomlyTicking()) {
                --this.tickingBlockCount;
            }
        }

        if (!var2.isEmpty()) {
            --this.tickingFluidCount;
        }

        if (!param3.isAir()) {
            ++this.nonEmptyBlockCount;
            if (param3.isRandomlyTicking()) {
                ++this.tickingBlockCount;
            }
        }

        if (!var3.isEmpty()) {
            ++this.tickingFluidCount;
        }

        return var0;
    }

    public boolean isEmpty() {
        return this.nonEmptyBlockCount == 0;
    }

    public static boolean isEmpty(@Nullable LevelChunkSection param0) {
        return param0 == LevelChunk.EMPTY_SECTION || param0.isEmpty();
    }

    public boolean isRandomlyTicking() {
        return this.isRandomlyTickingBlocks() || this.isRandomlyTickingFluids();
    }

    public boolean isRandomlyTickingBlocks() {
        return this.tickingBlockCount > 0;
    }

    public boolean isRandomlyTickingFluids() {
        return this.tickingFluidCount > 0;
    }

    public int bottomBlockY() {
        return this.bottomBlockY;
    }

    public void recalcBlockCounts() {
        this.nonEmptyBlockCount = 0;
        this.tickingBlockCount = 0;
        this.tickingFluidCount = 0;
        this.states.count((param0, param1) -> {
            FluidState var0 = param0.getFluidState();
            if (!param0.isAir()) {
                this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + param1);
                if (param0.isRandomlyTicking()) {
                    this.tickingBlockCount = (short)(this.tickingBlockCount + param1);
                }
            }

            if (!var0.isEmpty()) {
                this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + param1);
                if (var0.isRandomlyTicking()) {
                    this.tickingFluidCount = (short)(this.tickingFluidCount + param1);
                }
            }

        });
    }

    public PalettedContainer<BlockState> getStates() {
        return this.states;
    }

    @OnlyIn(Dist.CLIENT)
    public void read(FriendlyByteBuf param0) {
        this.nonEmptyBlockCount = param0.readShort();
        this.states.read(param0);
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeShort(this.nonEmptyBlockCount);
        this.states.write(param0);
    }

    public int getSerializedSize() {
        return 2 + this.states.getSerializedSize();
    }

    public boolean maybeHas(BlockState param0) {
        return this.states.maybeHas(param0);
    }
}
