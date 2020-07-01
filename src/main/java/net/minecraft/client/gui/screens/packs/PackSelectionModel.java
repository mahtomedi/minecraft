package net.minecraft.client.gui.screens.packs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PackSelectionModel {
    private final PackRepository repository;
    private final List<Pack> selected;
    private final List<Pack> unselected;
    private final Function<Pack, ResourceLocation> iconGetter;
    private final Runnable onListChanged;
    private final Consumer<PackRepository> output;

    public PackSelectionModel(Runnable param0, Function<Pack, ResourceLocation> param1, PackRepository param2, Consumer<PackRepository> param3) {
        this.onListChanged = param0;
        this.iconGetter = param1;
        this.repository = param2;
        this.selected = Lists.newArrayList(param2.getSelectedPacks());
        Collections.reverse(this.selected);
        this.unselected = Lists.newArrayList(param2.getAvailablePacks());
        this.unselected.removeAll(this.selected);
        this.output = param3;
    }

    public Stream<PackSelectionModel.Entry> getUnselected() {
        return this.unselected.stream().map(param0 -> new PackSelectionModel.UnselectedPackEntry(param0));
    }

    public Stream<PackSelectionModel.Entry> getSelected() {
        return this.selected.stream().map(param0 -> new PackSelectionModel.SelectedPackEntry(param0));
    }

    public void commit() {
        this.repository.setSelected(Lists.reverse(this.selected).stream().map(Pack::getId).collect(ImmutableList.toImmutableList()));
        this.output.accept(this.repository);
    }

    public void findNewPacks() {
        this.repository.reload();
        this.selected.clear();
        this.selected.addAll(this.repository.getSelectedPacks());
        Collections.reverse(this.selected);
        this.unselected.clear();
        this.unselected.addAll(this.repository.getAvailablePacks());
        this.unselected.removeAll(this.selected);
    }

    @OnlyIn(Dist.CLIENT)
    public interface Entry {
        ResourceLocation getIconTexture();

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
        private final Pack pack;

        public EntryBase(Pack param0) {
            this.pack = param0;
        }

        protected abstract List<Pack> getSelfList();

        protected abstract List<Pack> getOtherList();

        @Override
        public ResourceLocation getIconTexture() {
            return PackSelectionModel.this.iconGetter.apply(this.pack);
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
            List<Pack> var0 = this.getSelfList();
            int var1 = var0.indexOf(this.pack);
            var0.remove(var1);
            var0.add(var1 + param0, this.pack);
            PackSelectionModel.this.onListChanged.run();
        }

        @Override
        public boolean canMoveUp() {
            List<Pack> var0 = this.getSelfList();
            int var1 = var0.indexOf(this.pack);
            return var1 > 0 && !var0.get(var1 - 1).isFixedPosition();
        }

        @Override
        public void moveUp() {
            this.move(-1);
        }

        @Override
        public boolean canMoveDown() {
            List<Pack> var0 = this.getSelfList();
            int var1 = var0.indexOf(this.pack);
            return var1 >= 0 && var1 < var0.size() - 1 && !var0.get(var1 + 1).isFixedPosition();
        }

        @Override
        public void moveDown() {
            this.move(1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class SelectedPackEntry extends PackSelectionModel.EntryBase {
        public SelectedPackEntry(Pack param0) {
            super(param0);
        }

        @Override
        protected List<Pack> getSelfList() {
            return PackSelectionModel.this.selected;
        }

        @Override
        protected List<Pack> getOtherList() {
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
    class UnselectedPackEntry extends PackSelectionModel.EntryBase {
        public UnselectedPackEntry(Pack param0) {
            super(param0);
        }

        @Override
        protected List<Pack> getSelfList() {
            return PackSelectionModel.this.unselected;
        }

        @Override
        protected List<Pack> getOtherList() {
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
