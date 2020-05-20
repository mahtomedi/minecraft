package net.minecraft.client.gui.screens.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameModeSwitcherScreen extends Screen {
    private static final ResourceLocation GAMEMODE_SWITCHER_LOCATION = new ResourceLocation("textures/gui/container/gamemode_switcher.png");
    private static final int ALL_SLOTS_WIDTH = GameModeSwitcherScreen.GameModeIcon.values().length * 30 - 5;
    private final Optional<GameModeSwitcherScreen.GameModeIcon> previousHovered;
    private Optional<GameModeSwitcherScreen.GameModeIcon> currentlyHovered = Optional.empty();
    private int firstMouseX;
    private int firstMouseY;
    private boolean setFirstMousePos;
    private final List<GameModeSwitcherScreen.GameModeSlot> slots = Lists.newArrayList();

    public GameModeSwitcherScreen() {
        super(NarratorChatListener.NO_TITLE);
        this.previousHovered = GameModeSwitcherScreen.GameModeIcon.getFromGameType(Minecraft.getInstance().gameMode.getPrevPlayerMode());
    }

    @Override
    protected void init() {
        super.init();
        this.currentlyHovered = this.previousHovered.isPresent()
            ? this.previousHovered
            : GameModeSwitcherScreen.GameModeIcon.getFromGameType(this.minecraft.gameMode.getPlayerMode());

        for(int var0 = 0; var0 < GameModeSwitcherScreen.GameModeIcon.VALUES.length; ++var0) {
            GameModeSwitcherScreen.GameModeIcon var1 = GameModeSwitcherScreen.GameModeIcon.VALUES[var0];
            this.slots.add(new GameModeSwitcherScreen.GameModeSlot(var1, this.width / 2 - ALL_SLOTS_WIDTH / 2 + var0 * 30, this.height / 2 - 30));
        }

    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (!this.checkToClose()) {
            param0.pushPose();
            RenderSystem.enableBlend();
            this.minecraft.getTextureManager().bind(GAMEMODE_SWITCHER_LOCATION);
            int var0 = this.width / 2 - 62;
            int var1 = this.height / 2 - 30 - 27;
            blit(param0, var0, var1, 0.0F, 0.0F, 125, 75, 128, 128);
            param0.popPose();
            super.render(param0, param1, param2, param3);
            this.currentlyHovered
                .ifPresent(param1x -> this.drawCenteredString(param0, this.font, param1x.getName(), this.width / 2, this.height / 2 - 30 - 20, -1));
            int var2 = this.font.width(I18n.get("debug.gamemodes.press_f4"));
            this.drawKeyOption(param0, I18n.get("debug.gamemodes.press_f4"), I18n.get("debug.gamemodes.select_next"), 5, var2);
            if (!this.setFirstMousePos) {
                this.firstMouseX = param1;
                this.firstMouseY = param2;
                this.setFirstMousePos = true;
            }

            boolean var3 = this.firstMouseX == param1 && this.firstMouseY == param2;

            for(GameModeSwitcherScreen.GameModeSlot var4 : this.slots) {
                var4.render(param0, param1, param2, param3);
                this.currentlyHovered.ifPresent(param1x -> var4.setSelected(param1x == var4.icon));
                if (!var3 && var4.isHovered()) {
                    this.currentlyHovered = Optional.of(var4.icon);
                }
            }

        }
    }

    private void drawKeyOption(PoseStack param0, String param1, String param2, int param3, int param4) {
        int var0 = 5636095;
        int var1 = 16777215;
        this.drawString(param0, this.font, "[", this.width / 2 - param4 - 18, this.height / 2 + param3, 5636095);
        this.drawCenteredString(param0, this.font, param1, this.width / 2 - param4 / 2 - 10, this.height / 2 + param3, 5636095);
        this.drawCenteredString(param0, this.font, "]", this.width / 2 - 5, this.height / 2 + param3, 5636095);
        this.drawString(param0, this.font, param2, this.width / 2 + 5, this.height / 2 + param3, 16777215);
    }

    private void switchToHoveredGameMode() {
        switchToHoveredGameMode(this.minecraft, this.currentlyHovered);
    }

    private static void switchToHoveredGameMode(Minecraft param0, Optional<GameModeSwitcherScreen.GameModeIcon> param1) {
        if (param0.gameMode != null && param0.player != null && param1.isPresent()) {
            Optional<GameModeSwitcherScreen.GameModeIcon> var0 = GameModeSwitcherScreen.GameModeIcon.getFromGameType(param0.gameMode.getPlayerMode());
            GameModeSwitcherScreen.GameModeIcon var1 = param1.get();
            if (var0.isPresent() && param0.player.hasPermissions(2) && var1 != var0.get()) {
                param0.player.chat(var1.getCommand());
            }

        }
    }

    private boolean checkToClose() {
        if (!InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), 292)) {
            this.switchToHoveredGameMode();
            this.minecraft.setScreen(null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 293 && this.currentlyHovered.isPresent()) {
            this.setFirstMousePos = false;
            this.currentlyHovered = this.currentlyHovered.get().getNext();
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    static enum GameModeIcon {
        CREATIVE(new TranslatableComponent("gameMode.creative"), "/gamemode creative", new ItemStack(Blocks.GRASS_BLOCK)),
        SURVIVAL(new TranslatableComponent("gameMode.survival"), "/gamemode survival", new ItemStack(Items.IRON_SWORD)),
        ADVENTURE(new TranslatableComponent("gameMode.adventure"), "/gamemode adventure", new ItemStack(Items.MAP)),
        SPECTATOR(new TranslatableComponent("gameMode.spectator"), "/gamemode spectator", new ItemStack(Items.ENDER_EYE));

        protected static final GameModeSwitcherScreen.GameModeIcon[] VALUES = values();
        final Component name;
        final String command;
        final ItemStack renderStack;

        private GameModeIcon(Component param0, String param1, ItemStack param2) {
            this.name = param0;
            this.command = param1;
            this.renderStack = param2;
        }

        private void drawIcon(ItemRenderer param0, int param1, int param2) {
            param0.renderAndDecorateItem(this.renderStack, param1, param2);
        }

        private Component getName() {
            return this.name;
        }

        private String getCommand() {
            return this.command;
        }

        private Optional<GameModeSwitcherScreen.GameModeIcon> getNext() {
            switch(this) {
                case CREATIVE:
                    return Optional.of(SURVIVAL);
                case SURVIVAL:
                    return Optional.of(ADVENTURE);
                case ADVENTURE:
                    return Optional.of(SPECTATOR);
                default:
                    return Optional.of(CREATIVE);
            }
        }

        private static Optional<GameModeSwitcherScreen.GameModeIcon> getFromGameType(GameType param0) {
            switch(param0) {
                case SPECTATOR:
                    return Optional.of(SPECTATOR);
                case SURVIVAL:
                    return Optional.of(SURVIVAL);
                case CREATIVE:
                    return Optional.of(CREATIVE);
                case ADVENTURE:
                    return Optional.of(ADVENTURE);
                default:
                    return Optional.empty();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class GameModeSlot extends AbstractWidget {
        private final GameModeSwitcherScreen.GameModeIcon icon;
        private boolean isSelected;

        public GameModeSlot(GameModeSwitcherScreen.GameModeIcon param1, int param2, int param3) {
            super(param2, param3, 25, 25, param1.getName());
            this.icon = param1;
        }

        @Override
        public void renderButton(PoseStack param0, int param1, int param2, float param3) {
            Minecraft var0 = Minecraft.getInstance();
            this.drawSlot(param0, var0.getTextureManager());
            this.icon.drawIcon(GameModeSwitcherScreen.this.itemRenderer, this.x + 5, this.y + 5);
            if (this.isSelected) {
                this.drawSelection(param0, var0.getTextureManager());
            }

        }

        @Override
        public boolean isHovered() {
            return super.isHovered() || this.isSelected;
        }

        public void setSelected(boolean param0) {
            this.isSelected = param0;
            this.narrate();
        }

        private void drawSlot(PoseStack param0, TextureManager param1) {
            param1.bind(GameModeSwitcherScreen.GAMEMODE_SWITCHER_LOCATION);
            param0.pushPose();
            param0.translate((double)this.x, (double)this.y, 0.0);
            blit(param0, 0, 0, 0.0F, 75.0F, 25, 25, 128, 128);
            param0.popPose();
        }

        private void drawSelection(PoseStack param0, TextureManager param1) {
            param1.bind(GameModeSwitcherScreen.GAMEMODE_SWITCHER_LOCATION);
            param0.pushPose();
            param0.translate((double)this.x, (double)this.y, 0.0);
            blit(param0, 0, 0, 25.0F, 75.0F, 25, 25, 128, 128);
            param0.popPose();
        }
    }
}