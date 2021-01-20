package com.mojang.realmsclient.dto;

import com.google.gson.Gson;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuardedSerializer {
    private final Gson gson = new Gson();

    public String toJson(ReflectionBasedSerialization param0) {
        return this.gson.toJson(param0);
    }

    @Nullable
    public <T extends ReflectionBasedSerialization> T fromJson(String param0, Class<T> param1) {
        return this.gson.fromJson(param0, param1);
    }
}
