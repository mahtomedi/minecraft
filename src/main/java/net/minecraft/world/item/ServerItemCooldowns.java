package net.minecraft.world.item;

import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.server.level.ServerPlayer;

public class ServerItemCooldowns extends ItemCooldowns {
    private final ServerPlayer player;

    public ServerItemCooldowns(ServerPlayer param0) {
        this.player = param0;
    }

    @Override
    protected void onCooldownStarted(Item param0, int param1) {
        super.onCooldownStarted(param0, param1);
        this.player.connection.send(new ClientboundCooldownPacket(param0, param1));
    }

    @Override
    protected void onCooldownEnded(Item param0) {
        super.onCooldownEnded(param0);
        this.player.connection.send(new ClientboundCooldownPacket(param0, 0));
    }
}
