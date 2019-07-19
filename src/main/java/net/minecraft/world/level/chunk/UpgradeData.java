package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpgradeData {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final UpgradeData EMPTY = new UpgradeData();
    private static final Direction8[] DIRECTIONS = Direction8.values();
    private final EnumSet<Direction8> sides = EnumSet.noneOf(Direction8.class);
    private final int[][] index = new int[16][];
    private static final Map<Block, UpgradeData.BlockFixer> MAP = new IdentityHashMap<>();
    private static final Set<UpgradeData.BlockFixer> CHUNKY_FIXERS = Sets.newHashSet();

    private UpgradeData() {
    }

    public UpgradeData(CompoundTag param0) {
        this();
        if (param0.contains("Indices", 10)) {
            CompoundTag var0 = param0.getCompound("Indices");

            for(int var1 = 0; var1 < this.index.length; ++var1) {
                String var2 = String.valueOf(var1);
                if (var0.contains(var2, 11)) {
                    this.index[var1] = var0.getIntArray(var2);
                }
            }
        }

        int var3 = param0.getInt("Sides");

        for(Direction8 var4 : Direction8.values()) {
            if ((var3 & 1 << var4.ordinal()) != 0) {
                this.sides.add(var4);
            }
        }

    }

    public void upgrade(LevelChunk param0) {
        this.upgradeInside(param0);

        for(Direction8 var0 : DIRECTIONS) {
            upgradeSides(param0, var0);
        }

        Level var1 = param0.getLevel();
        CHUNKY_FIXERS.forEach(param1 -> param1.processChunk(var1));
    }

    private static void upgradeSides(LevelChunk param0, Direction8 param1) {
        Level var0 = param0.getLevel();
        if (param0.getUpgradeData().sides.remove(param1)) {
            Set<Direction> var1 = param1.getDirections();
            int var2 = 0;
            int var3 = 15;
            boolean var4 = var1.contains(Direction.EAST);
            boolean var5 = var1.contains(Direction.WEST);
            boolean var6 = var1.contains(Direction.SOUTH);
            boolean var7 = var1.contains(Direction.NORTH);
            boolean var8 = var1.size() == 1;
            ChunkPos var9 = param0.getPos();
            int var10 = var9.getMinBlockX() + (!var8 || !var7 && !var6 ? (var5 ? 0 : 15) : 1);
            int var11 = var9.getMinBlockX() + (!var8 || !var7 && !var6 ? (var5 ? 0 : 15) : 14);
            int var12 = var9.getMinBlockZ() + (!var8 || !var4 && !var5 ? (var7 ? 0 : 15) : 1);
            int var13 = var9.getMinBlockZ() + (!var8 || !var4 && !var5 ? (var7 ? 0 : 15) : 14);
            Direction[] var14 = Direction.values();
            BlockPos.MutableBlockPos var15 = new BlockPos.MutableBlockPos();

            for(BlockPos var16 : BlockPos.betweenClosed(var10, 0, var12, var11, var0.getMaxBuildHeight() - 1, var13)) {
                BlockState var17 = var0.getBlockState(var16);
                BlockState var18 = var17;

                for(Direction var19 : var14) {
                    var15.set(var16).move(var19);
                    var18 = updateState(var18, var19, var0, var16, var15);
                }

                Block.updateOrDestroy(var17, var18, var0, var16, 18);
            }

        }
    }

    private static BlockState updateState(BlockState param0, Direction param1, LevelAccessor param2, BlockPos param3, BlockPos param4) {
        return MAP.getOrDefault(param0.getBlock(), UpgradeData.BlockFixers.DEFAULT)
            .updateShape(param0, param1, param2.getBlockState(param4), param2, param3, param4);
    }

    private void upgradeInside(LevelChunk param0) {
        try (
            BlockPos.PooledMutableBlockPos var0 = BlockPos.PooledMutableBlockPos.acquire();
            BlockPos.PooledMutableBlockPos var1 = BlockPos.PooledMutableBlockPos.acquire();
        ) {
            ChunkPos var2 = param0.getPos();
            LevelAccessor var3 = param0.getLevel();

            for(int var4 = 0; var4 < 16; ++var4) {
                LevelChunkSection var5 = param0.getSections()[var4];
                int[] var6 = this.index[var4];
                this.index[var4] = null;
                if (var5 != null && var6 != null && var6.length > 0) {
                    Direction[] var7 = Direction.values();
                    PalettedContainer<BlockState> var8 = var5.getStates();

                    for(int var9 : var6) {
                        int var10 = var9 & 15;
                        int var11 = var9 >> 8 & 15;
                        int var12 = var9 >> 4 & 15;
                        var0.set(var2.getMinBlockX() + var10, (var4 << 4) + var11, var2.getMinBlockZ() + var12);
                        BlockState var13 = var8.get(var9);
                        BlockState var14 = var13;

                        for(Direction var15 : var7) {
                            var1.set(var0).move(var15);
                            if (var0.getX() >> 4 == var2.x && var0.getZ() >> 4 == var2.z) {
                                var14 = updateState(var14, var15, var3, var0, var1);
                            }
                        }

                        Block.updateOrDestroy(var13, var14, var3, var0, 18);
                    }
                }
            }

            for(int var16 = 0; var16 < this.index.length; ++var16) {
                if (this.index[var16] != null) {
                    LOGGER.warn("Discarding update data for section {} for chunk ({} {})", var16, var2.x, var2.z);
                }

                this.index[var16] = null;
            }
        }

    }

    public boolean isEmpty() {
        for(int[] var0 : this.index) {
            if (var0 != null) {
                return false;
            }
        }

        return this.sides.isEmpty();
    }

    public CompoundTag write() {
        CompoundTag var0 = new CompoundTag();
        CompoundTag var1 = new CompoundTag();

        for(int var2 = 0; var2 < this.index.length; ++var2) {
            String var3 = String.valueOf(var2);
            if (this.index[var2] != null && this.index[var2].length != 0) {
                var1.putIntArray(var3, this.index[var2]);
            }
        }

        if (!var1.isEmpty()) {
            var0.put("Indices", var1);
        }

        int var4 = 0;

        for(Direction8 var5 : this.sides) {
            var4 |= 1 << var5.ordinal();
        }

        var0.putByte("Sides", (byte)var4);
        return var0;
    }

    public interface BlockFixer {
        BlockState updateShape(BlockState var1, Direction var2, BlockState var3, LevelAccessor var4, BlockPos var5, BlockPos var6);

        default void processChunk(LevelAccessor param0) {
        }
    }

    static enum BlockFixers implements UpgradeData.BlockFixer {
        BLACKLIST(
            Blocks.OBSERVER,
            Blocks.NETHER_PORTAL,
            Blocks.WHITE_CONCRETE_POWDER,
            Blocks.ORANGE_CONCRETE_POWDER,
            Blocks.MAGENTA_CONCRETE_POWDER,
            Blocks.LIGHT_BLUE_CONCRETE_POWDER,
            Blocks.YELLOW_CONCRETE_POWDER,
            Blocks.LIME_CONCRETE_POWDER,
            Blocks.PINK_CONCRETE_POWDER,
            Blocks.GRAY_CONCRETE_POWDER,
            Blocks.LIGHT_GRAY_CONCRETE_POWDER,
            Blocks.CYAN_CONCRETE_POWDER,
            Blocks.PURPLE_CONCRETE_POWDER,
            Blocks.BLUE_CONCRETE_POWDER,
            Blocks.BROWN_CONCRETE_POWDER,
            Blocks.GREEN_CONCRETE_POWDER,
            Blocks.RED_CONCRETE_POWDER,
            Blocks.BLACK_CONCRETE_POWDER,
            Blocks.ANVIL,
            Blocks.CHIPPED_ANVIL,
            Blocks.DAMAGED_ANVIL,
            Blocks.DRAGON_EGG,
            Blocks.GRAVEL,
            Blocks.SAND,
            Blocks.RED_SAND,
            Blocks.OAK_SIGN,
            Blocks.SPRUCE_SIGN,
            Blocks.BIRCH_SIGN,
            Blocks.ACACIA_SIGN,
            Blocks.JUNGLE_SIGN,
            Blocks.DARK_OAK_SIGN,
            Blocks.OAK_WALL_SIGN,
            Blocks.SPRUCE_WALL_SIGN,
            Blocks.BIRCH_WALL_SIGN,
            Blocks.ACACIA_WALL_SIGN,
            Blocks.JUNGLE_WALL_SIGN,
            Blocks.DARK_OAK_WALL_SIGN
        ) {
            @Override
            public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
                return param0;
            }
        },
        DEFAULT {
            @Override
            public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
                return param0.updateShape(param1, param3.getBlockState(param5), param3, param4, param5);
            }
        },
        CHEST(Blocks.CHEST, Blocks.TRAPPED_CHEST) {
            @Override
            public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
                if (param2.getBlock() == param0.getBlock()
                    && param1.getAxis().isHorizontal()
                    && param0.getValue(ChestBlock.TYPE) == ChestType.SINGLE
                    && param2.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
                    Direction var0 = param0.getValue(ChestBlock.FACING);
                    if (param1.getAxis() != var0.getAxis() && var0 == param2.getValue(ChestBlock.FACING)) {
                        ChestType var1 = param1 == var0.getClockWise() ? ChestType.LEFT : ChestType.RIGHT;
                        param3.setBlock(param5, param2.setValue(ChestBlock.TYPE, var1.getOpposite()), 18);
                        if (var0 == Direction.NORTH || var0 == Direction.EAST) {
                            BlockEntity var2 = param3.getBlockEntity(param4);
                            BlockEntity var3 = param3.getBlockEntity(param5);
                            if (var2 instanceof ChestBlockEntity && var3 instanceof ChestBlockEntity) {
                                ChestBlockEntity.swapContents((ChestBlockEntity)var2, (ChestBlockEntity)var3);
                            }
                        }

                        return param0.setValue(ChestBlock.TYPE, var1);
                    }
                }

                return param0;
            }
        },
        LEAVES(true, Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES) {
            private final ThreadLocal<List<ObjectSet<BlockPos>>> queue = ThreadLocal.withInitial(() -> Lists.newArrayListWithCapacity(7));

            @Override
            public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
                BlockState var0 = param0.updateShape(param1, param3.getBlockState(param5), param3, param4, param5);
                if (param0 != var0) {
                    int var1 = var0.getValue(BlockStateProperties.DISTANCE);
                    List<ObjectSet<BlockPos>> var2 = this.queue.get();
                    if (var2.isEmpty()) {
                        for(int var3 = 0; var3 < 7; ++var3) {
                            var2.add(new ObjectOpenHashSet<>());
                        }
                    }

                    var2.get(var1).add(param4.immutable());
                }

                return param0;
            }

            @Override
            public void processChunk(LevelAccessor param0) {
                BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
                List<ObjectSet<BlockPos>> var1 = this.queue.get();

                for(int var2 = 2; var2 < var1.size(); ++var2) {
                    int var3 = var2 - 1;
                    ObjectSet<BlockPos> var4 = var1.get(var3);
                    ObjectSet<BlockPos> var5 = var1.get(var2);

                    for(BlockPos var6 : var4) {
                        BlockState var7 = param0.getBlockState(var6);
                        if (var7.getValue(BlockStateProperties.DISTANCE) >= var3) {
                            param0.setBlock(var6, var7.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(var3)), 18);
                            if (var2 != 7) {
                                for(Direction var8 : DIRECTIONS) {
                                    var0.set(var6).move(var8);
                                    BlockState var9 = param0.getBlockState(var0);
                                    if (var9.hasProperty(BlockStateProperties.DISTANCE) && var7.getValue(BlockStateProperties.DISTANCE) > var2) {
                                        var5.add(var0.immutable());
                                    }
                                }
                            }
                        }
                    }
                }

                var1.clear();
            }
        },
        STEM_BLOCK(Blocks.MELON_STEM, Blocks.PUMPKIN_STEM) {
            @Override
            public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
                if (param0.getValue(StemBlock.AGE) == 7) {
                    StemGrownBlock var0 = ((StemBlock)param0.getBlock()).getFruit();
                    if (param2.getBlock() == var0) {
                        return var0.getAttachedStem().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, param1);
                    }
                }

                return param0;
            }
        };

        public static final Direction[] DIRECTIONS = Direction.values();

        private BlockFixers(Block... param0) {
            this(false, param0);
        }

        private BlockFixers(boolean param0, Block... param1) {
            for(Block param2 : param1) {
                UpgradeData.MAP.put(param2, this);
            }

            if (param0) {
                UpgradeData.CHUNKY_FIXERS.add(this);
            }

        }
    }
}
