package net.minecraft.client.gui.screens.resourcepacks;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.resourcepacks.lists.AvailableResourcePackList;
import net.minecraft.client.gui.screens.resourcepacks.lists.ResourcePackList;
import net.minecraft.client.gui.screens.resourcepacks.lists.SelectedResourcePackList;
import net.minecraft.client.resources.UnopenedResourcePack;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResourcePackSelectScreen extends OptionsSubScreen {
    private AvailableResourcePackList availableResourcePackList;
    private SelectedResourcePackList selectedResourcePackList;
    private boolean changed;

    public ResourcePackSelectScreen(Screen param0, Options param1) {
        super(param0, param1, new TranslatableComponent("resourcePack.title"));
    }

    @Override
    protected void init() {
        this.addButton(
            new Button(
                this.width / 2 - 154,
                this.height - 48,
                150,
                20,
                new TranslatableComponent("resourcePack.openFolder"),
                param0 -> Util.getPlatform().openFile(this.minecraft.getResourcePackDirectory())
            )
        );
        PackRepository<UnopenedResourcePack> var0 = this.minecraft.getResourcePackRepository();
        this.addButton(new Button(this.width / 2 + 4, this.height - 48, 150, 20, CommonComponents.GUI_DONE, param1 -> {
            if (this.changed) {
                List<String> var0x = Lists.newArrayList();

                for(ResourcePackList.ResourcePackEntry var1x : this.selectedResourcePackList.children()) {
                    var0x.add(var1x.getResourcePack().getId());
                }

                Collections.reverse(var0x);
                var0.setSelected(var0x);
                this.options.resourcePacks.clear();
                this.options.incompatibleResourcePacks.clear();

                for(UnopenedResourcePack var2x : var0.getSelectedPacks()) {
                    if (!var2x.isFixedPosition()) {
                        this.options.resourcePacks.add(var2x.getId());
                        if (!var2x.getCompatibility().isCompatible()) {
                            this.options.incompatibleResourcePacks.add(var2x.getId());
                        }
                    }
                }

                this.options.save();
                this.minecraft.setScreen(this.lastScreen);
                this.minecraft.reloadResourcePacks();
            } else {
                this.minecraft.setScreen(this.lastScreen);
            }

        }));
        AvailableResourcePackList var1 = this.availableResourcePackList;
        SelectedResourcePackList var2 = this.selectedResourcePackList;
        this.availableResourcePackList = new AvailableResourcePackList(this.minecraft, 200, this.height);
        this.availableResourcePackList.setLeftPos(this.width / 2 - 4 - 200);
        if (var1 != null) {
            this.availableResourcePackList.children().addAll(var1.children());
        }

        this.children.add(this.availableResourcePackList);
        this.selectedResourcePackList = new SelectedResourcePackList(this.minecraft, 200, this.height);
        this.selectedResourcePackList.setLeftPos(this.width / 2 + 4);
        if (var2 != null) {
            var2.children().forEach(param0 -> {
                this.selectedResourcePackList.children().add(param0);
                param0.updateParentList(this.selectedResourcePackList);
            });
        }

        this.children.add(this.selectedResourcePackList);
        if (!this.changed) {
            this.availableResourcePackList.children().clear();
            this.selectedResourcePackList.children().clear();
            var0.reload();
            List<UnopenedResourcePack> var3 = Lists.newArrayList(var0.getAvailablePacks());
            var3.removeAll(var0.getSelectedPacks());

            for(UnopenedResourcePack var4 : var3) {
                this.availableResourcePackList.addResourcePackEntry(new ResourcePackList.ResourcePackEntry(this.availableResourcePackList, this, var4));
            }

            for(UnopenedResourcePack var5 : Lists.reverse(Lists.newArrayList(var0.getSelectedPacks()))) {
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
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderDirtBackground(0);
        this.availableResourcePackList.render(param0, param1, param2, param3);
        this.selectedResourcePackList.render(param0, param1, param2, param3);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 16, 16777215);
        this.drawCenteredString(param0, this.font, I18n.get("resourcePack.folderInfo"), this.width / 2 - 77, this.height - 26, 8421504);
        super.render(param0, param1, param2, param3);
    }

    public void setChanged() {
        this.changed = true;
    }
}
