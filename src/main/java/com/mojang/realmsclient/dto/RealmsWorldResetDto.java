package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsWorldResetDto extends ValueObject implements ReflectionBasedSerialization {
    @SerializedName("seed")
    private final String seed;
    @SerializedName("worldTemplateId")
    private final long worldTemplateId;
    @SerializedName("levelType")
    private final int levelType;
    @SerializedName("generateStructures")
    private final boolean generateStructures;

    public RealmsWorldResetDto(String param0, long param1, int param2, boolean param3) {
        this.seed = param0;
        this.worldTemplateId = param1;
        this.levelType = param2;
        this.generateStructures = param3;
    }
}
