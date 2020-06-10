package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.resources.ResourcePack;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.Difficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionsScreen extends Screen {
    private static final Option[] OPTION_SCREEN_OPTIONS = new Option[]{Option.FOV};
    private final Screen lastScreen;
    private final Options options;
    private Button difficultyButton;
    private LockIconButton lockButton;
    private Difficulty currentDifficulty;

    public OptionsScreen(Screen param0, Options param1) {
        super(new TranslatableComponent("options.title"));
        this.lastScreen = param0;
        this.options = param1;
    }

    @Override
    protected void init() {
        int var0 = 0;

        for(Option var1 : OPTION_SCREEN_OPTIONS) {
            int var2 = this.width / 2 - 155 + var0 % 2 * 160;
            int var3 = this.height / 6 - 12 + 24 * (var0 >> 1);
            this.addButton(var1.createButton(this.minecraft.options, var2, var3, 150));
            ++var0;
        }

        if (this.minecraft.level != null) {
            this.currentDifficulty = this.minecraft.level.getDifficulty();
            this.difficultyButton = this.addButton(
                new Button(
                    this.width / 2 - 155 + var0 % 2 * 160,
                    this.height / 6 - 12 + 24 * (var0 >> 1),
                    150,
                    20,
                    this.getDifficultyText(this.currentDifficulty),
                    param0 -> {
                        this.currentDifficulty = Difficulty.byId(this.currentDifficulty.getId() + 1);
                        this.minecraft.getConnection().send(new ServerboundChangeDifficultyPacket(this.currentDifficulty));
                        this.difficultyButton.setMessage(this.getDifficultyText(this.currentDifficulty));
                    }
                )
            );
            if (this.minecraft.hasSingleplayerServer() && !this.minecraft.level.getLevelData().isHardcore()) {
                this.difficultyButton.setWidth(this.difficultyButton.getWidth() - 20);
                this.lockButton = this.addButton(
                    new LockIconButton(
                        this.difficultyButton.x + this.difficultyButton.getWidth(),
                        this.difficultyButton.y,
                        param0 -> this.minecraft
                                .setScreen(
                                    new ConfirmScreen(
                                        this::lockCallback,
                                        new TranslatableComponent("difficulty.lock.title"),
                                        new TranslatableComponent(
                                            "difficulty.lock.question",
                                            new TranslatableComponent("options.difficulty." + this.minecraft.level.getLevelData().getDifficulty().getKey())
                                        )
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
            this.addButton(
                new OptionButton(
                    this.width / 2 - 155 + var0 % 2 * 160,
                    this.height / 6 - 12 + 24 * (var0 >> 1),
                    150,
                    20,
                    Option.REALMS_NOTIFICATIONS,
                    Option.REALMS_NOTIFICATIONS.getMessage(this.options),
                    param0 -> {
                        Option.REALMS_NOTIFICATIONS.toggle(this.options);
                        this.options.save();
                        param0.setMessage(Option.REALMS_NOTIFICATIONS.getMessage(this.options));
                    }
                )
            );
        }

        this.addButton(
            new Button(
                this.width / 2 - 155,
                this.height / 6 + 48 - 6,
                150,
                20,
                new TranslatableComponent("options.skinCustomisation"),
                param0 -> this.minecraft.setScreen(new SkinCustomizationScreen(this, this.options))
            )
        );
        this.addButton(
            new Button(
                this.width / 2 + 5,
                this.height / 6 + 48 - 6,
                150,
                20,
                new TranslatableComponent("options.sounds"),
                param0 -> this.minecraft.setScreen(new SoundOptionsScreen(this, this.options))
            )
        );
        this.addButton(
            new Button(
                this.width / 2 - 155,
                this.height / 6 + 72 - 6,
                150,
                20,
                new TranslatableComponent("options.video"),
                param0 -> this.minecraft.setScreen(new VideoSettingsScreen(this, this.options))
            )
        );
        this.addButton(
            new Button(
                this.width / 2 + 5,
                this.height / 6 + 72 - 6,
                150,
                20,
                new TranslatableComponent("options.controls"),
                param0 -> this.minecraft.setScreen(new ControlsScreen(this, this.options))
            )
        );
        this.addButton(
            new Button(
                this.width / 2 - 155,
                this.height / 6 + 96 - 6,
                150,
                20,
                new TranslatableComponent("options.language"),
                param0 -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.options, this.minecraft.getLanguageManager()))
            )
        );
        this.addButton(
            new Button(
                this.width / 2 + 5,
                this.height / 6 + 96 - 6,
                150,
                20,
                new TranslatableComponent("options.chat.title"),
                param0 -> this.minecraft.setScreen(new ChatOptionsScreen(this, this.options))
            )
        );
        this.addButton(
            new Button(
                this.width / 2 - 155,
                this.height / 6 + 120 - 6,
                150,
                20,
                new TranslatableComponent("options.resourcepack"),
                param0 -> this.minecraft
                        .setScreen(
                            new ResourcePackSelectScreen(
                                this, this.minecraft.getResourcePackRepository(), this::updatePackList, this.minecraft.getResourcePackDirectory()
                            )
                        )
            )
        );
        this.addButton(
            new Button(
                this.width / 2 + 5,
                this.height / 6 + 120 - 6,
                150,
                20,
                new TranslatableComponent("options.accessibility.title"),
                param0 -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.options))
            )
        );
        this.addButton(
            new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
    }

    private void updatePackList(PackRepository<ResourcePack> param0) {
        List<String> var0 = ImmutableList.copyOf(this.options.resourcePacks);
        this.options.resourcePacks.clear();
        this.options.incompatibleResourcePacks.clear();

        for(ResourcePack var1 : param0.getSelectedPacks()) {
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

    private Component getDifficultyText(Difficulty param0) {
        return new TranslatableComponent("options.difficulty").append(": ").append(param0.getDisplayName());
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
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 15, 16777215);
        super.render(param0, param1, param2, param3);
    }
}
