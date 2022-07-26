package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AdvancementList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<ResourceLocation, Advancement> advancements = Maps.newHashMap();
    private final Set<Advancement> roots = Sets.newLinkedHashSet();
    private final Set<Advancement> tasks = Sets.newLinkedHashSet();
    @Nullable
    private AdvancementList.Listener listener;

    private void remove(Advancement param0) {
        for(Advancement var0 : param0.getChildren()) {
            this.remove(var0);
        }

        LOGGER.info("Forgot about advancement {}", param0.getId());
        this.advancements.remove(param0.getId());
        if (param0.getParent() == null) {
            this.roots.remove(param0);
            if (this.listener != null) {
                this.listener.onRemoveAdvancementRoot(param0);
            }
        } else {
            this.tasks.remove(param0);
            if (this.listener != null) {
                this.listener.onRemoveAdvancementTask(param0);
            }
        }

    }

    public void remove(Set<ResourceLocation> param0) {
        for(ResourceLocation var0 : param0) {
            Advancement var1 = this.advancements.get(var0);
            if (var1 == null) {
                LOGGER.warn("Told to remove advancement {} but I don't know what that is", var0);
            } else {
                this.remove(var1);
            }
        }

    }

    public void add(Map<ResourceLocation, Advancement.Builder> param0) {
        Map<ResourceLocation, Advancement.Builder> var0 = Maps.newHashMap(param0);

        while(!var0.isEmpty()) {
            boolean var1 = false;
            Iterator<Entry<ResourceLocation, Advancement.Builder>> var2 = var0.entrySet().iterator();

            while(var2.hasNext()) {
                Entry<ResourceLocation, Advancement.Builder> var3 = var2.next();
                ResourceLocation var4 = var3.getKey();
                Advancement.Builder var5 = var3.getValue();
                if (var5.canBuild(this.advancements::get)) {
                    Advancement var6 = var5.build(var4);
                    this.advancements.put(var4, var6);
                    var1 = true;
                    var2.remove();
                    if (var6.getParent() == null) {
                        this.roots.add(var6);
                        if (this.listener != null) {
                            this.listener.onAddAdvancementRoot(var6);
                        }
                    } else {
                        this.tasks.add(var6);
                        if (this.listener != null) {
                            this.listener.onAddAdvancementTask(var6);
                        }
                    }
                }
            }

            if (!var1) {
                for(Entry<ResourceLocation, Advancement.Builder> var7 : var0.entrySet()) {
                    LOGGER.error("Couldn't load advancement {}: {}", var7.getKey(), var7.getValue());
                }
                break;
            }
        }

        LOGGER.info("Loaded {} advancements", this.advancements.size());
    }

    public void clear() {
        this.advancements.clear();
        this.roots.clear();
        this.tasks.clear();
        if (this.listener != null) {
            this.listener.onAdvancementsCleared();
        }

    }

    public Iterable<Advancement> getRoots() {
        return this.roots;
    }

    public Collection<Advancement> getAllAdvancements() {
        return this.advancements.values();
    }

    @Nullable
    public Advancement get(ResourceLocation param0) {
        return this.advancements.get(param0);
    }

    public void setListener(@Nullable AdvancementList.Listener param0) {
        this.listener = param0;
        if (param0 != null) {
            for(Advancement var0 : this.roots) {
                param0.onAddAdvancementRoot(var0);
            }

            for(Advancement var1 : this.tasks) {
                param0.onAddAdvancementTask(var1);
            }
        }

    }

    public interface Listener {
        void onAddAdvancementRoot(Advancement var1);

        void onRemoveAdvancementRoot(Advancement var1);

        void onAddAdvancementTask(Advancement var1);

        void onRemoveAdvancementTask(Advancement var1);

        void onAdvancementsCleared();
    }
}
