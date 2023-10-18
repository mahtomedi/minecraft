package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Mth;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class OptimizeWorldScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ToIntFunction<ResourceKey<Level>> DIMENSION_COLORS = Util.make(new Reference2IntOpenHashMap<>(), param0 -> {
        param0.put(Level.OVERWORLD, -13408734);
        param0.put(Level.NETHER, -10075085);
        param0.put(Level.END, -8943531);
        param0.defaultReturnValue(-2236963);
    });
    private final BooleanConsumer callback;
    private final WorldUpgrader upgrader;

    @Nullable
    public static OptimizeWorldScreen create(
        Minecraft param0, BooleanConsumer param1, DataFixer param2, LevelStorageSource.LevelStorageAccess param3, boolean param4
    ) {
        try {
            WorldOpenFlows var0 = param0.createWorldOpenFlows();
            PackRepository var1 = ServerPacksSource.createPackRepository(param3);

            OptimizeWorldScreen var10;
            try (WorldStem var2 = var0.loadWorldStem(param3.getDataTag(), false, var1)) {
                WorldData var3 = var2.worldData();
                RegistryAccess.Frozen var4 = var2.registries().compositeAccess();
                param3.saveDataTag(var4, var3);
                var10 = new OptimizeWorldScreen(param1, param2, param3, var3.getLevelSettings(), param4, var4.registryOrThrow(Registries.LEVEL_STEM));
            }

            return var10;
        } catch (Exception var13) {
            LOGGER.warn("Failed to load datapacks, can't optimize world", (Throwable)var13);
            return null;
        }
    }

    private OptimizeWorldScreen(
        BooleanConsumer param0,
        DataFixer param1,
        LevelStorageSource.LevelStorageAccess param2,
        LevelSettings param3,
        boolean param4,
        Registry<LevelStem> param5
    ) {
        super(Component.translatable("optimizeWorld.title", param3.levelName()));
        this.callback = param0;
        this.upgrader = new WorldUpgrader(param2, param1, param5, param4);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, param0 -> {
            this.upgrader.cancel();
            this.callback.accept(false);
        }).bounds(this.width / 2 - 100, this.height / 4 + 150, 200, 20).build());
    }

    @Override
    public void tick() {
        if (this.upgrader.isFinished()) {
            this.callback.accept(true);
        }

    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Override
    public void removed() {
        this.upgrader.cancel();
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
        int var0 = this.width / 2 - 150;
        int var1 = this.width / 2 + 150;
        int var2 = this.height / 4 + 100;
        int var3 = var2 + 10;
        param0.drawCenteredString(this.font, this.upgrader.getStatus(), this.width / 2, var2 - 9 - 2, 10526880);
        if (this.upgrader.getTotalChunks() > 0) {
            param0.fill(var0 - 1, var2 - 1, var1 + 1, var3 + 1, -16777216);
            param0.drawString(this.font, Component.translatable("optimizeWorld.info.converted", this.upgrader.getConverted()), var0, 40, 10526880);
            param0.drawString(this.font, Component.translatable("optimizeWorld.info.skipped", this.upgrader.getSkipped()), var0, 40 + 9 + 3, 10526880);
            param0.drawString(this.font, Component.translatable("optimizeWorld.info.total", this.upgrader.getTotalChunks()), var0, 40 + (9 + 3) * 2, 10526880);
            int var4 = 0;

            for(ResourceKey<Level> var5 : this.upgrader.levels()) {
                int var6 = Mth.floor(this.upgrader.dimensionProgress(var5) * (float)(var1 - var0));
                param0.fill(var0 + var4, var2, var0 + var4 + var6, var3, DIMENSION_COLORS.applyAsInt(var5));
                var4 += var6;
            }

            int var7 = this.upgrader.getConverted() + this.upgrader.getSkipped();
            Component var8 = Component.translatable("optimizeWorld.progress.counter", var7, this.upgrader.getTotalChunks());
            Component var9 = Component.translatable("optimizeWorld.progress.percentage", Mth.floor(this.upgrader.getProgress() * 100.0F));
            param0.drawCenteredString(this.font, var8, this.width / 2, var2 + 2 * 9 + 2, 10526880);
            param0.drawCenteredString(this.font, var9, this.width / 2, var2 + (var3 - var2) / 2 - 9 / 2, 10526880);
        }

    }
}
