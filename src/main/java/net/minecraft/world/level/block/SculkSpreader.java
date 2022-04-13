package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class SculkSpreader {
    public static final int MAX_GROWTH_RATE_RADIUS = 24;
    public static final int MAX_CHARGE = 1000;
    public static final float MAX_DECAY_FACTOR = 0.5F;
    private static final int MAX_CURSORS = 32;
    public static final int SHRIEKER_PLACEMENT_RATE = 11;
    final boolean isWorldGeneration;
    private final TagKey<Block> replaceableBlocks;
    private final int growthSpawnCost;
    private final int noGrowthRadius;
    private final int chargeDecayRate;
    private final int additionalDecayRate;
    private List<SculkSpreader.ChargeCursor> cursors = new ArrayList<>();
    private static final Logger LOGGER = LogUtils.getLogger();

    public SculkSpreader(boolean param0, TagKey<Block> param1, int param2, int param3, int param4, int param5) {
        this.isWorldGeneration = param0;
        this.replaceableBlocks = param1;
        this.growthSpawnCost = param2;
        this.noGrowthRadius = param3;
        this.chargeDecayRate = param4;
        this.additionalDecayRate = param5;
    }

    public static SculkSpreader createLevelSpreader() {
        return new SculkSpreader(false, BlockTags.SCULK_REPLACEABLE, 10, 4, 10, 5);
    }

    public static SculkSpreader createWorldGenSpreader() {
        return new SculkSpreader(true, BlockTags.SCULK_REPLACEABLE_WORLD_GEN, 50, 1, 5, 10);
    }

    public TagKey<Block> replaceableBlocks() {
        return this.replaceableBlocks;
    }

    public int growthSpawnCost() {
        return this.growthSpawnCost;
    }

    public int noGrowthRadius() {
        return this.noGrowthRadius;
    }

    public int chargeDecayRate() {
        return this.chargeDecayRate;
    }

    public int additionalDecayRate() {
        return this.additionalDecayRate;
    }

    public boolean isWorldGeneration() {
        return this.isWorldGeneration;
    }

    @VisibleForTesting
    public List<SculkSpreader.ChargeCursor> getCursors() {
        return this.cursors;
    }

    public void clear() {
        this.cursors.clear();
    }

    public void load(CompoundTag param0) {
        if (param0.contains("cursors", 9)) {
            this.cursors.clear();
            List<SculkSpreader.ChargeCursor> var0 = SculkSpreader.ChargeCursor.CODEC
                .listOf()
                .parse(new Dynamic<>(NbtOps.INSTANCE, param0.getList("cursors", 10)))
                .resultOrPartial(LOGGER::error)
                .orElseGet(ArrayList::new);
            int var1 = Math.min(var0.size(), 32);

            for(int var2 = 0; var2 < var1; ++var2) {
                this.addCursor(var0.get(var2));
            }
        }

    }

    public void save(CompoundTag param0) {
        SculkSpreader.ChargeCursor.CODEC
            .listOf()
            .encodeStart(NbtOps.INSTANCE, this.cursors)
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("cursors", param1));
    }

    public void addCursors(BlockPos param0, int param1) {
        while(param1 > 0) {
            int var0 = Math.min(param1, 1000);
            this.addCursor(new SculkSpreader.ChargeCursor(param0, var0));
            param1 -= var0;
        }

    }

    private void addCursor(SculkSpreader.ChargeCursor param0) {
        if (this.cursors.size() < 32) {
            this.cursors.add(param0);
        }
    }

    public void updateCursors(LevelAccessor param0, BlockPos param1, RandomSource param2, boolean param3) {
        if (!this.cursors.isEmpty()) {
            List<SculkSpreader.ChargeCursor> var0 = new ArrayList<>();
            Map<BlockPos, SculkSpreader.ChargeCursor> var1 = new HashMap<>();
            Object2IntMap<BlockPos> var2 = new Object2IntOpenHashMap<>();

            for(SculkSpreader.ChargeCursor var3 : this.cursors) {
                var3.update(param0, param1, param2, this, param3);
                if (var3.charge <= 0) {
                    param0.levelEvent(3006, var3.getPos(), 0);
                } else {
                    BlockPos var4 = var3.getPos();
                    var2.computeInt(var4, (param1x, param2x) -> (param2x == null ? 0 : param2x) + var3.charge);
                    SculkSpreader.ChargeCursor var5 = var1.get(var4);
                    if (var5 == null) {
                        var1.put(var4, var3);
                        var0.add(var3);
                    } else if (!this.isWorldGeneration() && var3.charge + var5.charge <= 1000) {
                        var5.mergeWith(var3);
                    } else {
                        var0.add(var3);
                        if (var3.charge < var5.charge) {
                            var1.put(var4, var3);
                        }
                    }
                }
            }

            for(Entry<BlockPos> var6 : var2.object2IntEntrySet()) {
                BlockPos var7 = var6.getKey();
                int var8 = var6.getIntValue();
                SculkSpreader.ChargeCursor var9 = var1.get(var7);
                Collection<Direction> var10 = var9 == null ? null : var9.getFacingData();
                if (var8 > 0 && var10 != null) {
                    int var11 = (int)(Math.log1p((double)var8) / 2.3F) + 1;
                    int var12 = (var11 << 6) + MultifaceBlock.pack(var10);
                    param0.levelEvent(3006, var7, var12);
                }
            }

            this.cursors = var0;
        }
    }

    public static class ChargeCursor {
        private static final ObjectArrayList<Vec3i> NON_CORNER_NEIGHBOURS = Util.make(
            new ObjectArrayList<>(18),
            param0 -> BlockPos.betweenClosedStream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1))
                    .filter(param0x -> (param0x.getX() == 0 || param0x.getY() == 0 || param0x.getZ() == 0) && !param0x.equals(BlockPos.ZERO))
                    .map(BlockPos::immutable)
                    .forEach(param0::add)
        );
        public static final int MAX_CURSOR_DECAY_DELAY = 1;
        private BlockPos pos;
        int charge;
        private int updateDelay;
        private int decayDelay;
        @Nullable
        private Set<Direction> facings;
        private static final Codec<Set<Direction>> DIRECTION_SET = Direction.CODEC
            .listOf()
            .xmap(param0 -> Sets.newEnumSet(param0, Direction.class), Lists::newArrayList);
        public static final Codec<SculkSpreader.ChargeCursor> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        BlockPos.CODEC.fieldOf("pos").forGetter(SculkSpreader.ChargeCursor::getPos),
                        Codec.intRange(0, 1000).fieldOf("charge").orElse(0).forGetter(SculkSpreader.ChargeCursor::getCharge),
                        Codec.intRange(0, 1).fieldOf("decay_delay").orElse(1).forGetter(SculkSpreader.ChargeCursor::getDecayDelay),
                        Codec.intRange(0, Integer.MAX_VALUE).fieldOf("update_delay").orElse(0).forGetter(param0x -> param0x.updateDelay),
                        DIRECTION_SET.optionalFieldOf("facings").forGetter(param0x -> Optional.ofNullable(param0x.getFacingData()))
                    )
                    .apply(param0, SculkSpreader.ChargeCursor::new)
        );

        private ChargeCursor(BlockPos param0, int param1, int param2, int param3, Optional<Set<Direction>> param4) {
            this.pos = param0;
            this.charge = param1;
            this.decayDelay = param2;
            this.updateDelay = param3;
            this.facings = param4.orElse(null);
        }

        public ChargeCursor(BlockPos param0, int param1) {
            this(param0, param1, 1, 0, Optional.empty());
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public int getCharge() {
            return this.charge;
        }

        public int getDecayDelay() {
            return this.decayDelay;
        }

        @Nullable
        public Set<Direction> getFacingData() {
            return this.facings;
        }

        private boolean shouldUpdate(LevelAccessor param0, BlockPos param1, boolean param2) {
            if (this.charge <= 0) {
                return false;
            } else if (param2) {
                return true;
            } else {
                return param0 instanceof ServerLevel var0 ? var0.shouldTickBlocksAt(param1) : false;
            }
        }

        public void update(LevelAccessor param0, BlockPos param1, RandomSource param2, SculkSpreader param3, boolean param4) {
            if (this.shouldUpdate(param0, param1, param3.isWorldGeneration)) {
                if (this.updateDelay > 0) {
                    --this.updateDelay;
                } else {
                    BlockState var0 = param0.getBlockState(this.pos);
                    SculkBehaviour var1 = getBlockBehaviour(var0);
                    if (param4 && var1.attemptSpreadVein(param0, this.pos, var0, this.facings, param3.isWorldGeneration())) {
                        if (var1.canChangeBlockStateOnSpread()) {
                            var0 = param0.getBlockState(this.pos);
                            var1 = getBlockBehaviour(var0);
                        }

                        param0.playSound(null, this.pos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }

                    this.charge = var1.attemptUseCharge(this, param0, param1, param2, param3, param4);
                    if (this.charge <= 0) {
                        var1.onDischarged(param0, var0, this.pos, param2);
                    } else {
                        BlockPos var2 = getValidMovementPos(param0, this.pos, param2);
                        if (var2 != null) {
                            var1.onDischarged(param0, var0, this.pos, param2);
                            this.pos = var2.immutable();
                            if (param3.isWorldGeneration() && !this.pos.closerThan(new Vec3i(param1.getX(), this.pos.getY(), param1.getZ()), 15.0)) {
                                this.charge = 0;
                                return;
                            }

                            var0 = param0.getBlockState(var2);
                        }

                        if (var0.getBlock() instanceof SculkBehaviour) {
                            this.facings = MultifaceBlock.availableFaces(var0);
                        }

                        this.decayDelay = var1.updateDecayDelay(this.decayDelay);
                        this.updateDelay = var1.getSculkSpreadDelay();
                    }
                }
            }
        }

        void mergeWith(SculkSpreader.ChargeCursor param0) {
            this.charge += param0.charge;
            param0.charge = 0;
            this.updateDelay = Math.min(this.updateDelay, param0.updateDelay);
        }

        private static SculkBehaviour getBlockBehaviour(BlockState param0) {
            Block var2 = param0.getBlock();
            return var2 instanceof SculkBehaviour var0 ? var0 : SculkBehaviour.DEFAULT;
        }

        private static List<Vec3i> getRandomizedNonCornerNeighbourOffsets(RandomSource param0) {
            return Util.shuffledCopy(NON_CORNER_NEIGHBOURS, param0);
        }

        @Nullable
        private static BlockPos getValidMovementPos(LevelAccessor param0, BlockPos param1, RandomSource param2) {
            BlockPos.MutableBlockPos var0 = param1.mutable();
            BlockPos.MutableBlockPos var1 = param1.mutable();

            for(Vec3i var2 : getRandomizedNonCornerNeighbourOffsets(param2)) {
                var1.setWithOffset(param1, var2);
                BlockState var3 = param0.getBlockState(var1);
                if (var3.getBlock() instanceof SculkBehaviour && isMovementUnobstructed(param0, param1, var1)) {
                    var0.set(var1);
                    if (SculkVeinBlock.hasSubstrateAccess(param0, var3, var1)) {
                        break;
                    }
                }
            }

            return var0.equals(param1) ? null : var0;
        }

        private static boolean isMovementUnobstructed(LevelAccessor param0, BlockPos param1, BlockPos param2) {
            if (param1.distManhattan(param2) == 1) {
                return true;
            } else {
                BlockPos var0 = param2.subtract(param1);
                Direction var1 = Direction.fromAxisAndDirection(
                    Direction.Axis.X, var0.getX() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE
                );
                Direction var2 = Direction.fromAxisAndDirection(
                    Direction.Axis.Y, var0.getY() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE
                );
                Direction var3 = Direction.fromAxisAndDirection(
                    Direction.Axis.Z, var0.getZ() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE
                );
                if (var0.getX() == 0) {
                    return isUnobstructed(param0, param1, var2) || isUnobstructed(param0, param1, var3);
                } else if (var0.getY() == 0) {
                    return isUnobstructed(param0, param1, var1) || isUnobstructed(param0, param1, var3);
                } else {
                    return isUnobstructed(param0, param1, var1) || isUnobstructed(param0, param1, var2);
                }
            }
        }

        private static boolean isUnobstructed(LevelAccessor param0, BlockPos param1, Direction param2) {
            BlockPos var0 = param1.relative(param2);
            return !param0.getBlockState(var0).isFaceSturdy(param0, var0, param2.getOpposite());
        }
    }
}
