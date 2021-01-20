package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResettingGeneratedWorldTask extends ResettingWorldTask {
    private final WorldGenerationInfo generationInfo;

    public ResettingGeneratedWorldTask(WorldGenerationInfo param0, long param1, Component param2, Runnable param3) {
        super(param1, param2, param3);
        this.generationInfo = param0;
    }

    @Override
    protected void sendResetRequest(RealmsClient param0, long param1) throws RealmsServiceException {
        param0.resetWorldWithSeed(param1, this.generationInfo);
    }
}
