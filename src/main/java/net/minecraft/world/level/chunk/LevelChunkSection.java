package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class LevelChunkSection {
    public static final int SECTION_WIDTH = 16;
    public static final int SECTION_HEIGHT = 16;
    public static final int SECTION_SIZE = 4096;
    public static final int BIOME_CONTAINER_BITS = 2;
    private final int bottomBlockY;
    private short nonEmptyBlockCount;
    private short tickingBlockCount;
    private short tickingFluidCount;
    private final PalettedContainer<BlockState> states;
    private final PalettedContainer<Biome> biomes;

    public LevelChunkSection(int param0, PalettedContainer<BlockState> param1, PalettedContainer<Biome> param2) {
        this.bottomBlockY = getBottomBlockY(param0);
        this.states = param1;
        this.biomes = param2;
        this.recalcBlockCounts();
    }

    public LevelChunkSection(int param0, Registry<Biome> param1) {
        this.bottomBlockY = getBottomBlockY(param0);
        this.states = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
        this.biomes = new PalettedContainer<>(param1, param1.getOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
    }

    public static int getBottomBlockY(int param0) {
        return param0 << 4;
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

    public boolean hasOnlyAir() {
        return this.nonEmptyBlockCount == 0;
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

    public PalettedContainer<Biome> getBiomes() {
        return this.biomes;
    }

    public void read(FriendlyByteBuf param0) {
        this.nonEmptyBlockCount = param0.readShort();
        this.states.read(param0);
        this.biomes.read(param0);
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeShort(this.nonEmptyBlockCount);
        this.states.write(param0);
        this.biomes.write(param0);
    }

    public int getSerializedSize() {
        return 2 + this.states.getSerializedSize() + this.biomes.getSerializedSize();
    }

    public boolean maybeHas(Predicate<BlockState> param0) {
        return this.states.maybeHas(param0);
    }

    public Biome getNoiseBiome(int param0, int param1, int param2) {
        return this.biomes.get(param0, param1, param2);
    }

    public void fillBiomesFromNoise(BiomeSource param0, Climate.Sampler param1, int param2, int param3) {
        PalettedContainer<Biome> var0 = this.getBiomes();
        var0.acquire();

        try {
            int var1 = QuartPos.fromBlock(this.bottomBlockY());
            int var2 = 4;

            for(int var3 = 0; var3 < 4; ++var3) {
                for(int var4 = 0; var4 < 4; ++var4) {
                    for(int var5 = 0; var5 < 4; ++var5) {
                        var0.getAndSetUnchecked(var3, var4, var5, param0.getNoiseBiome(param2 + var3, var1 + var4, param3 + var5, param1));
                    }
                }
            }
        } finally {
            var0.release();
        }

    }
}
