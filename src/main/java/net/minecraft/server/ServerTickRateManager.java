package net.minecraft.server;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.TickRateManager;

public class ServerTickRateManager extends TickRateManager {
    private long remainingSprintTicks = 0L;
    private long sprintTickStartTime = 0L;
    private long sprintTimeSpend = 0L;
    private long scheduledCurrentSprintTicks = 0L;
    private boolean previousIsFrozen = false;
    private final MinecraftServer server;

    public ServerTickRateManager(MinecraftServer param0) {
        this.server = param0;
    }

    public boolean isSprinting() {
        return this.scheduledCurrentSprintTicks > 0L;
    }

    @Override
    public void setFrozen(boolean param0) {
        super.setFrozen(param0);
        this.updateStateToClients();
    }

    private void updateStateToClients() {
        this.server.getPlayerList().broadcastAll(ClientboundTickingStatePacket.from(this));
    }

    private void updateStepTicks() {
        this.server.getPlayerList().broadcastAll(ClientboundTickingStepPacket.from(this));
    }

    public boolean stepGameIfPaused(int param0) {
        if (!this.isFrozen()) {
            return false;
        } else {
            this.frozenTicksToRun = param0;
            this.updateStepTicks();
            return true;
        }
    }

    public boolean stopStepping() {
        if (this.frozenTicksToRun > 0) {
            this.frozenTicksToRun = 0;
            this.updateStepTicks();
            return true;
        } else {
            return false;
        }
    }

    public boolean stopSprinting() {
        if (this.remainingSprintTicks > 0L) {
            this.finishTickSprint();
            return true;
        } else {
            return false;
        }
    }

    public boolean requestGameToSprint(int param0) {
        boolean var0 = this.remainingSprintTicks > 0L;
        this.sprintTimeSpend = 0L;
        this.scheduledCurrentSprintTicks = (long)param0;
        this.remainingSprintTicks = (long)param0;
        this.previousIsFrozen = this.isFrozen();
        this.setFrozen(false);
        return var0;
    }

    private void finishTickSprint() {
        long var0 = this.scheduledCurrentSprintTicks - this.remainingSprintTicks;
        double var1 = Math.max(1.0, (double)this.sprintTimeSpend) / (double)TimeUtil.NANOSECONDS_PER_MILLISECOND;
        int var2 = (int)((double)(TimeUtil.MILLISECONDS_PER_SECOND * var0) / var1);
        String var3 = String.format("%.2f", var0 == 0L ? (double)this.millisecondsPerTick() : var1 / (double)var0);
        this.scheduledCurrentSprintTicks = 0L;
        this.sprintTimeSpend = 0L;
        this.server.createCommandSourceStack().sendSuccess(() -> Component.translatable("commands.tick.sprint.report", var2, var3), true);
        this.remainingSprintTicks = 0L;
        this.setFrozen(this.previousIsFrozen);
        this.server.onTickRateChanged();
    }

    public boolean checkShouldSprintThisTick() {
        if (!this.runGameElements) {
            return false;
        } else if (this.remainingSprintTicks > 0L) {
            this.sprintTickStartTime = System.nanoTime();
            --this.remainingSprintTicks;
            return true;
        } else {
            this.finishTickSprint();
            return false;
        }
    }

    public void endTickWork() {
        this.sprintTimeSpend += System.nanoTime() - this.sprintTickStartTime;
    }

    @Override
    public void setTickRate(float param0) {
        super.setTickRate(param0);
        this.server.onTickRateChanged();
        this.updateStateToClients();
    }

    public void updateJoiningPlayer(ServerPlayer param0) {
        param0.connection.send(ClientboundTickingStatePacket.from(this));
        param0.connection.send(ClientboundTickingStepPacket.from(this));
    }
}
