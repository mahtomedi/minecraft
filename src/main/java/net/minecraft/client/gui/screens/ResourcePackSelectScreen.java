package net.minecraft.client.gui.screens;

import java.io.File;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.resources.ResourcePack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResourcePackSelectScreen extends PackSelectionScreen {
    public ResourcePackSelectScreen(Screen param0, PackRepository<ResourcePack> param1, Consumer<PackRepository<ResourcePack>> param2, File param3) {
        super(
            param0,
            new TranslatableComponent("resourcePack.title"),
            param2x -> new PackSelectionModel<>(param2x, ResourcePack::bindIcon, param1, param2),
            param3
        );
    }
}
