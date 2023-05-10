package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LevelLoadingScreen extends Screen {
    private static final long NARRATION_DELAY_MS = 2000L;
    private final StoringChunkProgressListener progressListener;
    private long lastNarration = -1L;
    private boolean done;
    private static final Object2IntMap<ChunkStatus> COLORS = Util.make(new Object2IntOpenHashMap<>(), param0 -> {
        param0.defaultReturnValue(0);
        param0.put(ChunkStatus.EMPTY, 5526612);
        param0.put(ChunkStatus.STRUCTURE_STARTS, 10066329);
        param0.put(ChunkStatus.STRUCTURE_REFERENCES, 6250897);
        param0.put(ChunkStatus.BIOMES, 8434258);
        param0.put(ChunkStatus.NOISE, 13750737);
        param0.put(ChunkStatus.SURFACE, 7497737);
        param0.put(ChunkStatus.CARVERS, 3159410);
        param0.put(ChunkStatus.FEATURES, 2213376);
        param0.put(ChunkStatus.INITIALIZE_LIGHT, 13421772);
        param0.put(ChunkStatus.LIGHT, 16769184);
        param0.put(ChunkStatus.SPAWN, 15884384);
        param0.put(ChunkStatus.FULL, 16777215);
    });

    public LevelLoadingScreen(StoringChunkProgressListener param0) {
        super(GameNarrator.NO_TITLE);
        this.progressListener = param0;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected boolean shouldNarrateNavigation() {
        return false;
    }

    @Override
    public void removed() {
        this.done = true;
        this.triggerImmediateNarration(true);
    }

    @Override
    protected void updateNarratedWidget(NarrationElementOutput param0) {
        if (this.done) {
            param0.add(NarratedElementType.TITLE, (Component)Component.translatable("narrator.loading.done"));
        } else {
            String var0 = this.getFormattedProgress();
            param0.add(NarratedElementType.TITLE, var0);
        }

    }

    private String getFormattedProgress() {
        return Mth.clamp(this.progressListener.getProgress(), 0, 100) + "%";
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        long var0 = Util.getMillis();
        if (var0 - this.lastNarration > 2000L) {
            this.lastNarration = var0;
            this.triggerImmediateNarration(true);
        }

        int var1 = this.width / 2;
        int var2 = this.height / 2;
        int var3 = 30;
        renderChunks(param0, this.progressListener, var1, var2 + 30, 2, 0);
        param0.drawCenteredString(this.font, this.getFormattedProgress(), var1, var2 - 9 / 2 - 30, 16777215);
    }

    public static void renderChunks(GuiGraphics param0, StoringChunkProgressListener param1, int param2, int param3, int param4, int param5) {
        int var0 = param4 + param5;
        int var1 = param1.getFullDiameter();
        int var2 = var1 * var0 - param5;
        int var3 = param1.getDiameter();
        int var4 = var3 * var0 - param5;
        int var5 = param2 - var4 / 2;
        int var6 = param3 - var4 / 2;
        int var7 = var2 / 2 + 1;
        int var8 = -16772609;
        param0.drawManaged(() -> {
            if (param5 != 0) {
                param0.fill(param2 - var7, param3 - var7, param2 - var7 + 1, param3 + var7, -16772609);
                param0.fill(param2 + var7 - 1, param3 - var7, param2 + var7, param3 + var7, -16772609);
                param0.fill(param2 - var7, param3 - var7, param2 + var7, param3 - var7 + 1, -16772609);
                param0.fill(param2 - var7, param3 + var7 - 1, param2 + var7, param3 + var7, -16772609);
            }

            for(int var0x = 0; var0x < var3; ++var0x) {
                for(int var1x = 0; var1x < var3; ++var1x) {
                    ChunkStatus var2x = param1.getStatus(var0x, var1x);
                    int var3x = var5 + var0x * var0;
                    int var4x = var6 + var1x * var0;
                    param0.fill(var3x, var4x, var3x + param4, var4x + param4, COLORS.getInt(var2x) | 0xFF000000);
                }
            }

        });
    }
}
