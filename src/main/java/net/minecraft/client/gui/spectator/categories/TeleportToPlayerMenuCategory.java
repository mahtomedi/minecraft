package net.minecraft.client.gui.spectator.categories;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.PlayerMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TeleportToPlayerMenuCategory implements SpectatorMenuCategory, SpectatorMenuItem {
    private static final Ordering<PlayerInfo> PROFILE_ORDER = Ordering.from(
        (param0, param1) -> ComparisonChain.start().compare(param0.getProfile().getId(), param1.getProfile().getId()).result()
    );
    private final List<SpectatorMenuItem> items = Lists.newArrayList();

    public TeleportToPlayerMenuCategory() {
        this(PROFILE_ORDER.sortedCopy(Minecraft.getInstance().getConnection().getOnlinePlayers()));
    }

    public TeleportToPlayerMenuCategory(Collection<PlayerInfo> param0) {
        for(PlayerInfo var0 : PROFILE_ORDER.sortedCopy(param0)) {
            if (var0.getGameMode() != GameType.SPECTATOR) {
                this.items.add(new PlayerMenuItem(var0.getProfile()));
            }
        }

    }

    @Override
    public List<SpectatorMenuItem> getItems() {
        return this.items;
    }

    @Override
    public Component getPrompt() {
        return new TranslatableComponent("spectatorMenu.teleport.prompt");
    }

    @Override
    public void selectItem(SpectatorMenu param0) {
        param0.selectCategory(this);
    }

    @Override
    public Component getName() {
        return new TranslatableComponent("spectatorMenu.teleport");
    }

    @Override
    public void renderIcon(float param0, int param1) {
        Minecraft.getInstance().getTextureManager().bind(SpectatorGui.SPECTATOR_LOCATION);
        GuiComponent.blit(0, 0, 0.0F, 0.0F, 16, 16, 256, 256);
    }

    @Override
    public boolean isEnabled() {
        return !this.items.isEmpty();
    }
}
