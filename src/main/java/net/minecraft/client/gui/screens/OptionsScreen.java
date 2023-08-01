package net.minecraft.client.gui.screens;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.telemetry.TelemetryInfoScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.Difficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionsScreen extends Screen {
    private static final Component SKIN_CUSTOMIZATION = Component.translatable("options.skinCustomisation");
    private static final Component SOUNDS = Component.translatable("options.sounds");
    private static final Component VIDEO = Component.translatable("options.video");
    private static final Component CONTROLS = Component.translatable("options.controls");
    private static final Component LANGUAGE = Component.translatable("options.language");
    private static final Component CHAT = Component.translatable("options.chat.title");
    private static final Component RESOURCEPACK = Component.translatable("options.resourcepack");
    private static final Component ACCESSIBILITY = Component.translatable("options.accessibility.title");
    private static final Component TELEMETRY = Component.translatable("options.telemetry");
    private static final Component CREDITS_AND_ATTRIBUTION = Component.translatable("options.credits_and_attribution");
    private static final int COLUMNS = 2;
    private final Screen lastScreen;
    private final Options options;
    private CycleButton<Difficulty> difficultyButton;
    private LockIconButton lockButton;

    public OptionsScreen(Screen param0, Options param1) {
        super(Component.translatable("options.title"));
        this.lastScreen = param0;
        this.options = param1;
    }

    @Override
    protected void init() {
        GridLayout var0 = new GridLayout();
        var0.defaultCellSetting().paddingHorizontal(5).paddingBottom(4).alignHorizontallyCenter();
        GridLayout.RowHelper var1 = var0.createRowHelper(2);
        var1.addChild(this.options.fov().createButton(this.minecraft.options, 0, 0, 150));
        var1.addChild(this.createOnlineButton());
        var1.addChild(SpacerElement.height(26), 2);
        var1.addChild(this.openScreenButton(SKIN_CUSTOMIZATION, () -> new SkinCustomizationScreen(this, this.options)));
        var1.addChild(this.openScreenButton(SOUNDS, () -> new SoundOptionsScreen(this, this.options)));
        var1.addChild(this.openScreenButton(VIDEO, () -> new VideoSettingsScreen(this, this.options)));
        var1.addChild(this.openScreenButton(CONTROLS, () -> new ControlsScreen(this, this.options)));
        var1.addChild(this.openScreenButton(LANGUAGE, () -> new LanguageSelectScreen(this, this.options, this.minecraft.getLanguageManager())));
        var1.addChild(this.openScreenButton(CHAT, () -> new ChatOptionsScreen(this, this.options)));
        var1.addChild(
            this.openScreenButton(
                RESOURCEPACK,
                () -> new PackSelectionScreen(
                        this.minecraft.getResourcePackRepository(),
                        this::applyPacks,
                        this.minecraft.getResourcePackDirectory(),
                        Component.translatable("resourcePack.title")
                    )
            )
        );
        var1.addChild(this.openScreenButton(ACCESSIBILITY, () -> new AccessibilityOptionsScreen(this, this.options)));
        var1.addChild(this.openScreenButton(TELEMETRY, () -> new TelemetryInfoScreen(this, this.options)));
        var1.addChild(this.openScreenButton(CREDITS_AND_ATTRIBUTION, () -> new CreditsAndAttributionScreen(this)));
        var1.addChild(
            Button.builder(CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(this.lastScreen)).width(200).build(),
            2,
            var1.newCellSettings().paddingTop(6)
        );
        var0.arrangeElements();
        FrameLayout.alignInRectangle(var0, 0, this.height / 6 - 12, this.width, this.height, 0.5F, 0.0F);
        var0.visitWidgets(this::addRenderableWidget);
    }

    private void applyPacks(PackRepository param0) {
        this.options.updateResourcePacks(param0);
        this.minecraft.setScreen(this);
    }

    private LayoutElement createOnlineButton() {
        if (this.minecraft.level != null && this.minecraft.hasSingleplayerServer()) {
            this.difficultyButton = createDifficultyButton(0, 0, "options.difficulty", this.minecraft);
            if (!this.minecraft.level.getLevelData().isHardcore()) {
                this.lockButton = new LockIconButton(
                    0,
                    0,
                    param0 -> this.minecraft
                            .setScreen(
                                new ConfirmScreen(
                                    this::lockCallback,
                                    Component.translatable("difficulty.lock.title"),
                                    Component.translatable("difficulty.lock.question", this.minecraft.level.getLevelData().getDifficulty().getDisplayName())
                                )
                            )
                );
                this.difficultyButton.setWidth(this.difficultyButton.getWidth() - this.lockButton.getWidth());
                this.lockButton.setLocked(this.minecraft.level.getLevelData().isDifficultyLocked());
                this.lockButton.active = !this.lockButton.isLocked();
                this.difficultyButton.active = !this.lockButton.isLocked();
                EqualSpacingLayout var0 = new EqualSpacingLayout(150, 0, EqualSpacingLayout.Orientation.HORIZONTAL);
                var0.addChild(this.difficultyButton);
                var0.addChild(this.lockButton);
                return var0;
            } else {
                this.difficultyButton.active = false;
                return this.difficultyButton;
            }
        } else {
            return Button.builder(
                    Component.translatable("options.online"),
                    param0 -> this.minecraft.setScreen(OnlineOptionsScreen.createOnlineOptionsScreen(this.minecraft, this, this.options))
                )
                .bounds(this.width / 2 + 5, this.height / 6 - 12 + 24, 150, 20)
                .build();
        }
    }

    public static CycleButton<Difficulty> createDifficultyButton(int param0, int param1, String param2, Minecraft param3) {
        return CycleButton.builder(Difficulty::getDisplayName)
            .withValues(Difficulty.values())
            .withInitialValue(param3.level.getDifficulty())
            .create(
                param0,
                param1,
                150,
                20,
                Component.translatable(param2),
                (param1x, param2x) -> param3.getConnection().send(new ServerboundChangeDifficultyPacket(param2x))
            );
    }

    private void lockCallback(boolean param0) {
        this.minecraft.setScreen(this);
        if (param0 && this.minecraft.level != null) {
            this.minecraft.getConnection().send(new ServerboundLockDifficultyPacket(true));
            this.lockButton.setLocked(true);
            this.lockButton.active = false;
            this.difficultyButton.active = false;
        }

    }

    @Override
    public void removed() {
        this.options.save();
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
    }

    private Button openScreenButton(Component param0, Supplier<Screen> param1) {
        return Button.builder(param0, param1x -> this.minecraft.setScreen(param1.get())).build();
    }
}
