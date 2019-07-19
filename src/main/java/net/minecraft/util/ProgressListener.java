package net.minecraft.util;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ProgressListener {
    void progressStartNoAbort(Component var1);

    @OnlyIn(Dist.CLIENT)
    void progressStart(Component var1);

    void progressStage(Component var1);

    void progressStagePercentage(int var1);

    @OnlyIn(Dist.CLIENT)
    void stop();
}
