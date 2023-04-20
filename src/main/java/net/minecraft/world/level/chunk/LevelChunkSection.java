package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
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
    private short nonEmptyBlockCount;
    private short tickingBlockCount;
    private short tickingFluidCount;
    private final PalettedContainer<BlockState> states;
    private PalettedContainerRO<Holder<Biome>> biomes;

    public LevelChunkSection(PalettedContainer<BlockState> param0, PalettedContainerRO<Holder<Biome>> param1) {
        this.states = param0;
        this.biomes = param1;
        this.recalcBlockCounts();
    }

    public LevelChunkSection(Registry<Biome> param0) {
        this.states = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
        this.biomes = new PalettedContainer<>(param0.asHolderIdMap(), param0.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
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

    public void recalcBlockCounts() {
        class BlockCounter implements PalettedContainer.CountConsumer<BlockState> {
            public int nonEmptyBlockCount;
            public int tickingBlockCount;
            public int tickingFluidCount;

            public void accept(BlockState param0, int param1) {
                FluidState var0 = param0.getFluidState();
                if (!param0.isAir()) {
                    this.nonEmptyBlockCount += param1;
                    if (param0.isRandomlyTicking()) {
                        this.tickingBlockCount += param1;
                    }
                }

                if (!var0.isEmpty()) {
                    this.nonEmptyBlockCount += param1;
                    if (var0.isRandomlyTicking()) {
                        this.tickingFluidCount += param1;
                    }
                }

            }
        }

        BlockCounter var0 = new BlockCounter();
        this.states.count(var0);
        this.nonEmptyBlockCount = (short)var0.nonEmptyBlockCount;
        this.tickingBlockCount = (short)var0.tickingBlockCount;
        this.tickingFluidCount = (short)var0.tickingFluidCount;
    }

    public PalettedContainer<BlockState> getStates() {
        return this.states;
    }

    public PalettedContainerRO<Holder<Biome>> getBiomes() {
        return this.biomes;
    }

    public void read(FriendlyByteBuf param0) {
        this.nonEmptyBlockCount = param0.readShort();
        this.states.read(param0);
        PalettedContainer<Holder<Biome>> var0 = this.biomes.recreate();
        var0.read(param0);
        this.biomes = var0;
    }

    public void readBiomes(FriendlyByteBuf param0) {
        PalettedContainer<Holder<Biome>> var0 = this.biomes.recreate();
        var0.read(param0);
        this.biomes = var0;
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

    public Holder<Biome> getNoiseBiome(int param0, int param1, int param2) {
        return this.biomes.get(param0, param1, param2);
    }

    public void fillBiomesFromNoise(BiomeResolver param0, Climate.Sampler param1, int param2, int param3, int param4) {
        PalettedContainer<Holder<Biome>> var0 = this.biomes.recreate();
        int var1 = 4;

        for(int var2 = 0; var2 < 4; ++var2) {
            for(int var3 = 0; var3 < 4; ++var3) {
                for(int var4 = 0; var4 < 4; ++var4) {
                    var0.getAndSetUnchecked(var2, var3, var4, param0.getNoiseBiome(param2 + var2, param3 + var3, param4 + var4, param1));
                }
            }
        }

        this.biomes = var0;
    }
}
