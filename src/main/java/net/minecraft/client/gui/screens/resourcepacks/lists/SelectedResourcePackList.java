package net.minecraft.client.gui.screens.resourcepacks.lists;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SelectedResourcePackList extends ResourcePackList {
    public SelectedResourcePackList(Minecraft param0, int param1, int param2) {
        super(param0, param1, param2, new TranslatableComponent("resourcePack.selected.title"));
    }
}
