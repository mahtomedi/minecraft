package net.minecraft.client.resources.server;

import java.util.UUID;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface PackLoadFeedback {
    void sendResponse(UUID var1, PackLoadFeedback.Result var2);

    @OnlyIn(Dist.CLIENT)
    public static enum Result {
        ACCEPTED,
        DECLINED,
        APPLIED,
        DISCARDED,
        DOWNLOAD_FAILED,
        ACTIVATION_FAILED;
    }
}
