package net.minecraft.client.gui.screens;

import java.io.File;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DataPackSelectScreen extends PackSelectionScreen {
    private static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");

    public DataPackSelectScreen(Screen param0, PackRepository<Pack> param1, Consumer<PackRepository<Pack>> param2, File param3) {
        super(
            param0,
            new TranslatableComponent("dataPack.title"),
            param2x -> new PackSelectionModel<>(param2x, (param0x, param1x) -> param1x.bind(DEFAULT_ICON), param1, param2),
            param3
        );
    }
}
