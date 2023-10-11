package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Set;
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
    @SerializedName("experiments")
    private final Set<String> experiments;

    public RealmsWorldResetDto(String param0, long param1, int param2, boolean param3, Set<String> param4) {
        this.seed = param0;
        this.worldTemplateId = param1;
        this.levelType = param2;
        this.generateStructures = param3;
        this.experiments = param4;
    }
}
