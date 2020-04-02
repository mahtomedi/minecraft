package net.minecraft.world.entity.ai.village.poi;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.SectionStorage;

public class PoiManager extends SectionStorage<PoiSection> {
    private final PoiManager.DistanceTracker distanceTracker;
    private final LongSet loadedChunks = new LongOpenHashSet();

    public PoiManager(File param0, DataFixer param1, boolean param2) {
        super(param0, PoiSection::serialize, PoiSection::new, PoiSection::new, param1, DataFixTypes.POI_CHUNK, param2);
        this.distanceTracker = new PoiManager.DistanceTracker();
    }

    public void add(BlockPos param0, PoiType param1) {
        this.getOrCreate(SectionPos.of(param0).asLong()).add(param0, param1);
    }

    public void remove(BlockPos param0) {
        this.getOrCreate(SectionPos.of(param0).asLong()).remove(param0);
    }

    public long getCountInRange(Predicate<PoiType> param0, BlockPos param1, int param2, PoiManager.Occupancy param3) {
        return this.getInRange(param0, param1, param2, param3).count();
    }

    public boolean existsAtPosition(PoiType param0, BlockPos param1) {
        Optional<PoiType> var0 = this.getOrCreate(SectionPos.of(param1).asLong()).getType(param1);
        return var0.isPresent() && var0.get().equals(param0);
    }

    public Stream<PoiRecord> getInSquare(Predicate<PoiType> param0, BlockPos param1, int param2, PoiManager.Occupancy param3) {
        int var0 = Math.floorDiv(param2, 16) + 1;
        return ChunkPos.rangeClosed(new ChunkPos(param1), var0).flatMap(param2x -> this.getInChunk(param0, param2x, param3));
    }

    public Stream<PoiRecord> getInRange(Predicate<PoiType> param0, BlockPos param1, int param2, PoiManager.Occupancy param3) {
        int var0 = param2 * param2;
        return this.getInSquare(param0, param1, param2, param3).filter(param2x -> param2x.getPos().distSqr(param1) <= (double)var0);
    }

    public Stream<PoiRecord> getInChunk(Predicate<PoiType> param0, ChunkPos param1, PoiManager.Occupancy param2) {
        return IntStream.range(0, 16).boxed().flatMap(param3 -> this.getInSection(param0, SectionPos.of(param1, param3).asLong(), param2));
    }

    private Stream<PoiRecord> getInSection(Predicate<PoiType> param0, long param1, PoiManager.Occupancy param2) {
        return this.getOrLoad(param1).map(param2x -> param2x.getRecords(param0, param2)).orElseGet(Stream::empty);
    }

    public Stream<BlockPos> findAll(Predicate<PoiType> param0, Predicate<BlockPos> param1, BlockPos param2, int param3, PoiManager.Occupancy param4) {
        return this.getInRange(param0, param2, param3, param4).map(PoiRecord::getPos).filter(param1);
    }

    public Optional<BlockPos> find(Predicate<PoiType> param0, Predicate<BlockPos> param1, BlockPos param2, int param3, PoiManager.Occupancy param4) {
        return this.findAll(param0, param1, param2, param3, param4).findFirst();
    }

    public Optional<BlockPos> findClosest(Predicate<PoiType> param0, BlockPos param1, int param2, PoiManager.Occupancy param3) {
        return this.getInRange(param0, param1, param2, param3)
            .map(PoiRecord::getPos)
            .sorted(Comparator.comparingDouble(param1x -> param1x.distSqr(param1)))
            .findFirst();
    }

    public Optional<BlockPos> take(Predicate<PoiType> param0, Predicate<BlockPos> param1, BlockPos param2, int param3) {
        return this.getInRange(param0, param2, param3, PoiManager.Occupancy.HAS_SPACE)
            .filter(param1x -> param1.test(param1x.getPos()))
            .findFirst()
            .map(param0x -> {
                param0x.acquireTicket();
                return param0x.getPos();
            });
    }

    public Optional<BlockPos> getRandom(
        Predicate<PoiType> param0, Predicate<BlockPos> param1, PoiManager.Occupancy param2, BlockPos param3, int param4, Random param5
    ) {
        List<PoiRecord> var0 = this.getInRange(param0, param3, param4, param2).collect(Collectors.toList());
        Collections.shuffle(var0, param5);
        return var0.stream().filter(param1x -> param1.test(param1x.getPos())).findFirst().map(PoiRecord::getPos);
    }

    public boolean release(BlockPos param0) {
        return this.getOrCreate(SectionPos.of(param0).asLong()).release(param0);
    }

    public boolean exists(BlockPos param0, Predicate<PoiType> param1) {
        return this.getOrLoad(SectionPos.of(param0).asLong()).map(param2 -> param2.exists(param0, param1)).orElse(false);
    }

    public Optional<PoiType> getType(BlockPos param0) {
        PoiSection var0 = this.getOrCreate(SectionPos.of(param0).asLong());
        return var0.getType(param0);
    }

    public int sectionsToVillage(SectionPos param0) {
        this.distanceTracker.runAllUpdates();
        return this.distanceTracker.getLevel(param0.asLong());
    }

    private boolean isVillageCenter(long param0) {
        Optional<PoiSection> var0 = this.get(param0);
        return var0 == null
            ? false
            : var0.<Boolean>map(param0x -> param0x.getRecords(PoiType.ALL, PoiManager.Occupancy.IS_OCCUPIED).count() > 0L).orElse(false);
    }

    @Override
    public void tick(BooleanSupplier param0) {
        super.tick(param0);
        this.distanceTracker.runAllUpdates();
    }

    @Override
    protected void setDirty(long param0) {
        super.setDirty(param0);
        this.distanceTracker.update(param0, this.distanceTracker.getLevelFromSource(param0), false);
    }

    @Override
    protected void onSectionLoad(long param0) {
        this.distanceTracker.update(param0, this.distanceTracker.getLevelFromSource(param0), false);
    }

    public void checkConsistencyWithBlocks(ChunkPos param0, LevelChunkSection param1) {
        SectionPos var0 = SectionPos.of(param0, param1.bottomBlockY() >> 4);
        Util.ifElse(this.getOrLoad(var0.asLong()), param2 -> param2.refresh(param2x -> {
                if (mayHavePoi(param1)) {
                    this.updateFromSection(param1, var0, param2x);
                }

            }), () -> {
            if (mayHavePoi(param1)) {
                PoiSection var0x = this.getOrCreate(var0.asLong());
                this.updateFromSection(param1, var0, var0x::add);
            }

        });
    }

    private static boolean mayHavePoi(LevelChunkSection param0) {
        return PoiType.allPoiStates().anyMatch(param0::maybeHas);
    }

    private void updateFromSection(LevelChunkSection param0, SectionPos param1, BiConsumer<BlockPos, PoiType> param2) {
        param1.blocksInside()
            .forEach(
                param2x -> {
                    BlockState var0 = param0.getBlockState(
                        SectionPos.sectionRelative(param2x.getX()), SectionPos.sectionRelative(param2x.getY()), SectionPos.sectionRelative(param2x.getZ())
                    );
                    PoiType.forState(var0).ifPresent(param2xx -> param2.accept(param2x, param2xx));
                }
            );
    }

    public void ensureLoadedAndValid(LevelReader param0, BlockPos param1, int param2) {
        SectionPos.aroundChunk(new ChunkPos(param1), Math.floorDiv(param2, 16))
            .map(param0x -> Pair.of(param0x, this.getOrLoad(param0x.asLong())))
            .filter(param0x -> !param0x.getSecond().map(PoiSection::isValid).orElse(false))
            .map(param0x -> param0x.getFirst().chunk())
            .filter(param0x -> this.loadedChunks.add(param0x.toLong()))
            .forEach(param1x -> param0.getChunk(param1x.x, param1x.z, ChunkStatus.EMPTY));
    }

    final class DistanceTracker extends SectionTracker {
        private final Long2ByteMap levels = new Long2ByteOpenHashMap();

        protected DistanceTracker() {
            super(7, 16, 256);
            this.levels.defaultReturnValue((byte)7);
        }

        @Override
        protected int getLevelFromSource(long param0) {
            return PoiManager.this.isVillageCenter(param0) ? 0 : 7;
        }

        @Override
        protected int getLevel(long param0) {
            return this.levels.get(param0);
        }

        @Override
        protected void setLevel(long param0, int param1) {
            if (param1 > 6) {
                this.levels.remove(param0);
            } else {
                this.levels.put(param0, (byte)param1);
            }

        }

        public void runAllUpdates() {
            super.runUpdates(Integer.MAX_VALUE);
        }
    }

    public static enum Occupancy {
        HAS_SPACE(PoiRecord::hasSpace),
        IS_OCCUPIED(PoiRecord::isOccupied),
        ANY(param0 -> true);

        private final Predicate<? super PoiRecord> test;

        private Occupancy(Predicate<? super PoiRecord> param0) {
            this.test = param0;
        }

        public Predicate<? super PoiRecord> getTest() {
            return this.test;
        }
    }
}
