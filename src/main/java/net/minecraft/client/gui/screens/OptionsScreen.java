package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.Difficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionsScreen extends Screen {
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
        int var0 = 0;

        for(OptionInstance<?> var1 : new OptionInstance[]{this.options.fov()}) {
            int var2 = this.width / 2 - 155 + var0 % 2 * 160;
            int var3 = this.height / 6 - 12 + 24 * (var0 >> 1);
            this.addRenderableWidget(var1.createButton(this.minecraft.options, var2, var3, 150));
            ++var0;
        }

        if (this.minecraft.level != null && this.minecraft.hasSingleplayerServer()) {
            this.difficultyButton = this.addRenderableWidget(createDifficultyButton(var0, this.width, this.height, "options.difficulty", this.minecraft));
            if (!this.minecraft.level.getLevelData().isHardcore()) {
                this.difficultyButton.setWidth(this.difficultyButton.getWidth() - 20);
                this.lockButton = this.addRenderableWidget(
                    new LockIconButton(
                        this.difficultyButton.getX() + this.difficultyButton.getWidth(),
                        this.difficultyButton.getY(),
                        param0 -> this.minecraft
                                .setScreen(
                                    new ConfirmScreen(
                                        this::lockCallback,
                                        Component.translatable("difficulty.lock.title"),
                                        Component.translatable("difficulty.lock.question", this.minecraft.level.getLevelData().getDifficulty().getDisplayName())
                                    )
                                )
                    )
                );
                this.lockButton.setLocked(this.minecraft.level.getLevelData().isDifficultyLocked());
                this.lockButton.active = !this.lockButton.isLocked();
                this.difficultyButton.active = !this.lockButton.isLocked();
            } else {
                this.difficultyButton.active = false;
            }
        } else {
            this.addRenderableWidget(
                Button.builder(Component.translatable("options.online"), param0 -> this.minecraft.setScreen(new OnlineOptionsScreen(this, this.options)))
                    .bounds(this.width / 2 + 5, this.height / 6 - 12 + 24 * (var0 >> 1), 150, 20)
                    .build()
            );
        }

        this.addRenderableWidget(
            Button.builder(
                    Component.translatable("options.skinCustomisation"), param0 -> this.minecraft.setScreen(new SkinCustomizationScreen(this, this.options))
                )
                .bounds(this.width / 2 - 155, this.height / 6 + 48 - 6, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("options.sounds"), param0 -> this.minecraft.setScreen(new SoundOptionsScreen(this, this.options)))
                .bounds(this.width / 2 + 5, this.height / 6 + 48 - 6, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("options.video"), param0 -> this.minecraft.setScreen(new VideoSettingsScreen(this, this.options)))
                .bounds(this.width / 2 - 155, this.height / 6 + 72 - 6, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("options.controls"), param0 -> this.minecraft.setScreen(new ControlsScreen(this, this.options)))
                .bounds(this.width / 2 + 5, this.height / 6 + 72 - 6, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(
                    Component.translatable("options.language"),
                    param0 -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.options, this.minecraft.getLanguageManager()))
                )
                .bounds(this.width / 2 - 155, this.height / 6 + 96 - 6, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("options.chat.title"), param0 -> this.minecraft.setScreen(new ChatOptionsScreen(this, this.options)))
                .bounds(this.width / 2 + 5, this.height / 6 + 96 - 6, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(
                    Component.translatable("options.resourcepack"),
                    param0 -> this.minecraft
                            .setScreen(
                                new PackSelectionScreen(
                                    this,
                                    this.minecraft.getResourcePackRepository(),
                                    this::updatePackList,
                                    this.minecraft.getResourcePackDirectory(),
                                    Component.translatable("resourcePack.title")
                                )
                            )
                )
                .bounds(this.width / 2 - 155, this.height / 6 + 120 - 6, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(
                    Component.translatable("options.accessibility.title"),
                    param0 -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.options))
                )
                .bounds(this.width / 2 + 5, this.height / 6 + 120 - 6, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 - 100, this.height / 6 + 168, 200, 20)
                .build()
        );
    }

    public static CycleButton<Difficulty> createDifficultyButton(int param0, int param1, int param2, String param3, Minecraft param4) {
        return CycleButton.builder(Difficulty::getDisplayName)
            .withValues(Difficulty.values())
            .withInitialValue(param4.level.getDifficulty())
            .create(
                param1 / 2 - 155 + param0 % 2 * 160,
                param2 / 6 - 12 + 24 * (param0 >> 1),
                150,
                20,
                Component.translatable(param3),
                (param1x, param2x) -> param4.getConnection().send(new ServerboundChangeDifficultyPacket(param2x))
            );
    }

    private void updatePackList(PackRepository param0) {
        List<String> var0 = ImmutableList.copyOf(this.options.resourcePacks);
        this.options.resourcePacks.clear();
        this.options.incompatibleResourcePacks.clear();

        for(Pack var1 : param0.getSelectedPacks()) {
            if (!var1.isFixedPosition()) {
                this.options.resourcePacks.add(var1.getId());
                if (!var1.getCompatibility().isCompatible()) {
                    this.options.incompatibleResourcePacks.add(var1.getId());
                }
            }
        }

        this.options.save();
        List<String> var2 = ImmutableList.copyOf(this.options.resourcePacks);
        if (!var2.equals(var0)) {
            this.minecraft.reloadResourcePacks();
        }

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
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 15, 16777215);
        super.render(param0, param1, param2, param3);
    }
}
