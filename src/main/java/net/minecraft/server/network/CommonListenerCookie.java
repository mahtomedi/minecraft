package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ClientInformation;

public record CommonListenerCookie(GameProfile gameProfile, int latency, ClientInformation clientInformation) {
    public static CommonListenerCookie createInitial(GameProfile param0) {
        return new CommonListenerCookie(param0, 0, ClientInformation.createDefault());
    }
}
