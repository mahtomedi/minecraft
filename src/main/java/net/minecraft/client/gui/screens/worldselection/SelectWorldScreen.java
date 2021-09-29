package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SelectWorldScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final Screen lastScreen;
    @Nullable
    private List<FormattedCharSequence> toolTip;
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
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, new TranslatableComponent("selectWorld.search"));
        this.searchBox.setResponder(param0 -> this.list.refreshList(() -> param0, false));
        this.list = new WorldSelectionList(this, this.minecraft, this.width, this.height, 48, this.height - 64, 36, () -> this.searchBox.getValue(), this.list);
        this.addWidget(this.searchBox);
        this.addWidget(this.list);
        this.selectButton = this.addRenderableWidget(
            new Button(
                this.width / 2 - 154,
                this.height - 52,
                150,
                20,
                new TranslatableComponent("selectWorld.select"),
                param0 -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::joinWorld)
            )
        );
        this.addRenderableWidget(
            new Button(
                this.width / 2 + 4,
                this.height - 52,
                150,
                20,
                new TranslatableComponent("selectWorld.create"),
                param0 -> this.minecraft.setScreen(CreateWorldScreen.create(this))
            )
        );
        this.renameButton = this.addRenderableWidget(
            new Button(
                this.width / 2 - 154,
                this.height - 28,
                72,
                20,
                new TranslatableComponent("selectWorld.edit"),
                param0 -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::editWorld)
            )
        );
        this.deleteButton = this.addRenderableWidget(
            new Button(
                this.width / 2 - 76,
                this.height - 28,
                72,
                20,
                new TranslatableComponent("selectWorld.delete"),
                param0 -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::deleteWorld)
            )
        );
        this.copyButton = this.addRenderableWidget(
            new Button(
                this.width / 2 + 4,
                this.height - 28,
                72,
                20,
                new TranslatableComponent("selectWorld.recreate"),
                param0 -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::recreateWorld)
            )
        );
        this.addRenderableWidget(
            new Button(this.width / 2 + 82, this.height - 28, 72, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
        this.updateButtonStatus(false);
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
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.toolTip = null;
        this.list.render(param0, param1, param2, param3);
        this.searchBox.render(param0, param1, param2, param3);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 8, 16777215);
        super.render(param0, param1, param2, param3);
        if (this.toolTip != null) {
            this.renderTooltip(param0, this.toolTip, param1, param2);
        }

    }

    public void setToolTip(List<FormattedCharSequence> param0) {
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
