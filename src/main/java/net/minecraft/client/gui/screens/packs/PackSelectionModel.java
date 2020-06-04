package net.minecraft.client.gui.screens.packs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PackSelectionModel<T extends Pack> {
    private final List<T> selected;
    private final List<T> unselected;
    private final BiConsumer<T, TextureManager> iconBinder;
    private final Runnable onListChanged;
    private final PackSelectionModel.CommitHandler<T> output;

    public PackSelectionModel(
        Runnable param0, BiConsumer<T, TextureManager> param1, Collection<T> param2, Collection<T> param3, PackSelectionModel.CommitHandler<T> param4
    ) {
        this.onListChanged = param0;
        this.iconBinder = param1;
        this.selected = Lists.newArrayList(param2);
        this.unselected = Lists.newArrayList(param3);
        this.output = param4;
    }

    public Stream<PackSelectionModel.Entry> getUnselected() {
        return this.unselected.stream().map(param0 -> new PackSelectionModel.UnselectedPackEntry(param0));
    }

    public Stream<PackSelectionModel.Entry> getSelected() {
        return this.selected.stream().map(param0 -> new PackSelectionModel.SelectedPackEntry(param0));
    }

    public void commit(boolean param0) {
        this.output.accept(ImmutableList.copyOf(this.selected), ImmutableList.copyOf(this.unselected), param0);
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface CommitHandler<T extends Pack> {
        void accept(List<T> var1, List<T> var2, boolean var3);
    }

    @OnlyIn(Dist.CLIENT)
    public interface Entry {
        void bindIcon(TextureManager var1);

        PackCompatibility getCompatibility();

        Component getTitle();

        Component getDescription();

        PackSource getPackSource();

        default FormattedText getExtendedDescription() {
            return this.getPackSource().decorate(this.getDescription());
        }

        boolean isFixedPosition();

        boolean isRequired();

        void select();

        void unselect();

        void moveUp();

        void moveDown();

        boolean isSelected();

        default boolean canSelect() {
            return !this.isSelected();
        }

        default boolean canUnselect() {
            return this.isSelected() && !this.isRequired();
        }

        boolean canMoveUp();

        boolean canMoveDown();
    }

    @OnlyIn(Dist.CLIENT)
    abstract class EntryBase implements PackSelectionModel.Entry {
        private final T pack;

        public EntryBase(T param0) {
            this.pack = param0;
        }

        protected abstract List<T> getSelfList();

        protected abstract List<T> getOtherList();

        @Override
        public void bindIcon(TextureManager param0) {
            PackSelectionModel.this.iconBinder.accept(this.pack, param0);
        }

        @Override
        public PackCompatibility getCompatibility() {
            return this.pack.getCompatibility();
        }

        @Override
        public Component getTitle() {
            return this.pack.getTitle();
        }

        @Override
        public Component getDescription() {
            return this.pack.getDescription();
        }

        @Override
        public PackSource getPackSource() {
            return this.pack.getPackSource();
        }

        @Override
        public boolean isFixedPosition() {
            return this.pack.isFixedPosition();
        }

        @Override
        public boolean isRequired() {
            return this.pack.isRequired();
        }

        protected void toggleSelection() {
            this.getSelfList().remove(this.pack);
            this.pack.getDefaultPosition().insert(this.getOtherList(), this.pack, Function.identity(), true);
            PackSelectionModel.this.onListChanged.run();
        }

        protected void move(int param0) {
            List<T> var0 = this.getSelfList();
            int var1 = var0.indexOf(this.pack);
            var0.remove(var1);
            var0.add(var1 + param0, this.pack);
            PackSelectionModel.this.onListChanged.run();
        }

        @Override
        public boolean canMoveUp() {
            List<T> var0 = this.getSelfList();
            int var1 = var0.indexOf(this.pack);
            return var1 > 0 && !var0.get(var1 - 1).isFixedPosition();
        }

        @Override
        public void moveUp() {
            this.move(-1);
        }

        @Override
        public boolean canMoveDown() {
            List<T> var0 = this.getSelfList();
            int var1 = var0.indexOf(this.pack);
            return var1 >= 0 && var1 < var0.size() - 1 && !var0.get(var1 + 1).isFixedPosition();
        }

        @Override
        public void moveDown() {
            this.move(1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class SelectedPackEntry extends PackSelectionModel<T>.EntryBase {
        public SelectedPackEntry(T param0) {
            super(param0);
        }

        @Override
        protected List<T> getSelfList() {
            return PackSelectionModel.this.selected;
        }

        @Override
        protected List<T> getOtherList() {
            return PackSelectionModel.this.unselected;
        }

        @Override
        public boolean isSelected() {
            return true;
        }

        @Override
        public void select() {
        }

        @Override
        public void unselect() {
            this.toggleSelection();
        }
    }

    @OnlyIn(Dist.CLIENT)
    class UnselectedPackEntry extends PackSelectionModel<T>.EntryBase {
        public UnselectedPackEntry(T param0) {
            super(param0);
        }

        @Override
        protected List<T> getSelfList() {
            return PackSelectionModel.this.unselected;
        }

        @Override
        protected List<T> getOtherList() {
            return PackSelectionModel.this.selected;
        }

        @Override
        public boolean isSelected() {
            return false;
        }

        @Override
        public void select() {
            this.toggleSelection();
        }

        @Override
        public void unselect() {
        }
    }
}
