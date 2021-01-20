package com.mojang.realmsclient.gui.task;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NoStartupDelay implements RestartDelayCalculator {
    @Override
    public void markExecutionStart() {
    }

    @Override
    public long getNextDelayMs() {
        return 0L;
    }
}
