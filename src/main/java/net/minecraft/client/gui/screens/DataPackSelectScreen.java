package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.DataPackConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DataPackSelectScreen extends PackSelectionScreen {
    private static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");

    public DataPackSelectScreen(Screen param0, final DataPackConfig param1, final BiConsumer<DataPackConfig, PackRepository<Pack>> param2, final File param3) {
        super(
            param0,
            new TranslatableComponent("dataPack.title"),
            new Function<Runnable, PackSelectionModel<?>>() {
                private DataPackConfig workingConfig = param1;
                private final PackRepository<Pack> repository = new PackRepository<>(
                    Pack::new, new ServerPacksSource(), new FolderRepositorySource(param3, PackSource.DEFAULT)
                );
    
                public PackSelectionModel<?> apply(Runnable param0) {
                    this.repository.reload();
                    List<String> var0 = this.workingConfig.getEnabled();
                    List<Pack> var1 = DataPackSelectScreen.getPacksByName(this.repository, var0.stream());
                    List<Pack> var2 = DataPackSelectScreen.getPacksByName(
                        this.repository, this.repository.getAvailableIds().stream().filter(param1xx -> !var0.contains(param1xx))
                    );
                    return new PackSelectionModel<>(
                        param0,
                        (param0x, param1xx) -> param1xx.bind(DataPackSelectScreen.DEFAULT_ICON),
                        Lists.reverse(var1),
                        var2,
                        (param1xx, param2xx, param3xx) -> {
                            List<String> var0x = Lists.reverse(param1xx).stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
                            List<String> var1x = param2xx.stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
                            this.workingConfig = new DataPackConfig(var0x, var1x);
                            if (!param3xx) {
                                this.repository.setSelected(var0x);
                                param2.accept(this.workingConfig, this.repository);
                            }
        
                        }
                    );
                }
            },
            param1x -> param3
        );
    }

    private static List<Pack> getPacksByName(PackRepository<Pack> param0, Stream<String> param1) {
        return param1.map(param0::getPack).filter(Objects::nonNull).collect(ImmutableList.toImmutableList());
    }
}
