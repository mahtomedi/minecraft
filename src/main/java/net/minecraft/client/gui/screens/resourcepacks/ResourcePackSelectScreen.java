package net.minecraft.client.gui.screens.resourcepacks;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.resourcepacks.lists.AvailableResourcePackList;
import net.minecraft.client.gui.screens.resourcepacks.lists.ResourcePackList;
import net.minecraft.client.gui.screens.resourcepacks.lists.SelectedResourcePackList;
import net.minecraft.client.resources.UnopenedResourcePack;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResourcePackSelectScreen extends Screen {
    private final Screen parentScreen;
    private AvailableResourcePackList availableResourcePackList;
    private SelectedResourcePackList selectedResourcePackList;
    private boolean changed;

    public ResourcePackSelectScreen(Screen param0) {
        super(new TranslatableComponent("resourcePack.title"));
        this.parentScreen = param0;
    }

    @Override
    protected void init() {
        this.addButton(
            new Button(
                this.width / 2 - 154,
                this.height - 48,
                150,
                20,
                I18n.get("resourcePack.openFolder"),
                param0 -> Util.getPlatform().openFile(this.minecraft.getResourcePackDirectory())
            )
        );
        this.addButton(new Button(this.width / 2 + 4, this.height - 48, 150, 20, I18n.get("gui.done"), param0 -> {
            if (this.changed) {
                List<UnopenedResourcePack> var0x = Lists.newArrayList();

                for(ResourcePackList.ResourcePackEntry var1x : this.selectedResourcePackList.children()) {
                    var0x.add(var1x.getResourcePack());
                }

                Collections.reverse(var0x);
                this.minecraft.getResourcePackRepository().setSelected(var0x);
                this.minecraft.options.resourcePacks.clear();
                this.minecraft.options.incompatibleResourcePacks.clear();

                for(UnopenedResourcePack var6x : var0x) {
                    if (!var6x.isFixedPosition()) {
                        this.minecraft.options.resourcePacks.add(var6x.getId());
                        if (!var6x.getCompatibility().isCompatible()) {
                            this.minecraft.options.incompatibleResourcePacks.add(var6x.getId());
                        }
                    }
                }

                this.minecraft.options.save();
                this.minecraft.setScreen(this.parentScreen);
                this.minecraft.reloadResourcePacks();
            } else {
                this.minecraft.setScreen(this.parentScreen);
            }

        }));
        AvailableResourcePackList var0 = this.availableResourcePackList;
        SelectedResourcePackList var1 = this.selectedResourcePackList;
        this.availableResourcePackList = new AvailableResourcePackList(this.minecraft, 200, this.height);
        this.availableResourcePackList.setLeftPos(this.width / 2 - 4 - 200);
        if (var0 != null) {
            this.availableResourcePackList.children().addAll(var0.children());
        }

        this.children.add(this.availableResourcePackList);
        this.selectedResourcePackList = new SelectedResourcePackList(this.minecraft, 200, this.height);
        this.selectedResourcePackList.setLeftPos(this.width / 2 + 4);
        if (var1 != null) {
            this.selectedResourcePackList.children().addAll(var1.children());
        }

        this.children.add(this.selectedResourcePackList);
        if (!this.changed) {
            this.availableResourcePackList.children().clear();
            this.selectedResourcePackList.children().clear();
            PackRepository<UnopenedResourcePack> var2 = this.minecraft.getResourcePackRepository();
            var2.reload();
            List<UnopenedResourcePack> var3 = Lists.newArrayList(var2.getAvailable());
            var3.removeAll(var2.getSelected());

            for(UnopenedResourcePack var4 : var3) {
                this.availableResourcePackList.addResourcePackEntry(new ResourcePackList.ResourcePackEntry(this.availableResourcePackList, this, var4));
            }

            for(UnopenedResourcePack var5 : Lists.reverse(Lists.newArrayList(var2.getSelected()))) {
                this.selectedResourcePackList.addResourcePackEntry(new ResourcePackList.ResourcePackEntry(this.selectedResourcePackList, this, var5));
            }
        }

    }

    public void select(ResourcePackList.ResourcePackEntry param0) {
        this.availableResourcePackList.children().remove(param0);
        param0.addToList(this.selectedResourcePackList);
        this.setChanged();
    }

    public void deselect(ResourcePackList.ResourcePackEntry param0) {
        this.selectedResourcePackList.children().remove(param0);
        this.availableResourcePackList.addResourcePackEntry(param0);
        this.setChanged();
    }

    public boolean isSelected(ResourcePackList.ResourcePackEntry param0) {
        return this.selectedResourcePackList.children().contains(param0);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderDirtBackground(0);
        this.availableResourcePackList.render(param0, param1, param2);
        this.selectedResourcePackList.render(param0, param1, param2);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 16, 16777215);
        this.drawCenteredString(this.font, I18n.get("resourcePack.folderInfo"), this.width / 2 - 77, this.height - 26, 8421504);
        super.render(param0, param1, param2);
    }

    public void setChanged() {
        this.changed = true;
    }
}
