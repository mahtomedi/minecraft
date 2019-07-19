package net.minecraft.client.multiplayer;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientAdvancements {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft minecraft;
    private final AdvancementList advancements = new AdvancementList();
    private final Map<Advancement, AdvancementProgress> progress = Maps.newHashMap();
    @Nullable
    private ClientAdvancements.Listener listener;
    @Nullable
    private Advancement selectedTab;

    public ClientAdvancements(Minecraft param0) {
        this.minecraft = param0;
    }

    public void update(ClientboundUpdateAdvancementsPacket param0) {
        if (param0.shouldReset()) {
            this.advancements.clear();
            this.progress.clear();
        }

        this.advancements.remove(param0.getRemoved());
        this.advancements.add(param0.getAdded());

        for(Entry<ResourceLocation, AdvancementProgress> var0 : param0.getProgress().entrySet()) {
            Advancement var1 = this.advancements.get(var0.getKey());
            if (var1 != null) {
                AdvancementProgress var2 = var0.getValue();
                var2.update(var1.getCriteria(), var1.getRequirements());
                this.progress.put(var1, var2);
                if (this.listener != null) {
                    this.listener.onUpdateAdvancementProgress(var1, var2);
                }

                if (!param0.shouldReset() && var2.isDone() && var1.getDisplay() != null && var1.getDisplay().shouldShowToast()) {
                    this.minecraft.getToasts().addToast(new AdvancementToast(var1));
                }
            } else {
                LOGGER.warn("Server informed client about progress for unknown advancement {}", var0.getKey());
            }
        }

    }

    public AdvancementList getAdvancements() {
        return this.advancements;
    }

    public void setSelectedTab(@Nullable Advancement param0, boolean param1) {
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
        this.advancements.setListener(param0);
        if (param0 != null) {
            for(Entry<Advancement, AdvancementProgress> var0 : this.progress.entrySet()) {
                param0.onUpdateAdvancementProgress(var0.getKey(), var0.getValue());
            }

            param0.onSelectedTabChanged(this.selectedTab);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public interface Listener extends AdvancementList.Listener {
        void onUpdateAdvancementProgress(Advancement var1, AdvancementProgress var2);

        void onSelectedTabChanged(@Nullable Advancement var1);
    }
}
