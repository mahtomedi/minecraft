package net.minecraft.server.level;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

public class ServerBossEvent extends BossEvent {
    private final Set<ServerPlayer> players = Sets.newHashSet();
    private final Set<ServerPlayer> unmodifiablePlayers = Collections.unmodifiableSet(this.players);
    private boolean visible = true;

    public ServerBossEvent(Component param0, BossEvent.BossBarColor param1, BossEvent.BossBarOverlay param2) {
        super(Mth.createInsecureUUID(), param0, param1, param2);
    }

    @Override
    public void setProgress(float param0) {
        if (param0 != this.progress) {
            super.setProgress(param0);
            this.broadcast(ClientboundBossEventPacket::createUpdateProgressPacket);
        }

    }

    @Override
    public void setColor(BossEvent.BossBarColor param0) {
        if (param0 != this.color) {
            super.setColor(param0);
            this.broadcast(ClientboundBossEventPacket::createUpdateStylePacket);
        }

    }

    @Override
    public void setOverlay(BossEvent.BossBarOverlay param0) {
        if (param0 != this.overlay) {
            super.setOverlay(param0);
            this.broadcast(ClientboundBossEventPacket::createUpdateStylePacket);
        }

    }

    @Override
    public BossEvent setDarkenScreen(boolean param0) {
        if (param0 != this.darkenScreen) {
            super.setDarkenScreen(param0);
            this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
        }

        return this;
    }

    @Override
    public BossEvent setPlayBossMusic(boolean param0) {
        if (param0 != this.playBossMusic) {
            super.setPlayBossMusic(param0);
            this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
        }

        return this;
    }

    @Override
    public BossEvent setCreateWorldFog(boolean param0) {
        if (param0 != this.createWorldFog) {
            super.setCreateWorldFog(param0);
            this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
        }

        return this;
    }

    @Override
    public void setName(Component param0) {
        if (!Objects.equal(param0, this.name)) {
            super.setName(param0);
            this.broadcast(ClientboundBossEventPacket::createUpdateNamePacket);
        }

    }

    private void broadcast(Function<BossEvent, ClientboundBossEventPacket> param0) {
        if (this.visible) {
            ClientboundBossEventPacket var0 = param0.apply(this);

            for(ServerPlayer var1 : this.players) {
                var1.connection.send(var0);
            }
        }

    }

    public void addPlayer(ServerPlayer param0) {
        if (this.players.add(param0) && this.visible) {
            param0.connection.send(ClientboundBossEventPacket.createAddPacket(this));
        }

    }

    public void removePlayer(ServerPlayer param0) {
        if (this.players.remove(param0) && this.visible) {
            param0.connection.send(ClientboundBossEventPacket.createRemovePacket(this.getId()));
        }

    }

    public void removeAllPlayers() {
        if (!this.players.isEmpty()) {
            for(ServerPlayer var0 : Lists.newArrayList(this.players)) {
                this.removePlayer(var0);
            }
        }

    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean param0) {
        if (param0 != this.visible) {
            this.visible = param0;

            for(ServerPlayer var0 : this.players) {
                var0.connection.send(param0 ? ClientboundBossEventPacket.createAddPacket(this) : ClientboundBossEventPacket.createRemovePacket(this.getId()));
            }
        }

    }

    public Collection<ServerPlayer> getPlayers() {
        return this.unmodifiablePlayers;
    }
}
