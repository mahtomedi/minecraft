package net.minecraft.client.server;

import com.mojang.authlib.GameProfile;
import java.net.SocketAddress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IntegratedPlayerList extends PlayerList {
    private CompoundTag playerData;

    public IntegratedPlayerList(IntegratedServer param0) {
        super(param0, 8);
        this.setViewDistance(10);
    }

    @Override
    protected void save(ServerPlayer param0) {
        if (param0.getName().getString().equals(this.getServer().getSingleplayerName())) {
            this.playerData = param0.saveWithoutId(new CompoundTag());
        }

        super.save(param0);
    }

    @Override
    public Component canPlayerLogin(SocketAddress param0, GameProfile param1) {
        return (Component)(param1.getName().equalsIgnoreCase(this.getServer().getSingleplayerName()) && this.getPlayerByName(param1.getName()) != null
            ? new TranslatableComponent("multiplayer.disconnect.name_taken")
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
