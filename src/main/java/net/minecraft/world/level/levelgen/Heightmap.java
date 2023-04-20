package net.minecraft.world.level.levelgen;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.slf4j.Logger;

public class Heightmap {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final Predicate<BlockState> NOT_AIR = param0 -> !param0.isAir();
    static final Predicate<BlockState> MATERIAL_MOTION_BLOCKING = BlockBehaviour.BlockStateBase::blocksMotion;
    private final BitStorage data;
    private final Predicate<BlockState> isOpaque;
    private final ChunkAccess chunk;

    public Heightmap(ChunkAccess param0, Heightmap.Types param1) {
        this.isOpaque = param1.isOpaque();
        this.chunk = param0;
        int var0 = Mth.ceillog2(param0.getHeight() + 1);
        this.data = new SimpleBitStorage(var0, 256);
    }

    public static void primeHeightmaps(ChunkAccess param0, Set<Heightmap.Types> param1) {
        int var0 = param1.size();
        ObjectList<Heightmap> var1 = new ObjectArrayList<>(var0);
        ObjectListIterator<Heightmap> var2 = var1.iterator();
        int var3 = param0.getHighestSectionPosition() + 16;
        BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

        for(int var5 = 0; var5 < 16; ++var5) {
            for(int var6 = 0; var6 < 16; ++var6) {
                for(Heightmap.Types var7 : param1) {
                    var1.add(param0.getOrCreateHeightmapUnprimed(var7));
                }

                for(int var8 = var3 - 1; var8 >= param0.getMinBuildHeight(); --var8) {
                    var4.set(var5, var8, var6);
                    BlockState var9 = param0.getBlockState(var4);
                    if (!var9.is(Blocks.AIR)) {
                        while(var2.hasNext()) {
                            Heightmap var10 = var2.next();
                            if (var10.isOpaque.test(var9)) {
                                var10.setHeight(var5, var6, var8 + 1);
                                var2.remove();
                            }
                        }

                        if (var1.isEmpty()) {
                            break;
                        }

                        var2.back(var0);
                    }
                }
            }
        }

    }

    public boolean update(int param0, int param1, int param2, BlockState param3) {
        int var0 = this.getFirstAvailable(param0, param2);
        if (param1 <= var0 - 2) {
            return false;
        } else {
            if (this.isOpaque.test(param3)) {
                if (param1 >= var0) {
                    this.setHeight(param0, param2, param1 + 1);
                    return true;
                }
            } else if (var0 - 1 == param1) {
                BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

                for(int var2 = param1 - 1; var2 >= this.chunk.getMinBuildHeight(); --var2) {
                    var1.set(param0, var2, param2);
                    if (this.isOpaque.test(this.chunk.getBlockState(var1))) {
                        this.setHeight(param0, param2, var2 + 1);
                        return true;
                    }
                }

                this.setHeight(param0, param2, this.chunk.getMinBuildHeight());
                return true;
            }

            return false;
        }
    }

    public int getFirstAvailable(int param0, int param1) {
        return this.getFirstAvailable(getIndex(param0, param1));
    }

    public int getHighestTaken(int param0, int param1) {
        return this.getFirstAvailable(getIndex(param0, param1)) - 1;
    }

    private int getFirstAvailable(int param0) {
        return this.data.get(param0) + this.chunk.getMinBuildHeight();
    }

    private void setHeight(int param0, int param1, int param2) {
        this.data.set(getIndex(param0, param1), param2 - this.chunk.getMinBuildHeight());
    }

    public void setRawData(ChunkAccess param0, Heightmap.Types param1, long[] param2) {
        long[] var0 = this.data.getRaw();
        if (var0.length == param2.length) {
            System.arraycopy(param2, 0, var0, 0, param2.length);
        } else {
            LOGGER.warn("Ignoring heightmap data for chunk " + param0.getPos() + ", size does not match; expected: " + var0.length + ", got: " + param2.length);
            primeHeightmaps(param0, EnumSet.of(param1));
        }
    }

    public long[] getRawData() {
        return this.data.getRaw();
    }

    private static int getIndex(int param0, int param1) {
        return param0 + param1 * 16;
    }

    public static enum Types implements StringRepresentable {
        WORLD_SURFACE_WG("WORLD_SURFACE_WG", Heightmap.Usage.WORLDGEN, Heightmap.NOT_AIR),
        WORLD_SURFACE("WORLD_SURFACE", Heightmap.Usage.CLIENT, Heightmap.NOT_AIR),
        OCEAN_FLOOR_WG("OCEAN_FLOOR_WG", Heightmap.Usage.WORLDGEN, Heightmap.MATERIAL_MOTION_BLOCKING),
        OCEAN_FLOOR("OCEAN_FLOOR", Heightmap.Usage.LIVE_WORLD, Heightmap.MATERIAL_MOTION_BLOCKING),
        MOTION_BLOCKING("MOTION_BLOCKING", Heightmap.Usage.CLIENT, param0 -> param0.blocksMotion() || !param0.getFluidState().isEmpty()),
        MOTION_BLOCKING_NO_LEAVES(
            "MOTION_BLOCKING_NO_LEAVES",
            Heightmap.Usage.LIVE_WORLD,
            param0 -> (param0.blocksMotion() || !param0.getFluidState().isEmpty()) && !(param0.getBlock() instanceof LeavesBlock)
        );

        public static final Codec<Heightmap.Types> CODEC = StringRepresentable.fromEnum(Heightmap.Types::values);
        private final String serializationKey;
        private final Heightmap.Usage usage;
        private final Predicate<BlockState> isOpaque;

        private Types(String param0, Heightmap.Usage param1, Predicate<BlockState> param2) {
            this.serializationKey = param0;
            this.usage = param1;
            this.isOpaque = param2;
        }

        public String getSerializationKey() {
            return this.serializationKey;
        }

        public boolean sendToClient() {
            return this.usage == Heightmap.Usage.CLIENT;
        }

        public boolean keepAfterWorldgen() {
            return this.usage != Heightmap.Usage.WORLDGEN;
        }

        public Predicate<BlockState> isOpaque() {
            return this.isOpaque;
        }

        @Override
        public String getSerializedName() {
            return this.serializationKey;
        }
    }

    public static enum Usage {
        WORLDGEN,
        LIVE_WORLD,
        CLIENT;
    }
}
