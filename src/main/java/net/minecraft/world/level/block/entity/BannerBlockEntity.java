package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BannerBlockEntity extends BlockEntity implements Nameable {
    public static final int MAX_PATTERNS = 6;
    public static final String TAG_PATTERNS = "Patterns";
    public static final String TAG_PATTERN = "Pattern";
    public static final String TAG_COLOR = "Color";
    @Nullable
    private Component name;
    private DyeColor baseColor;
    @Nullable
    private ListTag itemPatterns;
    @Nullable
    private List<Pair<BannerPattern, DyeColor>> patterns;

    public BannerBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.BANNER, param0, param1);
        this.baseColor = ((AbstractBannerBlock)param1.getBlock()).getColor();
    }

    public BannerBlockEntity(BlockPos param0, BlockState param1, DyeColor param2) {
        this(param0, param1);
        this.baseColor = param2;
    }

    @Nullable
    public static ListTag getItemPatterns(ItemStack param0) {
        ListTag var0 = null;
        CompoundTag var1 = BlockItem.getBlockEntityData(param0);
        if (var1 != null && var1.contains("Patterns", 9)) {
            var0 = var1.getList("Patterns", 10).copy();
        }

        return var0;
    }

    public void fromItem(ItemStack param0, DyeColor param1) {
        this.baseColor = param1;
        this.fromItem(param0);
    }

    public void fromItem(ItemStack param0) {
        this.itemPatterns = getItemPatterns(param0);
        this.patterns = null;
        this.name = param0.hasCustomHoverName() ? param0.getHoverName() : null;
    }

    @Override
    public Component getName() {
        return (Component)(this.name != null ? this.name : Component.translatable("block.minecraft.banner"));
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }

    public void setCustomName(Component param0) {
        this.name = param0;
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        if (this.itemPatterns != null) {
            param0.put("Patterns", this.itemPatterns);
        }

        if (this.name != null) {
            param0.putString("CustomName", Component.Serializer.toJson(this.name));
        }

    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(param0.getString("CustomName"));
        }

        this.itemPatterns = param0.getList("Patterns", 10);
        this.patterns = null;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public static int getPatternCount(ItemStack param0) {
        CompoundTag var0 = BlockItem.getBlockEntityData(param0);
        return var0 != null && var0.contains("Patterns") ? var0.getList("Patterns", 10).size() : 0;
    }

    public List<Pair<BannerPattern, DyeColor>> getPatterns() {
        if (this.patterns == null) {
            this.patterns = createPatterns(this.baseColor, this.itemPatterns);
        }

        return this.patterns;
    }

    public static List<Pair<BannerPattern, DyeColor>> createPatterns(DyeColor param0, @Nullable ListTag param1) {
        List<Pair<BannerPattern, DyeColor>> var0 = Lists.newArrayList();
        var0.add(Pair.of(BannerPattern.BASE, param0));
        if (param1 != null) {
            for(int var1 = 0; var1 < param1.size(); ++var1) {
                CompoundTag var2 = param1.getCompound(var1);
                BannerPattern var3 = BannerPattern.byHash(var2.getString("Pattern"));
                if (var3 != null) {
                    int var4 = var2.getInt("Color");
                    var0.add(Pair.of(var3, DyeColor.byId(var4)));
                }
            }
        }

        return var0;
    }

    public static void removeLastPattern(ItemStack param0) {
        CompoundTag var0 = BlockItem.getBlockEntityData(param0);
        if (var0 != null && var0.contains("Patterns", 9)) {
            ListTag var1 = var0.getList("Patterns", 10);
            if (!var1.isEmpty()) {
                var1.remove(var1.size() - 1);
                if (var1.isEmpty()) {
                    var0.remove("Patterns");
                }

                BlockItem.setBlockEntityData(param0, BlockEntityType.BANNER, var0);
            }
        }
    }

    public ItemStack getItem() {
        ItemStack var0 = new ItemStack(BannerBlock.byColor(this.baseColor));
        if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
            CompoundTag var1 = new CompoundTag();
            var1.put("Patterns", this.itemPatterns.copy());
            BlockItem.setBlockEntityData(var0, this.getType(), var1);
        }

        if (this.name != null) {
            var0.setHoverName(this.name);
        }

        return var0;
    }

    public DyeColor getBaseColor() {
        return this.baseColor;
    }
}
