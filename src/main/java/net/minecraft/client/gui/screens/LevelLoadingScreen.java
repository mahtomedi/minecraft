package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LevelLoadingScreen extends Screen {
    private final StoringChunkProgressListener progressListener;
    private long lastNarration = -1L;
    private static final Object2IntMap<ChunkStatus> COLORS = Util.make(new Object2IntOpenHashMap<>(), param0 -> {
        param0.defaultReturnValue(0);
        param0.put(ChunkStatus.EMPTY, 5526612);
        param0.put(ChunkStatus.STRUCTURE_STARTS, 10066329);
        param0.put(ChunkStatus.STRUCTURE_REFERENCES, 6250897);
        param0.put(ChunkStatus.BIOMES, 8434258);
        param0.put(ChunkStatus.NOISE, 13750737);
        param0.put(ChunkStatus.SURFACE, 7497737);
        param0.put(ChunkStatus.CARVERS, 7169628);
        param0.put(ChunkStatus.LIQUID_CARVERS, 3159410);
        param0.put(ChunkStatus.FEATURES, 2213376);
        param0.put(ChunkStatus.LIGHT, 13421772);
        param0.put(ChunkStatus.SPAWN, 15884384);
        param0.put(ChunkStatus.HEIGHTMAPS, 15658734);
        param0.put(ChunkStatus.FULL, 16777215);
    });

    public LevelLoadingScreen(StoringChunkProgressListener param0) {
        super(NarratorChatListener.NO_TITLE);
        this.progressListener = param0;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void removed() {
        NarratorChatListener.INSTANCE.sayNow(I18n.get("narrator.loading.done"));
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        String var0 = Mth.clamp(this.progressListener.getProgress(), 0, 100) + "%";
        long var1 = Util.getMillis();
        if (var1 - this.lastNarration > 2000L) {
            this.lastNarration = var1;
            NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.loading", var0).getString());
        }

        int var2 = this.width / 2;
        int var3 = this.height / 2;
        int var4 = 30;
        renderChunks(this.progressListener, var2, var3 + 30, 2, 0);
        this.drawCenteredString(this.font, var0, var2, var3 - 9 / 2 - 30, 16777215);
    }

    public static void renderChunks(StoringChunkProgressListener param0, int param1, int param2, int param3, int param4) {
        int var0 = param3 + param4;
        int var1 = param0.getFullDiameter();
        int var2 = var1 * var0 - param4;
        int var3 = param0.getDiameter();
        int var4 = var3 * var0 - param4;
        int var5 = param1 - var4 / 2;
        int var6 = param2 - var4 / 2;
        int var7 = var2 / 2 + 1;
        int var8 = -16772609;
        if (param4 != 0) {
            fill(param1 - var7, param2 - var7, param1 - var7 + 1, param2 + var7, -16772609);
            fill(param1 + var7 - 1, param2 - var7, param1 + var7, param2 + var7, -16772609);
            fill(param1 - var7, param2 - var7, param1 + var7, param2 - var7 + 1, -16772609);
            fill(param1 - var7, param2 + var7 - 1, param1 + var7, param2 + var7, -16772609);
        }

        for(int var9 = 0; var9 < var3; ++var9) {
            for(int var10 = 0; var10 < var3; ++var10) {
                ChunkStatus var11 = param0.getStatus(var9, var10);
                int var12 = var5 + var9 * var0;
                int var13 = var6 + var10 * var0;
                fill(var12, var13, var12 + param3, var13 + param3, COLORS.getInt(var11) | 0xFF000000);
            }
        }

    }
}
