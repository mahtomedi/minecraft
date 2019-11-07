package net.minecraft.client;

import com.mojang.bridge.Bridge;
import com.mojang.bridge.game.GameSession;
import com.mojang.bridge.game.GameVersion;
import com.mojang.bridge.game.Language;
import com.mojang.bridge.game.PerformanceMetrics;
import com.mojang.bridge.game.RunningGame;
import com.mojang.bridge.launcher.Launcher;
import com.mojang.bridge.launcher.SessionEventListener;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.FrameTimer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Game implements RunningGame {
    private final Minecraft minecraft;
    @Nullable
    private final Launcher launcher;
    private SessionEventListener listener = SessionEventListener.NONE;

    public Game(Minecraft param0) {
        this.minecraft = param0;
        this.launcher = Bridge.getLauncher();
        if (this.launcher != null) {
            this.launcher.registerGame(this);
        }

    }

    @Override
    public GameVersion getVersion() {
        return SharedConstants.getCurrentVersion();
    }

    @Override
    public Language getSelectedLanguage() {
        return this.minecraft.getLanguageManager().getSelected();
    }

    @Nullable
    @Override
    public GameSession getCurrentSession() {
        ClientLevel var0 = this.minecraft.level;
        return var0 == null ? null : new Session(var0, this.minecraft.player, this.minecraft.player.connection);
    }

    @Override
    public PerformanceMetrics getPerformanceMetrics() {
        FrameTimer var0 = this.minecraft.getFrameTimer();
        long var1 = 2147483647L;
        long var2 = -2147483648L;
        long var3 = 0L;

        for(long var4 : var0.getLog()) {
            var1 = Math.min(var1, var4);
            var2 = Math.max(var2, var4);
            var3 += var4;
        }

        return new Game.Metrics((int)var1, (int)var2, (int)(var3 / (long)var0.getLog().length), var0.getLog().length);
    }

    @Override
    public void setSessionEventListener(SessionEventListener param0) {
        this.listener = param0;
    }

    public void onStartGameSession() {
        this.listener.onStartGameSession(this.getCurrentSession());
    }

    public void onLeaveGameSession() {
        this.listener.onLeaveGameSession(this.getCurrentSession());
    }

    @OnlyIn(Dist.CLIENT)
    static class Metrics implements PerformanceMetrics {
        private final int min;
        private final int max;
        private final int average;
        private final int samples;

        public Metrics(int param0, int param1, int param2, int param3) {
            this.min = param0;
            this.max = param1;
            this.average = param2;
            this.samples = param3;
        }

        @Override
        public int getMinTime() {
            return this.min;
        }

        @Override
        public int getMaxTime() {
            return this.max;
        }

        @Override
        public int getAverageTime() {
            return this.average;
        }

        @Override
        public int getSampleCount() {
            return this.samples;
        }
    }
}
