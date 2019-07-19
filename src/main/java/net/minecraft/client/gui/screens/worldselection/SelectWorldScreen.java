package net.minecraft.client.gui.screens.worldselection;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SelectWorldScreen extends Screen {
    protected final Screen lastScreen;
    private String toolTip;
    private Button deleteButton;
    private Button selectButton;
    private Button renameButton;
    private Button copyButton;
    protected EditBox searchBox;
    private WorldSelectionList list;

    public SelectWorldScreen(Screen param0) {
        super(new TranslatableComponent("selectWorld.title"));
        this.lastScreen = param0;
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        return super.mouseScrolled(param0, param1, param2);
    }

    @Override
    public void tick() {
        this.searchBox.tick();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, I18n.get("selectWorld.search"));
        this.searchBox.setResponder(param0 -> this.list.refreshList(() -> param0, false));
        this.list = new WorldSelectionList(this, this.minecraft, this.width, this.height, 48, this.height - 64, 36, () -> this.searchBox.getValue(), this.list);
        this.children.add(this.searchBox);
        this.children.add(this.list);
        this.selectButton = this.addButton(
            new Button(
                this.width / 2 - 154,
                this.height - 52,
                150,
                20,
                I18n.get("selectWorld.select"),
                param0 -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::joinWorld)
            )
        );
        this.addButton(
            new Button(
                this.width / 2 + 4, this.height - 52, 150, 20, I18n.get("selectWorld.create"), param0 -> this.minecraft.setScreen(new CreateWorldScreen(this))
            )
        );
        this.renameButton = this.addButton(
            new Button(
                this.width / 2 - 154,
                this.height - 28,
                72,
                20,
                I18n.get("selectWorld.edit"),
                param0 -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::editWorld)
            )
        );
        this.deleteButton = this.addButton(
            new Button(
                this.width / 2 - 76,
                this.height - 28,
                72,
                20,
                I18n.get("selectWorld.delete"),
                param0 -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::deleteWorld)
            )
        );
        this.copyButton = this.addButton(
            new Button(
                this.width / 2 + 4,
                this.height - 28,
                72,
                20,
                I18n.get("selectWorld.recreate"),
                param0 -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::recreateWorld)
            )
        );
        this.addButton(new Button(this.width / 2 + 82, this.height - 28, 72, 20, I18n.get("gui.cancel"), param0 -> this.minecraft.setScreen(this.lastScreen)));
        this.updateButtonStatus(false);
        this.setInitialFocus(this.searchBox);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        return super.keyPressed(param0, param1, param2) ? true : this.searchBox.keyPressed(param0, param1, param2);
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        return this.searchBox.charTyped(param0, param1);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.toolTip = null;
        this.list.render(param0, param1, param2);
        this.searchBox.render(param0, param1, param2);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 8, 16777215);
        super.render(param0, param1, param2);
        if (this.toolTip != null) {
            this.renderTooltip(Lists.newArrayList(Splitter.on("\n").split(this.toolTip)), param0, param1);
        }

    }

    public void setToolTip(String param0) {
        this.toolTip = param0;
    }

    public void updateButtonStatus(boolean param0) {
        this.selectButton.active = param0;
        this.deleteButton.active = param0;
        this.renameButton.active = param0;
        this.copyButton.active = param0;
    }

    @Override
    public void removed() {
        if (this.list != null) {
            this.list.children().forEach(WorldSelectionList.WorldListEntry::close);
        }

    }
}
