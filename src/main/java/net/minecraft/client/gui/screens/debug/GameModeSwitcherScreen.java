package net.minecraft.client.gui.screens.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameModeSwitcherScreen extends Screen {
    static final ResourceLocation SLOT_SPRITE = new ResourceLocation("gamemode_switcher/slot");
    static final ResourceLocation SELECTION_SPRITE = new ResourceLocation("gamemode_switcher/selection");
    private static final ResourceLocation GAMEMODE_SWITCHER_LOCATION = new ResourceLocation("textures/gui/container/gamemode_switcher.png");
    private static final int SPRITE_SHEET_WIDTH = 128;
    private static final int SPRITE_SHEET_HEIGHT = 128;
    private static final int SLOT_AREA = 26;
    private static final int SLOT_PADDING = 5;
    private static final int SLOT_AREA_PADDED = 31;
    private static final int HELP_TIPS_OFFSET_Y = 5;
    private static final int ALL_SLOTS_WIDTH = GameModeSwitcherScreen.GameModeIcon.values().length * 31 - 5;
    private static final Component SELECT_KEY = Component.translatable(
        "debug.gamemodes.select_next", Component.translatable("debug.gamemodes.press_f4").withStyle(ChatFormatting.AQUA)
    );
    private final GameModeSwitcherScreen.GameModeIcon previousHovered;
    private GameModeSwitcherScreen.GameModeIcon currentlyHovered;
    private int firstMouseX;
    private int firstMouseY;
    private boolean setFirstMousePos;
    private final List<GameModeSwitcherScreen.GameModeSlot> slots = Lists.newArrayList();

    public GameModeSwitcherScreen() {
        super(GameNarrator.NO_TITLE);
        this.previousHovered = GameModeSwitcherScreen.GameModeIcon.getFromGameType(this.getDefaultSelected());
        this.currentlyHovered = this.previousHovered;
    }

    private GameType getDefaultSelected() {
        MultiPlayerGameMode var0 = Minecraft.getInstance().gameMode;
        GameType var1 = var0.getPreviousPlayerMode();
        if (var1 != null) {
            return var1;
        } else {
            return var0.getPlayerMode() == GameType.CREATIVE ? GameType.SURVIVAL : GameType.CREATIVE;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.currentlyHovered = this.previousHovered;

        for(int var0 = 0; var0 < GameModeSwitcherScreen.GameModeIcon.VALUES.length; ++var0) {
            GameModeSwitcherScreen.GameModeIcon var1 = GameModeSwitcherScreen.GameModeIcon.VALUES[var0];
            this.slots.add(new GameModeSwitcherScreen.GameModeSlot(var1, this.width / 2 - ALL_SLOTS_WIDTH / 2 + var0 * 31, this.height / 2 - 31));
        }

    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        if (!this.checkToClose()) {
            param0.pose().pushPose();
            RenderSystem.enableBlend();
            int var0 = this.width / 2 - 62;
            int var1 = this.height / 2 - 31 - 27;
            param0.blit(GAMEMODE_SWITCHER_LOCATION, var0, var1, 0.0F, 0.0F, 125, 75, 128, 128);
            param0.pose().popPose();
            super.render(param0, param1, param2, param3);
            param0.drawCenteredString(this.font, this.currentlyHovered.getName(), this.width / 2, this.height / 2 - 31 - 20, -1);
            param0.drawCenteredString(this.font, SELECT_KEY, this.width / 2, this.height / 2 + 5, 16777215);
            if (!this.setFirstMousePos) {
                this.firstMouseX = param1;
                this.firstMouseY = param2;
                this.setFirstMousePos = true;
            }

            boolean var2 = this.firstMouseX == param1 && this.firstMouseY == param2;

            for(GameModeSwitcherScreen.GameModeSlot var3 : this.slots) {
                var3.render(param0, param1, param2, param3);
                var3.setSelected(this.currentlyHovered == var3.icon);
                if (!var2 && var3.isHoveredOrFocused()) {
                    this.currentlyHovered = var3.icon;
                }
            }

        }
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
    }

    private void switchToHoveredGameMode() {
        switchToHoveredGameMode(this.minecraft, this.currentlyHovered);
    }

    private static void switchToHoveredGameMode(Minecraft param0, GameModeSwitcherScreen.GameModeIcon param1) {
        if (param0.gameMode != null && param0.player != null) {
            GameModeSwitcherScreen.GameModeIcon var0 = GameModeSwitcherScreen.GameModeIcon.getFromGameType(param0.gameMode.getPlayerMode());
            if (param0.player.hasPermissions(2) && param1 != var0) {
                param0.player.connection.sendUnsignedCommand(param1.getCommand());
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
        if (param0 == 293) {
            this.setFirstMousePos = false;
            this.currentlyHovered = this.currentlyHovered.getNext();
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
        CREATIVE(Component.translatable("gameMode.creative"), "gamemode creative", new ItemStack(Blocks.GRASS_BLOCK)),
        SURVIVAL(Component.translatable("gameMode.survival"), "gamemode survival", new ItemStack(Items.IRON_SWORD)),
        ADVENTURE(Component.translatable("gameMode.adventure"), "gamemode adventure", new ItemStack(Items.MAP)),
        SPECTATOR(Component.translatable("gameMode.spectator"), "gamemode spectator", new ItemStack(Items.ENDER_EYE));

        protected static final GameModeSwitcherScreen.GameModeIcon[] VALUES = values();
        private static final int ICON_AREA = 16;
        protected static final int ICON_TOP_LEFT = 5;
        final Component name;
        final String command;
        final ItemStack renderStack;

        private GameModeIcon(Component param0, String param1, ItemStack param2) {
            this.name = param0;
            this.command = param1;
            this.renderStack = param2;
        }

        void drawIcon(GuiGraphics param0, int param1, int param2) {
            param0.renderItem(this.renderStack, param1, param2);
        }

        Component getName() {
            return this.name;
        }

        String getCommand() {
            return this.command;
        }

        GameModeSwitcherScreen.GameModeIcon getNext() {
            return switch(this) {
                case CREATIVE -> SURVIVAL;
                case SURVIVAL -> ADVENTURE;
                case ADVENTURE -> SPECTATOR;
                case SPECTATOR -> CREATIVE;
            };
        }

        static GameModeSwitcherScreen.GameModeIcon getFromGameType(GameType param0) {
            return switch(param0) {
                case SPECTATOR -> SPECTATOR;
                case SURVIVAL -> SURVIVAL;
                case CREATIVE -> CREATIVE;
                case ADVENTURE -> ADVENTURE;
            };
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class GameModeSlot extends AbstractWidget {
        final GameModeSwitcherScreen.GameModeIcon icon;
        private boolean isSelected;

        public GameModeSlot(GameModeSwitcherScreen.GameModeIcon param1, int param2, int param3) {
            super(param2, param3, 26, 26, param1.getName());
            this.icon = param1;
        }

        @Override
        public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
            this.drawSlot(param0);
            this.icon.drawIcon(param0, this.getX() + 5, this.getY() + 5);
            if (this.isSelected) {
                this.drawSelection(param0);
            }

        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput param0) {
            this.defaultButtonNarrationText(param0);
        }

        @Override
        public boolean isHoveredOrFocused() {
            return super.isHoveredOrFocused() || this.isSelected;
        }

        public void setSelected(boolean param0) {
            this.isSelected = param0;
        }

        private void drawSlot(GuiGraphics param0) {
            param0.blitSprite(GameModeSwitcherScreen.SLOT_SPRITE, this.getX(), this.getY(), 26, 26);
        }

        private void drawSelection(GuiGraphics param0) {
            param0.blitSprite(GameModeSwitcherScreen.SELECTION_SPRITE, this.getX(), this.getY(), 26, 26);
        }
    }
}
