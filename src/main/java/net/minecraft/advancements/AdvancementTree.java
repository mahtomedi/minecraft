package net.minecraft.advancements;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AdvancementTree {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<ResourceLocation, AdvancementNode> nodes = new Object2ObjectOpenHashMap<>();
    private final Set<AdvancementNode> roots = new ObjectLinkedOpenHashSet<>();
    private final Set<AdvancementNode> tasks = new ObjectLinkedOpenHashSet<>();
    @Nullable
    private AdvancementTree.Listener listener;

    private void remove(AdvancementNode param0) {
        for(AdvancementNode var0 : param0.children()) {
            this.remove(var0);
        }

        LOGGER.info("Forgot about advancement {}", param0.holder());
        this.nodes.remove(param0.holder().id());
        if (param0.parent() == null) {
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
            AdvancementNode var1 = this.nodes.get(var0);
            if (var1 == null) {
                LOGGER.warn("Told to remove advancement {} but I don't know what that is", var0);
            } else {
                this.remove(var1);
            }
        }

    }

    public void addAll(Collection<AdvancementHolder> param0) {
        List<AdvancementHolder> var0 = new ArrayList<>(param0);

        while(!var0.isEmpty()) {
            if (!var0.removeIf(this::tryInsert)) {
                LOGGER.error("Couldn't load advancements: {}", var0);
                break;
            }
        }

        LOGGER.info("Loaded {} advancements", this.nodes.size());
    }

    private boolean tryInsert(AdvancementHolder param0x) {
        Optional<ResourceLocation> var0x = param0x.value().parent();
        AdvancementNode var1 = var0x.map(this.nodes::get).orElse(null);
        if (var1 == null && var0x.isPresent()) {
            return false;
        } else {
            AdvancementNode var2 = new AdvancementNode(param0x, var1);
            if (var1 != null) {
                var1.addChild(var2);
            }

            this.nodes.put(param0x.id(), var2);
            if (var1 == null) {
                this.roots.add(var2);
                if (this.listener != null) {
                    this.listener.onAddAdvancementRoot(var2);
                }
            } else {
                this.tasks.add(var2);
                if (this.listener != null) {
                    this.listener.onAddAdvancementTask(var2);
                }
            }

            return true;
        }
    }

    public void clear() {
        this.nodes.clear();
        this.roots.clear();
        this.tasks.clear();
        if (this.listener != null) {
            this.listener.onAdvancementsCleared();
        }

    }

    public Iterable<AdvancementNode> roots() {
        return this.roots;
    }

    public Collection<AdvancementNode> nodes() {
        return this.nodes.values();
    }

    @Nullable
    public AdvancementNode get(ResourceLocation param0) {
        return this.nodes.get(param0);
    }

    @Nullable
    public AdvancementNode get(AdvancementHolder param0) {
        return this.nodes.get(param0.id());
    }

    public void setListener(@Nullable AdvancementTree.Listener param0) {
        this.listener = param0;
        if (param0 != null) {
            for(AdvancementNode var0 : this.roots) {
                param0.onAddAdvancementRoot(var0);
            }

            for(AdvancementNode var1 : this.tasks) {
                param0.onAddAdvancementTask(var1);
            }
        }

    }

    public interface Listener {
        void onAddAdvancementRoot(AdvancementNode var1);

        void onRemoveAdvancementRoot(AdvancementNode var1);

        void onAddAdvancementTask(AdvancementNode var1);

        void onRemoveAdvancementTask(AdvancementNode var1);

        void onAdvancementsCleared();
    }
}
