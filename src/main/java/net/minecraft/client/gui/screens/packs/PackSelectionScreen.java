package net.minecraft.client.gui.screens.packs;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackDetector;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PackSelectionScreen extends Screen {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int LIST_WIDTH = 200;
    private static final Component DRAG_AND_DROP = Component.translatable("pack.dropInfo").withStyle(ChatFormatting.GRAY);
    private static final Component DIRECTORY_BUTTON_TOOLTIP = Component.translatable("pack.folderInfo");
    private static final int RELOAD_COOLDOWN = 20;
    private static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");
    private final PackSelectionModel model;
    @Nullable
    private PackSelectionScreen.Watcher watcher;
    private long ticksToReload;
    private TransferableSelectionList availablePackList;
    private TransferableSelectionList selectedPackList;
    private final Path packDir;
    private Button doneButton;
    private final Map<String, ResourceLocation> packIcons = Maps.newHashMap();

    public PackSelectionScreen(PackRepository param0, Consumer<PackRepository> param1, Path param2, Component param3) {
        super(param3);
        this.model = new PackSelectionModel(this::populateLists, this::getPackIcon, param0, param1);
        this.packDir = param2;
        this.watcher = PackSelectionScreen.Watcher.create(param2);
    }

    @Override
    public void onClose() {
        this.model.commit();
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
        this.availablePackList = this.addRenderableWidget(
            new TransferableSelectionList(this.minecraft, this, 200, this.height, Component.translatable("pack.available.title"))
        );
        this.availablePackList.setX(this.width / 2 - 4 - 200);
        this.selectedPackList = this.addRenderableWidget(
            new TransferableSelectionList(this.minecraft, this, 200, this.height, Component.translatable("pack.selected.title"))
        );
        this.selectedPackList.setX(this.width / 2 + 4);
        this.addRenderableWidget(
            Button.builder(Component.translatable("pack.openFolder"), param0 -> Util.getPlatform().openUri(this.packDir.toUri()))
                .bounds(this.width / 2 - 154, this.height - 48, 150, 20)
                .tooltip(Tooltip.create(DIRECTORY_BUTTON_TOOLTIP))
                .build()
        );
        this.doneButton = this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, param0 -> this.onClose()).bounds(this.width / 2 + 4, this.height - 48, 150, 20).build()
        );
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
        TransferableSelectionList.PackEntry var0 = param0.getSelected();
        String var1 = var0 == null ? "" : var0.getPackId();
        param0.setSelected(null);
        param1.forEach(param2 -> {
            TransferableSelectionList.PackEntry var0x = new TransferableSelectionList.PackEntry(this.minecraft, param0, param2);
            param0.children().add(var0x);
            if (param2.getId().equals(var1)) {
                param0.setSelected(var0x);
            }

        });
    }

    public void updateFocus(TransferableSelectionList param0) {
        TransferableSelectionList var0 = this.selectedPackList == param0 ? this.availablePackList : this.selectedPackList;
        this.changeFocus(ComponentPath.path(var0.getFirstElement(), var0, this));
    }

    public void clearSelected() {
        this.selectedPackList.setSelected(null);
        this.availablePackList.setSelected(null);
    }

    private void reload() {
        this.model.findNewPacks();
        this.populateLists();
        this.ticksToReload = 0L;
        this.packIcons.clear();
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
        param0.drawCenteredString(this.font, DRAG_AND_DROP, this.width / 2, 20, 16777215);
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderDirtBackground(param0);
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
            } catch (IOException var8) {
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
        String var0 = extractPackNames(param0).collect(Collectors.joining(", "));
        this.minecraft
            .setScreen(
                new ConfirmScreen(
                    param1 -> {
                        if (param1) {
                            List<Path> var0x = new ArrayList<>(param0.size());
                            Set<Path> var1x = new HashSet<>(param0);
                            PackDetector<Path> var2x = new PackDetector<Path>(this.minecraft.directoryValidator()) {
                                protected Path createZipPack(Path param0) {
                                    return param0;
                                }
            
                                protected Path createDirectoryPack(Path param0) {
                                    return param0;
                                }
                            };
                            List<ForbiddenSymlinkInfo> var3 = new ArrayList<>();
            
                            for(Path var4 : param0) {
                                try {
                                    Path var5 = (Path)var2x.detectPackResources(var4, var3);
                                    if (var5 == null) {
                                        LOGGER.warn("Path {} does not seem like pack", var4);
                                    } else {
                                        var0x.add(var5);
                                        var1x.remove(var5);
                                    }
                                } catch (IOException var10) {
                                    LOGGER.warn("Failed to check {} for packs", var4, var10);
                                }
                            }
            
                            if (!var3.isEmpty()) {
                                this.minecraft.setScreen(NoticeWithLinkScreen.createPackSymlinkWarningScreen(() -> this.minecraft.setScreen(this)));
                                return;
                            }
            
                            if (!var0x.isEmpty()) {
                                copyPacks(this.minecraft, var0x, this.packDir);
                                this.reload();
                            }
            
                            if (!var1x.isEmpty()) {
                                String var7 = extractPackNames(var1x).collect(Collectors.joining(", "));
                                this.minecraft
                                    .setScreen(
                                        new AlertScreen(
                                            () -> this.minecraft.setScreen(this),
                                            Component.translatable("pack.dropRejected.title"),
                                            Component.translatable("pack.dropRejected.message", var7)
                                        )
                                    );
                                return;
                            }
                        }
            
                        this.minecraft.setScreen(this);
                    },
                    Component.translatable("pack.dropConfirm"),
                    Component.literal(var0)
                )
            );
    }

    private static Stream<String> extractPackNames(Collection<Path> param0) {
        return param0.stream().map(Path::getFileName).map(Path::toString);
    }

    private ResourceLocation loadPackIcon(TextureManager param0, Pack param1) {
        try {
            ResourceLocation var9;
            try (PackResources var0 = param1.open()) {
                IoSupplier<InputStream> var1 = var0.getRootResource("pack.png");
                if (var1 == null) {
                    return DEFAULT_ICON;
                }

                String var2 = param1.getId();
                ResourceLocation var3 = new ResourceLocation(
                    "minecraft", "pack/" + Util.sanitizeName(var2, ResourceLocation::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(var2) + "/icon"
                );

                try (InputStream var4 = var1.get()) {
                    NativeImage var5 = NativeImage.read(var4);
                    param0.register(var3, new DynamicTexture(var5));
                    var9 = var3;
                }
            }

            return var9;
        } catch (Exception var14) {
            LOGGER.warn("Failed to load icon from pack {}", param1.getId(), var14);
            return DEFAULT_ICON;
        }
    }

    private ResourceLocation getPackIcon(Pack param0x) {
        return this.packIcons.computeIfAbsent(param0x.getId(), param1x -> this.loadPackIcon(this.minecraft.getTextureManager(), param0x));
    }

    @OnlyIn(Dist.CLIENT)
    static class Watcher implements AutoCloseable {
        private final WatchService watcher;
        private final Path packPath;

        public Watcher(Path param0) throws IOException {
            this.packPath = param0;
            this.watcher = param0.getFileSystem().newWatchService();

            try {
                this.watchDir(param0);

                try (DirectoryStream<Path> var0 = Files.newDirectoryStream(param0)) {
                    for(Path var1 : var0) {
                        if (Files.isDirectory(var1, LinkOption.NOFOLLOW_LINKS)) {
                            this.watchDir(var1);
                        }
                    }
                }

            } catch (Exception var7) {
                this.watcher.close();
                throw var7;
            }
        }

        @Nullable
        public static PackSelectionScreen.Watcher create(Path param0) {
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
