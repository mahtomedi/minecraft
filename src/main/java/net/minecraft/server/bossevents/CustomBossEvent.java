package net.minecraft.server.bossevents;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

public class CustomBossEvent extends ServerBossEvent {
    private final ResourceLocation id;
    private final Set<UUID> players = Sets.newHashSet();
    private int value;
    private int max = 100;

    public CustomBossEvent(ResourceLocation param0, Component param1) {
        super(param1, BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
        this.id = param0;
        this.setPercent(0.0F);
    }

    public ResourceLocation getTextId() {
        return this.id;
    }

    @Override
    public void addPlayer(ServerPlayer param0) {
        super.addPlayer(param0);
        this.players.add(param0.getUUID());
    }

    public void addOfflinePlayer(UUID param0) {
        this.players.add(param0);
    }

    @Override
    public void removePlayer(ServerPlayer param0) {
        super.removePlayer(param0);
        this.players.remove(param0.getUUID());
    }

    @Override
    public void removeAllPlayers() {
        super.removeAllPlayers();
        this.players.clear();
    }

    public int getValue() {
        return this.value;
    }

    public int getMax() {
        return this.max;
    }

    public void setValue(int param0) {
        this.value = param0;
        this.setPercent(Mth.clamp((float)param0 / (float)this.max, 0.0F, 1.0F));
    }

    public void setMax(int param0) {
        this.max = param0;
        this.setPercent(Mth.clamp((float)this.value / (float)param0, 0.0F, 1.0F));
    }

    public final Component getDisplayName() {
        return ComponentUtils.wrapInSquareBrackets(this.getName())
            .withStyle(
                param0 -> param0.setColor(this.getColor().getFormatting())
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(this.getTextId().toString())))
                        .setInsertion(this.getTextId().toString())
            );
    }

    public boolean setPlayers(Collection<ServerPlayer> param0) {
        Set<UUID> var0 = Sets.newHashSet();
        Set<ServerPlayer> var1 = Sets.newHashSet();

        for(UUID var2 : this.players) {
            boolean var3 = false;

            for(ServerPlayer var4 : param0) {
                if (var4.getUUID().equals(var2)) {
                    var3 = true;
                    break;
                }
            }

            if (!var3) {
                var0.add(var2);
            }
        }

        for(ServerPlayer var5 : param0) {
            boolean var6 = false;

            for(UUID var7 : this.players) {
                if (var5.getUUID().equals(var7)) {
                    var6 = true;
                    break;
                }
            }

            if (!var6) {
                var1.add(var5);
            }
        }

        for(UUID var8 : var0) {
            for(ServerPlayer var9 : this.getPlayers()) {
                if (var9.getUUID().equals(var8)) {
                    this.removePlayer(var9);
                    break;
                }
            }

            this.players.remove(var8);
        }

        for(ServerPlayer var10 : var1) {
            this.addPlayer(var10);
        }

        return !var0.isEmpty() || !var1.isEmpty();
    }

    public CompoundTag save() {
        CompoundTag var0 = new CompoundTag();
        var0.putString("Name", Component.Serializer.toJson(this.name));
        var0.putBoolean("Visible", this.isVisible());
        var0.putInt("Value", this.value);
        var0.putInt("Max", this.max);
        var0.putString("Color", this.getColor().getName());
        var0.putString("Overlay", this.getOverlay().getName());
        var0.putBoolean("DarkenScreen", this.shouldDarkenScreen());
        var0.putBoolean("PlayBossMusic", this.shouldPlayBossMusic());
        var0.putBoolean("CreateWorldFog", this.shouldCreateWorldFog());
        ListTag var1 = new ListTag();

        for(UUID var2 : this.players) {
            var1.add(NbtUtils.createUUID(var2));
        }

        var0.put("Players", var1);
        return var0;
    }

    public static CustomBossEvent load(CompoundTag param0, ResourceLocation param1) {
        CustomBossEvent var0 = new CustomBossEvent(param1, Component.Serializer.fromJson(param0.getString("Name")));
        var0.setVisible(param0.getBoolean("Visible"));
        var0.setValue(param0.getInt("Value"));
        var0.setMax(param0.getInt("Max"));
        var0.setColor(BossEvent.BossBarColor.byName(param0.getString("Color")));
        var0.setOverlay(BossEvent.BossBarOverlay.byName(param0.getString("Overlay")));
        var0.setDarkenScreen(param0.getBoolean("DarkenScreen"));
        var0.setPlayBossMusic(param0.getBoolean("PlayBossMusic"));
        var0.setCreateWorldFog(param0.getBoolean("CreateWorldFog"));
        ListTag var1 = param0.getList("Players", 11);

        for(int var2 = 0; var2 < var1.size(); ++var2) {
            var0.addOfflinePlayer(NbtUtils.loadUUID(var1.get(var2)));
        }

        return var0;
    }

    public void onPlayerConnect(ServerPlayer param0) {
        if (this.players.contains(param0.getUUID())) {
            this.addPlayer(param0);
        }

    }

    public void onPlayerDisconnect(ServerPlayer param0) {
        super.removePlayer(param0);
    }
}
