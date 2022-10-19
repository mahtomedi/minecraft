package net.minecraft.client.server;

import com.mojang.authlib.GameProfile;
import java.net.SocketAddress;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IntegratedPlayerList extends PlayerList {
    private CompoundTag playerData;

    public IntegratedPlayerList(IntegratedServer param0, LayeredRegistryAccess<RegistryLayer> param1, PlayerDataStorage param2) {
        super(param0, param1, param2, 8);
        this.setViewDistance(10);
    }

    @Override
    protected void save(ServerPlayer param0) {
        if (this.getServer().isSingleplayerOwner(param0.getGameProfile())) {
            this.playerData = param0.saveWithoutId(new CompoundTag());
        }

        super.save(param0);
    }

    @Override
    public Component canPlayerLogin(SocketAddress param0, GameProfile param1) {
        return (Component)(this.getServer().isSingleplayerOwner(param1) && this.getPlayerByName(param1.getName()) != null
            ? Component.translatable("multiplayer.disconnect.name_taken")
            : super.canPlayerLogin(param0, param1));
    }

    public IntegratedServer getServer() {
        return (IntegratedServer)super.getServer();
    }

    @Override
    public CompoundTag getSingleplayerData() {
        return this.playerData;
    }
}
