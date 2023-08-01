package net.minecraft.client.gui.screens.worldselection;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SelectWorldScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final WorldOptions TEST_OPTIONS = new WorldOptions((long)"test1".hashCode(), true, false);
    protected final Screen lastScreen;
    private Button deleteButton;
    private Button selectButton;
    private Button renameButton;
    private Button copyButton;
    protected EditBox searchBox;
    private WorldSelectionList list;

    public SelectWorldScreen(Screen param0) {
        super(Component.translatable("selectWorld.title"));
        this.lastScreen = param0;
    }

    @Override
    protected void init() {
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, Component.translatable("selectWorld.search"));
        this.searchBox.setResponder(param0 -> this.list.updateFilter(param0));
        this.list = new WorldSelectionList(this, this.minecraft, this.width, this.height, 48, this.height - 64, 36, this.searchBox.getValue(), this.list);
        this.addWidget(this.searchBox);
        this.addWidget(this.list);
        this.selectButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("selectWorld.select"), param0 -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::joinWorld)
                )
                .bounds(this.width / 2 - 154, this.height - 52, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("selectWorld.create"), param0 -> CreateWorldScreen.openFresh(this.minecraft, this))
                .bounds(this.width / 2 + 4, this.height - 52, 150, 20)
                .build()
        );
        this.renameButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("selectWorld.edit"), param0 -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::editWorld)
                )
                .bounds(this.width / 2 - 154, this.height - 28, 72, 20)
                .build()
        );
        this.deleteButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("selectWorld.delete"),
                    param0 -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::deleteWorld)
                )
                .bounds(this.width / 2 - 76, this.height - 28, 72, 20)
                .build()
        );
        this.copyButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("selectWorld.recreate"),
                    param0 -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::recreateWorld)
                )
                .bounds(this.width / 2 + 4, this.height - 28, 72, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_BACK, param0 -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 + 82, this.height - 28, 72, 20)
                .build()
        );
        this.updateButtonStatus(false, false);
        this.setInitialFocus(this.searchBox);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        return super.keyPressed(param0, param1, param2) ? true : this.searchBox.keyPressed(param0, param1, param2);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        return this.searchBox.charTyped(param0, param1);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.list.render(param0, param1, param2, param3);
        this.searchBox.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
    }

    public void updateButtonStatus(boolean param0, boolean param1) {
        this.selectButton.active = param0;
        this.renameButton.active = param0;
        this.copyButton.active = param0;
        this.deleteButton.active = param1;
    }

    @Override
    public void removed() {
        if (this.list != null) {
            this.list.children().forEach(WorldSelectionList.Entry::close);
        }

    }
}
