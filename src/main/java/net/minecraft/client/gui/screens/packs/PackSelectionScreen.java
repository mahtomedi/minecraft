package net.minecraft.client.gui.screens.packs;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PackSelectionScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component DRAG_AND_DROP = new TranslatableComponent("pack.dropInfo").withStyle(ChatFormatting.DARK_GRAY);
    private static final Component DIRECTORY_BUTTON_TOOLTIP = new TranslatableComponent("pack.folderInfo");
    private static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");
    private final PackSelectionModel model;
    private final Screen lastScreen;
    @Nullable
    private PackSelectionScreen.Watcher watcher;
    private long ticksToReload;
    private TransferableSelectionList availablePackList;
    private TransferableSelectionList selectedPackList;
    private final File packDir;
    private Button doneButton;
    private final Map<String, ResourceLocation> packIcons = Maps.newHashMap();

    public PackSelectionScreen(Screen param0, PackRepository param1, Consumer<PackRepository> param2, File param3, Component param4) {
        super(param4);
        this.lastScreen = param0;
        this.model = new PackSelectionModel(this::populateLists, this::getPackIcon, param1, param2);
        this.packDir = param3;
        this.watcher = PackSelectionScreen.Watcher.create(param3);
    }

    @Override
    public void onClose() {
        this.model.commit();
        this.minecraft.setScreen(this.lastScreen);
        this.closeWatcher();
    }

    private void closeWatcher() {
        if (this.watcher != null) {
            try {
                this.watcher.close();
                this.watcher = null;
            } catch (Exception var2) {
            }
        }

    }

    @Override
    protected void init() {
        this.doneButton = this.addButton(new Button(this.width / 2 + 4, this.height - 48, 150, 20, CommonComponents.GUI_DONE, param0 -> this.onClose()));
        this.addButton(
            new Button(
                this.width / 2 - 154,
                this.height - 48,
                150,
                20,
                new TranslatableComponent("pack.openFolder"),
                param0 -> Util.getPlatform().openFile(this.packDir),
                (param0, param1, param2, param3) -> this.renderTooltip(param1, DIRECTORY_BUTTON_TOOLTIP, param2, param3)
            )
        );
        this.availablePackList = new TransferableSelectionList(this.minecraft, 200, this.height, new TranslatableComponent("pack.available.title"));
        this.availablePackList.setLeftPos(this.width / 2 - 4 - 200);
        this.children.add(this.availablePackList);
        this.selectedPackList = new TransferableSelectionList(this.minecraft, 200, this.height, new TranslatableComponent("pack.selected.title"));
        this.selectedPackList.setLeftPos(this.width / 2 + 4);
        this.children.add(this.selectedPackList);
        this.reload();
    }

    @Override
    public void tick() {
        if (this.watcher != null) {
            try {
                if (this.watcher.pollForChanges()) {
                    this.ticksToReload = 20L;
                }
            } catch (IOException var2) {
                LOGGER.warn("Failed to poll for directory {} changes, stopping", this.packDir);
                this.closeWatcher();
            }
        }

        if (this.ticksToReload > 0L && --this.ticksToReload == 0L) {
            this.reload();
        }

    }

    private void populateLists() {
        this.updateList(this.selectedPackList, this.model.getSelected());
        this.updateList(this.availablePackList, this.model.getUnselected());
        this.doneButton.active = !this.selectedPackList.children().isEmpty();
    }

    private void updateList(TransferableSelectionList param0, Stream<PackSelectionModel.Entry> param1) {
        param0.children().clear();
        param1.forEach(param1x -> param0.children().add(new TransferableSelectionList.PackEntry(this.minecraft, param0, this, param1x)));
    }

    private void reload() {
        this.model.findNewPacks();
        this.populateLists();
        this.ticksToReload = 0L;
        this.packIcons.clear();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderDirtBackground(0);
        this.availablePackList.render(param0, param1, param2, param3);
        this.selectedPackList.render(param0, param1, param2, param3);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 8, 16777215);
        drawCenteredString(param0, this.font, DRAG_AND_DROP, this.width / 2, 20, 16777215);
        super.render(param0, param1, param2, param3);
    }

    protected static void copyPacks(Minecraft param0, List<Path> param1, Path param2) {
        MutableBoolean var0 = new MutableBoolean();
        param1.forEach(param2x -> {
            try (Stream<Path> var1x = Files.walk(param2x)) {
                var1x.forEach(param3 -> {
                    try {
                        Util.copyBetweenDirs(param2x.getParent(), param2, param3);
                    } catch (IOException var5) {
                        LOGGER.warn("Failed to copy datapack file  from {} to {}", param3, param2, var5);
                        var0.setTrue();
                    }

                });
            } catch (IOException var16) {
                LOGGER.warn("Failed to copy datapack file from {} to {}", param2x, param2);
                var0.setTrue();
            }

        });
        if (var0.isTrue()) {
            SystemToast.onPackCopyFailure(param0, param2.toString());
        }

    }

    @Override
    public void onFilesDrop(List<Path> param0) {
        String var0 = param0.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
        this.minecraft.setScreen(new ConfirmScreen(param1 -> {
            if (param1) {
                copyPacks(this.minecraft, param0, this.packDir.toPath());
                this.reload();
            }

            this.minecraft.setScreen(this);
        }, new TranslatableComponent("pack.dropConfirm"), new TextComponent(var0)));
    }

    private ResourceLocation loadPackIcon(TextureManager param0, Pack param1) {
        try (
            PackResources var0 = param1.open();
            InputStream var1 = var0.getRootResource("pack.png");
        ) {
            String var2 = param1.getId();
            ResourceLocation var3 = new ResourceLocation(
                "minecraft", "pack/" + Util.sanitizeName(var2, ResourceLocation::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(var2) + "/icon"
            );
            NativeImage var4 = NativeImage.read(var1);
            param0.register(var3, new DynamicTexture(var4));
            return var3;
        } catch (FileNotFoundException var41) {
        } catch (Exception var42) {
            LOGGER.warn("Failed to load icon from pack {}", param1.getId(), var42);
        }

        return DEFAULT_ICON;
    }

    private ResourceLocation getPackIcon(Pack param0x) {
        return this.packIcons.computeIfAbsent(param0x.getId(), param1x -> this.loadPackIcon(this.minecraft.getTextureManager(), param0x));
    }

    @OnlyIn(Dist.CLIENT)
    static class Watcher implements AutoCloseable {
        private final WatchService watcher;
        private final Path packPath;

        public Watcher(File param0) throws IOException {
            this.packPath = param0.toPath();
            this.watcher = this.packPath.getFileSystem().newWatchService();

            try {
                this.watchDir(this.packPath);

                try (DirectoryStream<Path> var0 = Files.newDirectoryStream(this.packPath)) {
                    for(Path var1 : var0) {
                        if (Files.isDirectory(var1, LinkOption.NOFOLLOW_LINKS)) {
                            this.watchDir(var1);
                        }
                    }
                }

            } catch (Exception var16) {
                this.watcher.close();
                throw var16;
            }
        }

        @Nullable
        public static PackSelectionScreen.Watcher create(File param0) {
            try {
                return new PackSelectionScreen.Watcher(param0);
            } catch (IOException var2) {
                PackSelectionScreen.LOGGER.warn("Failed to initialize pack directory {} monitoring", param0, var2);
                return null;
            }
        }

        private void watchDir(Path param0) throws IOException {
            param0.register(this.watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        }

        public boolean pollForChanges() throws IOException {
            boolean var0 = false;

            WatchKey var1;
            while((var1 = this.watcher.poll()) != null) {
                for(WatchEvent<?> var3 : var1.pollEvents()) {
                    var0 = true;
                    if (var1.watchable() == this.packPath && var3.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path var4 = this.packPath.resolve((Path)var3.context());
                        if (Files.isDirectory(var4, LinkOption.NOFOLLOW_LINKS)) {
                            this.watchDir(var4);
                        }
                    }
                }

                var1.reset();
            }

            return var0;
        }

        @Override
        public void close() throws IOException {
            this.watcher.close();
        }
    }
}
