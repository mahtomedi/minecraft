package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.resources.ResourcePack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResourcePackSelectScreen extends PackSelectionScreen {
    public ResourcePackSelectScreen(Screen param0, Options param1, PackRepository<ResourcePack> param2, Runnable param3) {
        super(param0, new TranslatableComponent("resourcePack.title"), param3x -> {
            param2.reload();
            List<ResourcePack> var0 = Lists.newArrayList(param2.getSelectedPacks());
            List<ResourcePack> var1x = Lists.newArrayList(param2.getAvailablePacks());
            var1x.removeAll(var0);
            return new PackSelectionModel<>(param3x, ResourcePack::bindIcon, Lists.reverse(var0), var1x, (param3xx, param4, param5) -> {
                List<ResourcePack> var0x = Lists.reverse(param3xx);
                List<String> var1xx = var0x.stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
                param2.setSelected(var1xx);
                param1.resourcePacks.clear();
                param1.incompatibleResourcePacks.clear();

                for(ResourcePack var2x : var0x) {
                    if (!var2x.isFixedPosition()) {
                        param1.resourcePacks.add(var2x.getId());
                        if (!var2x.getCompatibility().isCompatible()) {
                            param1.incompatibleResourcePacks.add(var2x.getId());
                        }
                    }
                }

                param1.save();
                if (!param5) {
                    param3.run();
                }

            });
        }, Minecraft::getResourcePackDirectory);
    }
}
