package net.minecraft.client.gui.screens;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum SelectedGameMode {
    SURVIVAL(GameType.SURVIVAL),
    CREATIVE(GameType.CREATIVE),
    ADVENTURE(GameType.ADVENTURE),
    SPECTATOR(GameType.SPECTATOR);

    private final GameType gameType;
    private final Component displayName;

    private SelectedGameMode(GameType param0) {
        this.gameType = param0;
        this.displayName = new TranslatableComponent("selectWorld.gameMode." + param0.getName());
    }

    public GameType getGameType() {
        return this.gameType;
    }

    public Component getDisplayName() {
        return this.displayName;
    }
}
