package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    final Minecraft minecraft;
    private double lastUpdateTime = Double.MIN_VALUE;
    private final int radius = 12;
    @Nullable
    private ChunkDebugRenderer.ChunkData data;

    public ChunkDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        double var0 = (double)Util.getNanos();
        if (var0 - this.lastUpdateTime > 3.0E9) {
            this.lastUpdateTime = var0;
            IntegratedServer var1 = this.minecraft.getSingleplayerServer();
            if (var1 != null) {
                this.data = new ChunkDebugRenderer.ChunkData(var1, param2, param4);
            } else {
                this.data = null;
            }
        }

        if (this.data != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(2.0F);
            RenderSystem.disableTexture();
            RenderSystem.depthMask(false);
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
                    DebugRenderer.renderFloatingText(
                        var9,
                        (double)SectionPos.sectionToBlockCoord(var5.x, 8),
                        var3 + (double)var8,
                        (double)SectionPos.sectionToBlockCoord(var5.z, 8),
                        -1,
                        0.15F
                    );
                    var8 -= 2;
                }
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }

    }

    @OnlyIn(Dist.CLIENT)
    final class ChunkData {
        final Map<ChunkPos, String> clientData;
        final CompletableFuture<Map<ChunkPos, String>> serverData;

        ChunkData(IntegratedServer param0, double param1, double param2) {
            ClientLevel param3 = ChunkDebugRenderer.this.minecraft.level;
            ResourceKey<Level> var0 = param3.dimension();
            int var1 = SectionPos.posToSectionCoord(param1);
            int var2 = SectionPos.posToSectionCoord(param2);
            Builder<ChunkPos, String> var3 = ImmutableMap.builder();
            ClientChunkCache var4 = param3.getChunkSource();

            for(int var5 = var1 - 12; var5 <= var1 + 12; ++var5) {
                for(int var6 = var2 - 12; var6 <= var2 + 12; ++var6) {
                    ChunkPos var7 = new ChunkPos(var5, var6);
                    String var8 = "";
                    LevelChunk var9 = var4.getChunk(var5, var6, false);
                    var8 = var8 + "Client: ";
                    if (var9 == null) {
                        var8 = var8 + "0n/a\n";
                    } else {
                        var8 = var8 + (var9.isEmpty() ? " E" : "");
                        var8 = var8 + "\n";
                    }

                    var3.put(var7, var8);
                }
            }

            this.clientData = var3.build();
            this.serverData = param0.submit(() -> {
                ServerLevel var0x = param0.getLevel(var0);
                if (var0x == null) {
                    return ImmutableMap.of();
                } else {
                    Builder<ChunkPos, String> var1xx = ImmutableMap.builder();
                    ServerChunkCache var2x = var0x.getChunkSource();

                    for(int var3x = var1 - 12; var3x <= var1 + 12; ++var3x) {
                        for(int var4x = var2 - 12; var4x <= var2 + 12; ++var4x) {
                            ChunkPos var5x = new ChunkPos(var3x, var4x);
                            var1xx.put(var5x, "Server: " + var2x.getChunkDebugData(var5x));
                        }
                    }

                    return var1xx.build();
                }
            });
        }
    }
}
