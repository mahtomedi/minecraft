package net.minecraft.client.gui.spectator.categories;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
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
    private static final ResourceLocation TELEPORT_TO_TEAM_SPRITE = new ResourceLocation("spectator/teleport_to_team");
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
    public void renderIcon(GuiGraphics param0, float param1, int param2) {
        param0.blitSprite(TELEPORT_TO_TEAM_SPRITE, 0, 0, 16, 16);
    }

    @Override
    public boolean isEnabled() {
        return !this.items.isEmpty();
    }

    @OnlyIn(Dist.CLIENT)
    static class TeamSelectionItem implements SpectatorMenuItem {
        private final PlayerTeam team;
        private final Supplier<PlayerSkin> iconSkin;
        private final List<PlayerInfo> players;

        private TeamSelectionItem(PlayerTeam param0, List<PlayerInfo> param1, Supplier<PlayerSkin> param2) {
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
                Supplier<PlayerSkin> var4 = param0.getSkinManager().lookupInsecure(var3);
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
        public void renderIcon(GuiGraphics param0, float param1, int param2) {
            Integer var0 = this.team.getColor().getColor();
            if (var0 != null) {
                float var1 = (float)(var0 >> 16 & 0xFF) / 255.0F;
                float var2 = (float)(var0 >> 8 & 0xFF) / 255.0F;
                float var3 = (float)(var0 & 0xFF) / 255.0F;
                param0.fill(1, 1, 15, 15, Mth.color(var1 * param1, var2 * param1, var3 * param1) | param2 << 24);
            }

            param0.setColor(param1, param1, param1, (float)param2 / 255.0F);
            PlayerFaceRenderer.draw(param0, this.iconSkin.get(), 2, 2, 12);
            param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
