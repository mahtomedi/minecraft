package net.minecraft.server.players;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class SleepStatus {
    private int activePlayers;
    private int sleepingPlayers;

    public boolean areEnoughSleeping(int param0) {
        return this.sleepingPlayers >= this.sleepersNeeded(param0);
    }

    public boolean areEnoughDeepSleeping(int param0, List<ServerPlayer> param1) {
        int var0 = (int)param1.stream().filter(Player::isSleepingLongEnough).count();
        return var0 >= this.sleepersNeeded(param0);
    }

    public int sleepersNeeded(int param0) {
        return Math.max(1, Mth.ceil((float)(this.activePlayers * param0) / 100.0F));
    }

    public void removeAllSleepers() {
        this.sleepingPlayers = 0;
    }

    public int amountSleeping() {
        return this.sleepingPlayers;
    }

    public boolean update(List<ServerPlayer> param0) {
        int var0 = this.activePlayers;
        int var1 = this.sleepingPlayers;
        this.activePlayers = 0;
        this.sleepingPlayers = 0;

        for(ServerPlayer var2 : param0) {
            if (!var2.isSpectator()) {
                ++this.activePlayers;
                if (var2.isSleeping()) {
                    ++this.sleepingPlayers;
                }
            }
        }

        return (var1 > 0 || this.sleepingPlayers > 0) && (var0 != this.activePlayers || var1 != this.sleepingPlayers);
    }
}
