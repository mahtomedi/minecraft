package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private double lastUpdateTime = Double.MIN_VALUE;
    private final int radius = 12;
    @Nullable
    private ChunkDebugRenderer.ChunkData data;

    public ChunkDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(long param0) {
        double var0 = (double)Util.getNanos();
        if (var0 - this.lastUpdateTime > 3.0E9) {
            this.lastUpdateTime = var0;
            IntegratedServer var1 = this.minecraft.getSingleplayerServer();
            if (var1 != null) {
                this.data = new ChunkDebugRenderer.ChunkData(var1);
            } else {
                this.data = null;
            }
        }

        if (this.data != null) {
            GlStateManager.disableFog();
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
            );
            GlStateManager.lineWidth(2.0F);
            GlStateManager.disableTexture();
            GlStateManager.depthMask(false);
            Map<ChunkPos, String> var2 = this.data.serverData.getNow(null);
            double var3 = this.minecraft.gameRenderer.getMainCamera().getPosition().y * 0.85;

            for(Entry<ChunkPos, String> var4 : this.data.clientData.entrySet()) {
                ChunkPos var5 = var4.getKey();
                String var6 = var4.getValue();
                if (var2 != null) {
                    var6 = var6 + (String)var2.get(var5);
                }

                String[] var7 = var6.split("\n");
                int var8 = 0;

                for(String var9 : var7) {
                    DebugRenderer.renderFloatingText(var9, (double)((var5.x << 4) + 8), var3 + (double)var8, (double)((var5.z << 4) + 8), -1, 0.15F);
                    var8 -= 2;
                }
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture();
            GlStateManager.disableBlend();
            GlStateManager.enableFog();
        }

    }

    @OnlyIn(Dist.CLIENT)
    final class ChunkData {
        private final Map<ChunkPos, String> clientData;
        private final CompletableFuture<Map<ChunkPos, String>> serverData;

        private ChunkData(IntegratedServer param0) {
            MultiPlayerLevel param1 = ChunkDebugRenderer.this.minecraft.level;
            DimensionType var0 = ChunkDebugRenderer.this.minecraft.level.dimension.getType();
            ServerLevel var1;
            if (param0.getLevel(var0) != null) {
                var1 = param0.getLevel(var0);
            } else {
                var1 = null;
            }

            Camera var3 = ChunkDebugRenderer.this.minecraft.gameRenderer.getMainCamera();
            int var4 = (int)var3.getPosition().x >> 4;
            int var5 = (int)var3.getPosition().z >> 4;
            Builder<ChunkPos, String> var6 = ImmutableMap.builder();
            ClientChunkCache var7 = param1.getChunkSource();

            for(int var8 = var4 - 12; var8 <= var4 + 12; ++var8) {
                for(int var9 = var5 - 12; var9 <= var5 + 12; ++var9) {
                    ChunkPos var10 = new ChunkPos(var8, var9);
                    String var11 = "";
                    LevelChunk var12 = var7.getChunk(var8, var9, false);
                    var11 = var11 + "Client: ";
                    if (var12 == null) {
                        var11 = var11 + "0n/a\n";
                    } else {
                        var11 = var11 + (var12.isEmpty() ? " E" : "");
                        var11 = var11 + "\n";
                    }

                    var6.put(var10, var11);
                }
            }

            this.clientData = var6.build();
            this.serverData = param0.submit(() -> {
                Builder<ChunkPos, String> var0x = ImmutableMap.builder();
                ServerChunkCache var1x = var1.getChunkSource();

                for(int var2x = var4 - 12; var2x <= var4 + 12; ++var2x) {
                    for(int var3x = var5 - 12; var3x <= var5 + 12; ++var3x) {
                        ChunkPos var4x = new ChunkPos(var2x, var3x);
                        var0x.put(var4x, "Server: " + var1x.getChunkDebugData(var4x));
                    }
                }

                return var0x.build();
            });
        }
    }
}
