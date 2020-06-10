package net.minecraft.client.gui.screens.packs;

import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class PackSelectionScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component DRAG_AND_DROP = new TranslatableComponent("pack.dropInfo").withStyle(ChatFormatting.DARK_GRAY);
    private static final Component DIRECTORY_BUTTON_TOOLTIP = new TranslatableComponent("pack.folderInfo");
    private final PackSelectionModel<?> model;
    private final Screen lastScreen;
    private boolean shouldCommit;
    private TransferableSelectionList availablePackList;
    private TransferableSelectionList selectedPackList;
    private final File packDir;
    private Button doneButton;

    public PackSelectionScreen(Screen param0, TranslatableComponent param1, Function<Runnable, PackSelectionModel<?>> param2, File param3) {
        super(param1);
        this.lastScreen = param0;
        this.model = param2.apply(this::populateLists);
        this.packDir = param3;
    }

    @Override
    public void removed() {
        if (this.shouldCommit) {
            this.shouldCommit = false;
            this.model.commit();
        }

    }

    @Override
    public void onClose() {
        this.shouldCommit = true;
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    protected void init() {
        this.doneButton = this.addButton(new Button(this.width / 2 + 4, this.height - 48, 150, 20, CommonComponents.GUI_DONE, param0 -> this.onClose()));
        this.addButton(
            new Button(
                this.width / 2 - 154,
                this.height - 48,
                150,
                20,
                new TranslatableComponent("pack.openFolder"),
                param0 -> Util.getPlatform().openFile(this.packDir),
                (param0, param1, param2, param3) -> this.renderTooltip(param1, DIRECTORY_BUTTON_TOOLTIP, param2, param3)
            )
        );
        this.availablePackList = new TransferableSelectionList(this.minecraft, 200, this.height, new TranslatableComponent("pack.available.title"));
        this.availablePackList.setLeftPos(this.width / 2 - 4 - 200);
        this.children.add(this.availablePackList);
        this.selectedPackList = new TransferableSelectionList(this.minecraft, 200, this.height, new TranslatableComponent("pack.selected.title"));
        this.selectedPackList.setLeftPos(this.width / 2 + 4);
        this.children.add(this.selectedPackList);
        this.populateLists();
    }

    private void populateLists() {
        this.updateList(this.selectedPackList, this.model.getSelected());
        this.updateList(this.availablePackList, this.model.getUnselected());
        this.doneButton.active = !this.selectedPackList.children().isEmpty();
    }

    private void updateList(TransferableSelectionList param0, Stream<PackSelectionModel.Entry> param1) {
        param0.children().clear();
        param1.forEach(param1x -> param0.children().add(new TransferableSelectionList.PackEntry(this.minecraft, param0, this, param1x)));
    }

    private void reload() {
        this.model.findNewPacks();
        this.populateLists();
        this.shouldCommit = true;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderDirtBackground(0);
        this.availablePackList.render(param0, param1, param2, param3);
        this.selectedPackList.render(param0, param1, param2, param3);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 8, 16777215);
        this.drawCenteredString(param0, this.font, DRAG_AND_DROP, this.width / 2, 20, 16777215);
        super.render(param0, param1, param2, param3);
    }

    protected static void copyPacks(Minecraft param0, List<Path> param1, Path param2) {
        MutableBoolean var0 = new MutableBoolean();
        param1.forEach(param2x -> {
            try (Stream<Path> var1x = Files.walk(param2x)) {
                var1x.forEach(param3 -> {
                    try {
                        Util.copyBetweenDirs(param2x.getParent(), param2, param3);
                    } catch (IOException var5) {
                        LOGGER.warn("Failed to copy datapack file  from {} to {}", param3, param2, var5);
                        var0.setTrue();
                    }

                });
            } catch (IOException var16) {
                LOGGER.warn("Failed to copy datapack file from {} to {}", param2x, param2);
                var0.setTrue();
            }

        });
        if (var0.isTrue()) {
            SystemToast.onPackCopyFailure(param0, param2.toString());
        }

    }

    @Override
    public void onFilesDrop(List<Path> param0) {
        String var0 = param0.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
        this.minecraft.setScreen(new ConfirmScreen(param1 -> {
            if (param1) {
                copyPacks(this.minecraft, param0, this.packDir.toPath());
                this.reload();
            }

            this.minecraft.setScreen(this);
        }, new TranslatableComponent("pack.dropConfirm"), new TextComponent(var0)));
    }
}
