package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BannerBlockEntity extends BlockEntity implements Nameable {
    private Component name;
    private DyeColor baseColor = DyeColor.WHITE;
    private ListTag itemPatterns;
    private boolean receivedData;
    private List<BannerPattern> patterns;
    private List<DyeColor> colors;
    private String textureHashName;

    public BannerBlockEntity() {
        super(BlockEntityType.BANNER);
    }

    public BannerBlockEntity(DyeColor param0) {
        this();
        this.baseColor = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public void fromItem(ItemStack param0, DyeColor param1) {
        this.itemPatterns = null;
        CompoundTag var0 = param0.getTagElement("BlockEntityTag");
        if (var0 != null && var0.contains("Patterns", 9)) {
            this.itemPatterns = var0.getList("Patterns", 10).copy();
        }

        this.baseColor = param1;
        this.patterns = null;
        this.colors = null;
        this.textureHashName = "";
        this.receivedData = true;
        this.name = param0.hasCustomHoverName() ? param0.getHoverName() : null;
    }

    @Override
    public Component getName() {
        return (Component)(this.name != null ? this.name : new TranslatableComponent("block.minecraft.banner"));
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
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        if (this.itemPatterns != null) {
            param0.put("Patterns", this.itemPatterns);
        }

        if (this.name != null) {
            param0.putString("CustomName", Component.Serializer.toJson(this.name));
        }

        return param0;
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(param0.getString("CustomName"));
        }

        if (this.hasLevel()) {
            this.baseColor = ((AbstractBannerBlock)this.getBlockState().getBlock()).getColor();
        } else {
            this.baseColor = null;
        }

        this.itemPatterns = param0.getList("Patterns", 10);
        this.patterns = null;
        this.colors = null;
        this.textureHashName = null;
        this.receivedData = true;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 6, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    public static int getPatternCount(ItemStack param0) {
        CompoundTag var0 = param0.getTagElement("BlockEntityTag");
        return var0 != null && var0.contains("Patterns") ? var0.getList("Patterns", 10).size() : 0;
    }

    @OnlyIn(Dist.CLIENT)
    public List<BannerPattern> getPatterns() {
        this.createPatternList();
        return this.patterns;
    }

    @OnlyIn(Dist.CLIENT)
    public List<DyeColor> getColors() {
        this.createPatternList();
        return this.colors;
    }

    @OnlyIn(Dist.CLIENT)
    public String getTextureHashName() {
        this.createPatternList();
        return this.textureHashName;
    }

    @OnlyIn(Dist.CLIENT)
    private void createPatternList() {
        if (this.patterns == null || this.colors == null || this.textureHashName == null) {
            if (!this.receivedData) {
                this.textureHashName = "";
            } else {
                this.patterns = Lists.newArrayList();
                this.colors = Lists.newArrayList();
                DyeColor var0 = this.getBaseColor(this::getBlockState);
                if (var0 == null) {
                    this.textureHashName = "banner_missing";
                } else {
                    this.patterns.add(BannerPattern.BASE);
                    this.colors.add(var0);
                    this.textureHashName = "b" + var0.getId();
                    if (this.itemPatterns != null) {
                        for(int var1 = 0; var1 < this.itemPatterns.size(); ++var1) {
                            CompoundTag var2 = this.itemPatterns.getCompound(var1);
                            BannerPattern var3 = BannerPattern.byHash(var2.getString("Pattern"));
                            if (var3 != null) {
                                this.patterns.add(var3);
                                int var4 = var2.getInt("Color");
                                this.colors.add(DyeColor.byId(var4));
                                this.textureHashName = this.textureHashName + var3.getHashname() + var4;
                            }
                        }
                    }
                }

            }
        }
    }

    public static void removeLastPattern(ItemStack param0) {
        CompoundTag var0 = param0.getTagElement("BlockEntityTag");
        if (var0 != null && var0.contains("Patterns", 9)) {
            ListTag var1 = var0.getList("Patterns", 10);
            if (!var1.isEmpty()) {
                var1.remove(var1.size() - 1);
                if (var1.isEmpty()) {
                    param0.removeTagKey("BlockEntityTag");
                }

            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack getItem(BlockState param0) {
        ItemStack var0 = new ItemStack(BannerBlock.byColor(this.getBaseColor(() -> param0)));
        if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
            var0.getOrCreateTagElement("BlockEntityTag").put("Patterns", this.itemPatterns.copy());
        }

        if (this.name != null) {
            var0.setHoverName(this.name);
        }

        return var0;
    }

    public DyeColor getBaseColor(Supplier<BlockState> param0) {
        if (this.baseColor == null) {
            this.baseColor = ((AbstractBannerBlock)param0.get().getBlock()).getColor();
        }

        return this.baseColor;
    }
}
