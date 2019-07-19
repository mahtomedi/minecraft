package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Stitcher {
    private static final Comparator<Stitcher.Holder> HOLDER_COMPARATOR = Comparator.<Stitcher.Holder, Integer>comparing(param0 -> -param0.height)
        .thenComparing(param0 -> -param0.width)
        .thenComparing(param0 -> param0.sprite.getName());
    private final int mipLevel;
    private final Set<Stitcher.Holder> texturesToBeStitched = Sets.newHashSetWithExpectedSize(256);
    private final List<Stitcher.Region> storage = Lists.newArrayListWithCapacity(256);
    private int storageX;
    private int storageY;
    private final int maxWidth;
    private final int maxHeight;

    public Stitcher(int param0, int param1, int param2) {
        this.mipLevel = param2;
        this.maxWidth = param0;
        this.maxHeight = param1;
    }

    public int getWidth() {
        return this.storageX;
    }

    public int getHeight() {
        return this.storageY;
    }

    public void registerSprite(TextureAtlasSprite param0) {
        Stitcher.Holder var0 = new Stitcher.Holder(param0, this.mipLevel);
        this.texturesToBeStitched.add(var0);
    }

    public void stitch() {
        List<Stitcher.Holder> var0 = Lists.newArrayList(this.texturesToBeStitched);
        var0.sort(HOLDER_COMPARATOR);

        for(Stitcher.Holder var1 : var0) {
            if (!this.addToStorage(var1)) {
                throw new StitcherException(var1.sprite, var0.stream().map(param0 -> param0.sprite).collect(ImmutableList.toImmutableList()));
            }
        }

        this.storageX = Mth.smallestEncompassingPowerOfTwo(this.storageX);
        this.storageY = Mth.smallestEncompassingPowerOfTwo(this.storageY);
    }

    public List<TextureAtlasSprite> gatherSprites() {
        List<TextureAtlasSprite> var0 = Lists.newArrayList();

        for(Stitcher.Region var1 : this.storage) {
            var1.walk(param1 -> {
                Stitcher.Holder var0x = param1.getHolder();
                TextureAtlasSprite var1x = var0x.sprite;
                var1x.init(this.storageX, this.storageY, param1.getX(), param1.getY());
                var0.add(var1x);
            });
        }

        return var0;
    }

    private static int smallestFittingMinTexel(int param0, int param1) {
        return (param0 >> param1) + ((param0 & (1 << param1) - 1) == 0 ? 0 : 1) << param1;
    }

    private boolean addToStorage(Stitcher.Holder param0) {
        for(Stitcher.Region var0 : this.storage) {
            if (var0.add(param0)) {
                return true;
            }
        }

        return this.expand(param0);
    }

    private boolean expand(Stitcher.Holder param0) {
        int var0 = Mth.smallestEncompassingPowerOfTwo(this.storageX);
        int var1 = Mth.smallestEncompassingPowerOfTwo(this.storageY);
        int var2 = Mth.smallestEncompassingPowerOfTwo(this.storageX + param0.width);
        int var3 = Mth.smallestEncompassingPowerOfTwo(this.storageY + param0.height);
        boolean var4 = var2 <= this.maxWidth;
        boolean var5 = var3 <= this.maxHeight;
        if (!var4 && !var5) {
            return false;
        } else {
            boolean var6 = var4 && var0 != var2;
            boolean var7 = var5 && var1 != var3;
            boolean var8;
            if (var6 ^ var7) {
                var8 = var6;
            } else {
                var8 = var4 && var0 <= var1;
            }

            Stitcher.Region var10;
            if (var8) {
                if (this.storageY == 0) {
                    this.storageY = param0.height;
                }

                var10 = new Stitcher.Region(this.storageX, 0, param0.width, this.storageY);
                this.storageX += param0.width;
            } else {
                var10 = new Stitcher.Region(0, this.storageY, this.storageX, param0.height);
                this.storageY += param0.height;
            }

            var10.add(param0);
            this.storage.add(var10);
            return true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Holder {
        public final TextureAtlasSprite sprite;
        public final int width;
        public final int height;

        public Holder(TextureAtlasSprite param0, int param1) {
            this.sprite = param0;
            this.width = Stitcher.smallestFittingMinTexel(param0.getWidth(), param1);
            this.height = Stitcher.smallestFittingMinTexel(param0.getHeight(), param1);
        }

        @Override
        public String toString() {
            return "Holder{width=" + this.width + ", height=" + this.height + '}';
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Region {
        private final int originX;
        private final int originY;
        private final int width;
        private final int height;
        private List<Stitcher.Region> subSlots;
        private Stitcher.Holder holder;

        public Region(int param0, int param1, int param2, int param3) {
            this.originX = param0;
            this.originY = param1;
            this.width = param2;
            this.height = param3;
        }

        public Stitcher.Holder getHolder() {
            return this.holder;
        }

        public int getX() {
            return this.originX;
        }

        public int getY() {
            return this.originY;
        }

        public boolean add(Stitcher.Holder param0) {
            if (this.holder != null) {
                return false;
            } else {
                int var0 = param0.width;
                int var1 = param0.height;
                if (var0 <= this.width && var1 <= this.height) {
                    if (var0 == this.width && var1 == this.height) {
                        this.holder = param0;
                        return true;
                    } else {
                        if (this.subSlots == null) {
                            this.subSlots = Lists.newArrayListWithCapacity(1);
                            this.subSlots.add(new Stitcher.Region(this.originX, this.originY, var0, var1));
                            int var2 = this.width - var0;
                            int var3 = this.height - var1;
                            if (var3 > 0 && var2 > 0) {
                                int var4 = Math.max(this.height, var2);
                                int var5 = Math.max(this.width, var3);
                                if (var4 >= var5) {
                                    this.subSlots.add(new Stitcher.Region(this.originX, this.originY + var1, var0, var3));
                                    this.subSlots.add(new Stitcher.Region(this.originX + var0, this.originY, var2, this.height));
                                } else {
                                    this.subSlots.add(new Stitcher.Region(this.originX + var0, this.originY, var2, var1));
                                    this.subSlots.add(new Stitcher.Region(this.originX, this.originY + var1, this.width, var3));
                                }
                            } else if (var2 == 0) {
                                this.subSlots.add(new Stitcher.Region(this.originX, this.originY + var1, var0, var3));
                            } else if (var3 == 0) {
                                this.subSlots.add(new Stitcher.Region(this.originX + var0, this.originY, var2, var1));
                            }
                        }

                        for(Stitcher.Region var6 : this.subSlots) {
                            if (var6.add(param0)) {
                                return true;
                            }
                        }

                        return false;
                    }
                } else {
                    return false;
                }
            }
        }

        public void walk(Consumer<Stitcher.Region> param0) {
            if (this.holder != null) {
                param0.accept(this);
            } else if (this.subSlots != null) {
                for(Stitcher.Region var0 : this.subSlots) {
                    var0.walk(param0);
                }
            }

        }

        @Override
        public String toString() {
            return "Slot{originX="
                + this.originX
                + ", originY="
                + this.originY
                + ", width="
                + this.width
                + ", height="
                + this.height
                + ", texture="
                + this.holder
                + ", subSlots="
                + this.subSlots
                + '}';
        }
    }
}
