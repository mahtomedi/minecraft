package net.minecraft.world.level.redstone;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class CollectingNeighborUpdater implements NeighborUpdater {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ServerLevel level;
    private final ArrayDeque<CollectingNeighborUpdater.NeighborUpdates> stack = new ArrayDeque<>();
    private final List<CollectingNeighborUpdater.NeighborUpdates> addedThisLayer = new ArrayList<>();
    private int count = 0;

    public CollectingNeighborUpdater(ServerLevel param0) {
        this.level = param0;
    }

    @Override
    public void neighborChanged(BlockPos param0, Block param1, BlockPos param2) {
        this.addAndRun(param0, new CollectingNeighborUpdater.SimpleNeighborUpdate(param0, param1, param2));
    }

    @Override
    public void neighborChanged(BlockState param0, BlockPos param1, Block param2, BlockPos param3, boolean param4) {
        this.addAndRun(param1, new CollectingNeighborUpdater.FullNeighborUpdate(param0, param1, param2, param3, param4));
    }

    @Override
    public void updateNeighborsAtExceptFromFacing(BlockPos param0, Block param1, @Nullable Direction param2) {
        this.addAndRun(param0, new CollectingNeighborUpdater.MultiNeighborUpdate(param0, param1, param2));
    }

    private void addAndRun(BlockPos param0, CollectingNeighborUpdater.NeighborUpdates param1) {
        int var0 = this.level.getServer().getMaxChainedNeighborUpdates();
        boolean var1 = this.count > 0;
        boolean var2 = var0 >= 0 && this.count >= var0;
        ++this.count;
        if (!var2) {
            if (var1) {
                this.addedThisLayer.add(param1);
            } else {
                this.stack.push(param1);
            }
        } else if (this.count - 1 == var0) {
            LOGGER.error("Too many chained neighbor updates. Skipping the rest. First skipped position: " + param0.toShortString());
        }

        if (!var1) {
            this.runUpdates();
        }

    }

    private void runUpdates() {
        try {
            while(!this.stack.isEmpty() || !this.addedThisLayer.isEmpty()) {
                for(int var0 = this.addedThisLayer.size() - 1; var0 >= 0; --var0) {
                    this.stack.push(this.addedThisLayer.get(var0));
                }

                this.addedThisLayer.clear();
                CollectingNeighborUpdater.NeighborUpdates var1 = this.stack.peek();

                while(this.addedThisLayer.isEmpty()) {
                    if (!var1.runNext(this.level)) {
                        this.stack.pop();
                        break;
                    }
                }
            }
        } finally {
            this.stack.clear();
            this.addedThisLayer.clear();
            this.count = 0;
        }

    }

    static record FullNeighborUpdate(BlockState state, BlockPos pos, Block block, BlockPos neighborPos, boolean movedByPiston)
        implements CollectingNeighborUpdater.NeighborUpdates {
        @Override
        public boolean runNext(ServerLevel param0) {
            NeighborUpdater.executeUpdate(param0, this.state, this.pos, this.block, this.neighborPos, this.movedByPiston);
            return false;
        }
    }

    static final class MultiNeighborUpdate implements CollectingNeighborUpdater.NeighborUpdates {
        private final BlockPos sourcePos;
        private final Block sourceBlock;
        @Nullable
        private final Direction skipDirection;
        private int idx = 0;

        MultiNeighborUpdate(BlockPos param0, Block param1, @Nullable Direction param2) {
            this.sourcePos = param0;
            this.sourceBlock = param1;
            this.skipDirection = param2;
            if (NeighborUpdater.UPDATE_ORDER[this.idx] == param2) {
                ++this.idx;
            }

        }

        @Override
        public boolean runNext(ServerLevel param0) {
            BlockPos var0 = this.sourcePos.relative(NeighborUpdater.UPDATE_ORDER[this.idx++]);
            BlockState var1 = param0.getBlockState(var0);
            var1.neighborChanged(param0, var0, this.sourceBlock, this.sourcePos, false);
            if (this.idx < NeighborUpdater.UPDATE_ORDER.length && NeighborUpdater.UPDATE_ORDER[this.idx] == this.skipDirection) {
                ++this.idx;
            }

            return this.idx < NeighborUpdater.UPDATE_ORDER.length;
        }
    }

    interface NeighborUpdates {
        boolean runNext(ServerLevel var1);
    }

    static record SimpleNeighborUpdate(BlockPos pos, Block block, BlockPos neighborPos) implements CollectingNeighborUpdater.NeighborUpdates {
        @Override
        public boolean runNext(ServerLevel param0) {
            BlockState var0 = param0.getBlockState(this.pos);
            NeighborUpdater.executeUpdate(param0, var0, this.pos, this.block, this.neighborPos, false);
            return false;
        }
    }
}