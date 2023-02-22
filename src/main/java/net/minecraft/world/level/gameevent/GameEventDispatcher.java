package net.minecraft.world.level.gameevent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;

public class GameEventDispatcher {
    private final ServerLevel level;

    public GameEventDispatcher(ServerLevel param0) {
        this.level = param0;
    }

    public void post(GameEvent param0, Vec3 param1, GameEvent.Context param2) {
        int var0 = param0.getNotificationRadius();
        BlockPos var1 = BlockPos.containing(param1);
        int var2 = SectionPos.blockToSectionCoord(var1.getX() - var0);
        int var3 = SectionPos.blockToSectionCoord(var1.getY() - var0);
        int var4 = SectionPos.blockToSectionCoord(var1.getZ() - var0);
        int var5 = SectionPos.blockToSectionCoord(var1.getX() + var0);
        int var6 = SectionPos.blockToSectionCoord(var1.getY() + var0);
        int var7 = SectionPos.blockToSectionCoord(var1.getZ() + var0);
        List<GameEvent.ListenerInfo> var8 = new ArrayList<>();
        GameEventListenerRegistry.ListenerVisitor var9 = (param4, param5) -> {
            if (param4.getDeliveryMode() == GameEventListener.DeliveryMode.BY_DISTANCE) {
                var8.add(new GameEvent.ListenerInfo(param0, param1, param2, param4, param5));
            } else {
                param4.handleGameEvent(this.level, param0, param2, param1);
            }

        };
        boolean var10 = false;

        for(int var11 = var2; var11 <= var5; ++var11) {
            for(int var12 = var4; var12 <= var7; ++var12) {
                ChunkAccess var13 = this.level.getChunkSource().getChunkNow(var11, var12);
                if (var13 != null) {
                    for(int var14 = var3; var14 <= var6; ++var14) {
                        var10 |= var13.getListenerRegistry(var14).visitInRangeListeners(param0, param1, param2, var9);
                    }
                }
            }
        }

        if (!var8.isEmpty()) {
            this.handleGameEventMessagesInQueue(var8);
        }

        if (var10) {
            DebugPackets.sendGameEventInfo(this.level, param0, param1);
        }

    }

    private void handleGameEventMessagesInQueue(List<GameEvent.ListenerInfo> param0) {
        Collections.sort(param0);

        for(GameEvent.ListenerInfo var0 : param0) {
            GameEventListener var1 = var0.recipient();
            var1.handleGameEvent(this.level, var0.gameEvent(), var0.context(), var0.source());
        }

    }
}
