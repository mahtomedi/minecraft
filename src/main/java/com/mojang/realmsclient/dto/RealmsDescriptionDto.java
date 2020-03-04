package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsDescriptionDto extends ValueObject implements ReflectionBasedSerialization {
    @SerializedName("name")
    public String name;
    @SerializedName("description")
    public String description;

    public RealmsDescriptionDto(String param0, String param1) {
        this.name = param0;
        this.description = param1;
    }
}
