package net.minecraft.client.multiplayer.prediction;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockStatePredictionHandler implements AutoCloseable {
    private final Long2ObjectOpenHashMap<BlockStatePredictionHandler.ServerVerifiedState> serverVerifiedStates = new Long2ObjectOpenHashMap<>();
    private int currentSequenceNr;
    private boolean isPredicting;

    public void retainKnownServerState(BlockPos param0, BlockState param1, LocalPlayer param2) {
        this.serverVerifiedStates
            .compute(
                param0.asLong(),
                (param2x, param3) -> param3 != null
                        ? param3.setSequence(this.currentSequenceNr)
                        : new BlockStatePredictionHandler.ServerVerifiedState(this.currentSequenceNr, param1, param2.position())
            );
    }

    public boolean updateKnownServerState(BlockPos param0, BlockState param1) {
        BlockStatePredictionHandler.ServerVerifiedState var0 = this.serverVerifiedStates.get(param0.asLong());
        if (var0 == null) {
            return false;
        } else {
            var0.setBlockState(param1);
            return true;
        }
    }

    public void endPredictionsUpTo(int param0, ClientLevel param1) {
        ObjectIterator<Entry<BlockStatePredictionHandler.ServerVerifiedState>> var0 = this.serverVerifiedStates.long2ObjectEntrySet().iterator();

        while(var0.hasNext()) {
            Entry<BlockStatePredictionHandler.ServerVerifiedState> var1 = var0.next();
            BlockStatePredictionHandler.ServerVerifiedState var2 = var1.getValue();
            if (var2.sequence <= param0) {
                BlockPos var3 = BlockPos.of(var1.getLongKey());
                var0.remove();
                param1.syncBlockState(var3, var2.blockState, var2.playerPos);
            }
        }

    }

    public BlockStatePredictionHandler startPredicting() {
        ++this.currentSequenceNr;
        this.isPredicting = true;
        return this;
    }

    @Override
    public void close() {
        this.isPredicting = false;
    }

    public int currentSequence() {
        return this.currentSequenceNr;
    }

    public boolean isPredicting() {
        return this.isPredicting;
    }

    @OnlyIn(Dist.CLIENT)
    static class ServerVerifiedState {
        final Vec3 playerPos;
        int sequence;
        BlockState blockState;

        ServerVerifiedState(int param0, BlockState param1, Vec3 param2) {
            this.sequence = param0;
            this.blockState = param1;
            this.playerPos = param2;
        }

        BlockStatePredictionHandler.ServerVerifiedState setSequence(int param0) {
            this.sequence = param0;
            return this;
        }

        void setBlockState(BlockState param0) {
            this.blockState = param0;
        }
    }
}
