package net.minecraft.client.gui.spectator.categories;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TeleportToTeamMenuCategory implements SpectatorMenuCategory, SpectatorMenuItem {
    private final List<SpectatorMenuItem> items = Lists.newArrayList();

    public TeleportToTeamMenuCategory() {
        Minecraft var0 = Minecraft.getInstance();

        for(PlayerTeam var1 : var0.level.getScoreboard().getPlayerTeams()) {
            this.items.add(new TeleportToTeamMenuCategory.TeamSelectionItem(var1));
        }

    }

    @Override
    public List<SpectatorMenuItem> getItems() {
        return this.items;
    }

    @Override
    public Component getPrompt() {
        return new TranslatableComponent("spectatorMenu.team_teleport.prompt");
    }

    @Override
    public void selectItem(SpectatorMenu param0) {
        param0.selectCategory(this);
    }

    @Override
    public Component getName() {
        return new TranslatableComponent("spectatorMenu.team_teleport");
    }

    @Override
    public void renderIcon(float param0, int param1) {
        Minecraft.getInstance().getTextureManager().bind(SpectatorGui.SPECTATOR_LOCATION);
        GuiComponent.blit(0, 0, 16.0F, 0.0F, 16, 16, 256, 256);
    }

    @Override
    public boolean isEnabled() {
        for(SpectatorMenuItem var0 : this.items) {
            if (var0.isEnabled()) {
                return true;
            }
        }

        return false;
    }

    @OnlyIn(Dist.CLIENT)
    class TeamSelectionItem implements SpectatorMenuItem {
        private final PlayerTeam team;
        private final ResourceLocation location;
        private final List<PlayerInfo> players;

        public TeamSelectionItem(PlayerTeam param0) {
            this.team = param0;
            this.players = Lists.newArrayList();

            for(String param1 : param0.getPlayers()) {
                PlayerInfo var0 = Minecraft.getInstance().getConnection().getPlayerInfo(param1);
                if (var0 != null) {
                    this.players.add(var0);
                }
            }

            if (this.players.isEmpty()) {
                this.location = DefaultPlayerSkin.getDefaultSkin();
            } else {
                String var1 = this.players.get(new Random().nextInt(this.players.size())).getProfile().getName();
                this.location = AbstractClientPlayer.getSkinLocation(var1);
                AbstractClientPlayer.registerSkinTexture(this.location, var1);
            }

        }

        @Override
        public void selectItem(SpectatorMenu param0) {
            param0.selectCategory(new TeleportToPlayerMenuCategory(this.players));
        }

        @Override
        public Component getName() {
            return this.team.getDisplayName();
        }

        @Override
        public void renderIcon(float param0, int param1) {
            Integer var0 = this.team.getColor().getColor();
            if (var0 != null) {
                float var1 = (float)(var0 >> 16 & 0xFF) / 255.0F;
                float var2 = (float)(var0 >> 8 & 0xFF) / 255.0F;
                float var3 = (float)(var0 & 0xFF) / 255.0F;
                GuiComponent.fill(1, 1, 15, 15, Mth.color(var1 * param0, var2 * param0, var3 * param0) | param1 << 24);
            }

            Minecraft.getInstance().getTextureManager().bind(this.location);
            RenderSystem.color4f(param0, param0, param0, (float)param1 / 255.0F);
            GuiComponent.blit(2, 2, 12, 12, 8.0F, 8.0F, 8, 8, 64, 64);
            GuiComponent.blit(2, 2, 12, 12, 40.0F, 8.0F, 8, 8, 64, 64);
        }

        @Override
        public boolean isEnabled() {
            return !this.players.isEmpty();
        }
    }
}
