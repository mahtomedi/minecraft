package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientAdvancements {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft;
    private final WorldSessionTelemetryManager telemetryManager;
    private final AdvancementTree tree = new AdvancementTree();
    private final Map<AdvancementHolder, AdvancementProgress> progress = new Object2ObjectOpenHashMap<>();
    @Nullable
    private ClientAdvancements.Listener listener;
    @Nullable
    private AdvancementHolder selectedTab;

    public ClientAdvancements(Minecraft param0, WorldSessionTelemetryManager param1) {
        this.minecraft = param0;
        this.telemetryManager = param1;
    }

    public void update(ClientboundUpdateAdvancementsPacket param0) {
        if (param0.shouldReset()) {
            this.tree.clear();
            this.progress.clear();
        }

        this.tree.remove(param0.getRemoved());
        this.tree.addAll(param0.getAdded());

        for(Entry<ResourceLocation, AdvancementProgress> var0 : param0.getProgress().entrySet()) {
            AdvancementNode var1 = this.tree.get(var0.getKey());
            if (var1 != null) {
                AdvancementProgress var2 = var0.getValue();
                var2.update(var1.advancement().requirements());
                this.progress.put(var1.holder(), var2);
                if (this.listener != null) {
                    this.listener.onUpdateAdvancementProgress(var1, var2);
                }

                if (!param0.shouldReset() && var2.isDone()) {
                    if (this.minecraft.level != null) {
                        this.telemetryManager.onAdvancementDone(this.minecraft.level, var1.holder());
                    }

                    Optional<DisplayInfo> var3 = var1.advancement().display();
                    if (var3.isPresent() && var3.get().shouldShowToast()) {
                        this.minecraft.getToasts().addToast(new AdvancementToast(var1.holder()));
                    }
                }
            } else {
                LOGGER.warn("Server informed client about progress for unknown advancement {}", var0.getKey());
            }
        }

    }

    public AdvancementTree getTree() {
        return this.tree;
    }

    public void setSelectedTab(@Nullable AdvancementHolder param0, boolean param1) {
        ClientPacketListener var0 = this.minecraft.getConnection();
        if (var0 != null && param0 != null && param1) {
            var0.send(ServerboundSeenAdvancementsPacket.openedTab(param0));
        }

        if (this.selectedTab != param0) {
            this.selectedTab = param0;
            if (this.listener != null) {
                this.listener.onSelectedTabChanged(param0);
            }
        }

    }

    public void setListener(@Nullable ClientAdvancements.Listener param0) {
        this.listener = param0;
        this.tree.setListener(param0);
        if (param0 != null) {
            this.progress.forEach((param1, param2) -> {
                AdvancementNode var0 = this.tree.get(param1);
                if (var0 != null) {
                    param0.onUpdateAdvancementProgress(var0, param2);
                }

            });
            param0.onSelectedTabChanged(this.selectedTab);
        }

    }

    @Nullable
    public AdvancementHolder get(ResourceLocation param0) {
        AdvancementNode var0 = this.tree.get(param0);
        return var0 != null ? var0.holder() : null;
    }

    @OnlyIn(Dist.CLIENT)
    public interface Listener extends AdvancementTree.Listener {
        void onUpdateAdvancementProgress(AdvancementNode var1, AdvancementProgress var2);

        void onSelectedTabChanged(@Nullable AdvancementHolder var1);
    }
}
