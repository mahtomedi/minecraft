package net.minecraft.client.gui.spectator.categories;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TeleportToTeamMenuCategory implements SpectatorMenuCategory, SpectatorMenuItem {
    private static final Component TELEPORT_TEXT = Component.translatable("spectatorMenu.team_teleport");
    private static final Component TELEPORT_PROMPT = Component.translatable("spectatorMenu.team_teleport.prompt");
    private final List<SpectatorMenuItem> items;

    public TeleportToTeamMenuCategory() {
        Minecraft var0 = Minecraft.getInstance();
        this.items = createTeamEntries(var0, var0.level.getScoreboard());
    }

    private static List<SpectatorMenuItem> createTeamEntries(Minecraft param0, Scoreboard param1) {
        return param1.getPlayerTeams().stream().flatMap(param1x -> TeleportToTeamMenuCategory.TeamSelectionItem.create(param0, param1x).stream()).toList();
    }

    @Override
    public List<SpectatorMenuItem> getItems() {
        return this.items;
    }

    @Override
    public Component getPrompt() {
        return TELEPORT_PROMPT;
    }

    @Override
    public void selectItem(SpectatorMenu param0) {
        param0.selectCategory(this);
    }

    @Override
    public Component getName() {
        return TELEPORT_TEXT;
    }

    @Override
    public void renderIcon(PoseStack param0, float param1, int param2) {
        RenderSystem.setShaderTexture(0, SpectatorGui.SPECTATOR_LOCATION);
        GuiComponent.blit(param0, 0, 0, 16.0F, 0.0F, 16, 16, 256, 256);
    }

    @Override
    public boolean isEnabled() {
        return !this.items.isEmpty();
    }

    @OnlyIn(Dist.CLIENT)
    static class TeamSelectionItem implements SpectatorMenuItem {
        private final PlayerTeam team;
        private final ResourceLocation iconSkin;
        private final List<PlayerInfo> players;

        private TeamSelectionItem(PlayerTeam param0, List<PlayerInfo> param1, ResourceLocation param2) {
            this.team = param0;
            this.players = param1;
            this.iconSkin = param2;
        }

        public static Optional<SpectatorMenuItem> create(Minecraft param0, PlayerTeam param1) {
            List<PlayerInfo> var0 = new ArrayList<>();

            for(String var1 : param1.getPlayers()) {
                PlayerInfo var2 = param0.getConnection().getPlayerInfo(var1);
                if (var2 != null && var2.getGameMode() != GameType.SPECTATOR) {
                    var0.add(var2);
                }
            }

            if (var0.isEmpty()) {
                return Optional.empty();
            } else {
                GameProfile var3 = var0.get(RandomSource.create().nextInt(var0.size())).getProfile();
                ResourceLocation var4 = param0.getSkinManager().getInsecureSkinLocation(var3);
                return Optional.of(new TeleportToTeamMenuCategory.TeamSelectionItem(param1, var0, var4));
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
        public void renderIcon(PoseStack param0, float param1, int param2) {
            Integer var0 = this.team.getColor().getColor();
            if (var0 != null) {
                float var1 = (float)(var0 >> 16 & 0xFF) / 255.0F;
                float var2 = (float)(var0 >> 8 & 0xFF) / 255.0F;
                float var3 = (float)(var0 & 0xFF) / 255.0F;
                GuiComponent.fill(param0, 1, 1, 15, 15, Mth.color(var1 * param1, var2 * param1, var3 * param1) | param2 << 24);
            }

            RenderSystem.setShaderTexture(0, this.iconSkin);
            RenderSystem.setShaderColor(param1, param1, param1, (float)param2 / 255.0F);
            PlayerFaceRenderer.draw(param0, 2, 2, 12);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
