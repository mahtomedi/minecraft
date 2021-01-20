package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResettingTemplateWorldTask extends ResettingWorldTask {
    private final WorldTemplate template;

    public ResettingTemplateWorldTask(WorldTemplate param0, long param1, Component param2, Runnable param3) {
        super(param1, param2, param3);
        this.template = param0;
    }

    @Override
    protected void sendResetRequest(RealmsClient param0, long param1) throws RealmsServiceException {
        param0.resetWorldWithTemplate(param1, this.template.id);
    }
}
