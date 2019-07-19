package com.mojang.realmsclient.dto;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsWorldResetDto extends ValueObject {
    private final String seed;
    private final long worldTemplateId;
    private final int levelType;
    private final boolean generateStructures;

    public RealmsWorldResetDto(String param0, long param1, int param2, boolean param3) {
        this.seed = param0;
        this.worldTemplateId = param1;
        this.levelType = param2;
        this.generateStructures = param3;
    }
}
