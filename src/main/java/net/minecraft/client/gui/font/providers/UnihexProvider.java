package net.minecraft.client.gui.font.providers;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class UnihexProvider implements GlyphProvider {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int GLYPH_HEIGHT = 16;
    private static final int DIGITS_PER_BYTE = 2;
    private static final int DIGITS_FOR_WIDTH_8 = 32;
    private static final int DIGITS_FOR_WIDTH_16 = 64;
    private static final int DIGITS_FOR_WIDTH_24 = 96;
    private static final int DIGITS_FOR_WIDTH_32 = 128;
    private final CodepointMap<UnihexProvider.Glyph> glyphs;

    UnihexProvider(CodepointMap<UnihexProvider.Glyph> param0) {
        this.glyphs = param0;
    }

    @Nullable
    @Override
    public GlyphInfo getGlyph(int param0) {
        return this.glyphs.get(param0);
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return this.glyphs.keySet();
    }

    @VisibleForTesting
    static void unpackBitsToBytes(IntBuffer param0, int param1, int param2, int param3) {
        int var0 = 32 - param2 - 1;
        int var1 = 32 - param3 - 1;

        for(int var2 = var0; var2 >= var1; --var2) {
            if (var2 < 32 && var2 >= 0) {
                boolean var3 = (param1 >> var2 & 1) != 0;
                param0.put(var3 ? -1 : 0);
            } else {
                param0.put(0);
            }
        }

    }

    static void unpackBitsToBytes(IntBuffer param0, UnihexProvider.LineData param1, int param2, int param3) {
        for(int var0 = 0; var0 < 16; ++var0) {
            int var1 = param1.line(var0);
            unpackBitsToBytes(param0, var1, param2, param3);
        }

    }

    @VisibleForTesting
    static void readFromStream(InputStream param0, UnihexProvider.ReaderOutput param1) throws IOException {
        int var0 = 0;
        ByteList var1 = new ByteArrayList(128);

        while(true) {
            boolean var2 = copyUntil(param0, var1, 58);
            int var3 = var1.size();
            if (var3 == 0 && !var2) {
                return;
            }

            if (!var2 || var3 != 4 && var3 != 5 && var3 != 6) {
                throw new IllegalArgumentException("Invalid entry at line " + var0 + ": expected 4, 5 or 6 hex digits followed by a colon");
            }

            int var4 = 0;

            for(int var5 = 0; var5 < var3; ++var5) {
                var4 = var4 << 4 | decodeHex(var0, var1.getByte(var5));
            }

            var1.clear();
            copyUntil(param0, var1, 10);
            int var6 = var1.size();

            UnihexProvider.LineData var7 = switch(var6) {
                case 32 -> UnihexProvider.ByteContents.read(var0, var1);
                case 64 -> UnihexProvider.ShortContents.read(var0, var1);
                case 96 -> UnihexProvider.IntContents.read24(var0, var1);
                case 128 -> UnihexProvider.IntContents.read32(var0, var1);
                default -> throw new IllegalArgumentException(
                "Invalid entry at line " + var0 + ": expected hex number describing (8,16,24,32) x 16 bitmap, followed by a new line"
            );
            };
            param1.accept(var4, var7);
            ++var0;
            var1.clear();
        }
    }

    static int decodeHex(int param0, ByteList param1, int param2) {
        return decodeHex(param0, param1.getByte(param2));
    }

    private static int decodeHex(int param0, byte param1) {
        return switch(param1) {
            case 48 -> 0;
            case 49 -> 1;
            case 50 -> 2;
            case 51 -> 3;
            case 52 -> 4;
            case 53 -> 5;
            case 54 -> 6;
            case 55 -> 7;
            case 56 -> 8;
            case 57 -> 9;
            default -> throw new IllegalArgumentException("Invalid entry at line " + param0 + ": expected hex digit, got " + (char)param1);
            case 65 -> 10;
            case 66 -> 11;
            case 67 -> 12;
            case 68 -> 13;
            case 69 -> 14;
            case 70 -> 15;
        };
    }

    private static boolean copyUntil(InputStream param0, ByteList param1, int param2) throws IOException {
        while(true) {
            int var0 = param0.read();
            if (var0 == -1) {
                return false;
            }

            if (var0 == param2) {
                return true;
            }

            param1.add((byte)var0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record ByteContents(byte[] contents) implements UnihexProvider.LineData {
        @Override
        public int line(int param0) {
            return this.contents[param0] << 24;
        }

        static UnihexProvider.LineData read(int param0, ByteList param1) {
            byte[] var0 = new byte[16];
            int var1 = 0;

            for(int var2 = 0; var2 < 16; ++var2) {
                int var3 = UnihexProvider.decodeHex(param0, param1, var1++);
                int var4 = UnihexProvider.decodeHex(param0, param1, var1++);
                byte var5 = (byte)(var3 << 4 | var4);
                var0[var2] = var5;
            }

            return new UnihexProvider.ByteContents(var0);
        }

        @Override
        public int bitWidth() {
            return 8;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Definition implements GlyphProviderDefinition {
        public static final MapCodec<UnihexProvider.Definition> CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        ResourceLocation.CODEC.fieldOf("hex_file").forGetter(param0x -> param0x.hexFile),
                        UnihexProvider.OverrideRange.CODEC.listOf().fieldOf("size_overrides").forGetter(param0x -> param0x.sizeOverrides)
                    )
                    .apply(param0, UnihexProvider.Definition::new)
        );
        private final ResourceLocation hexFile;
        private final List<UnihexProvider.OverrideRange> sizeOverrides;

        private Definition(ResourceLocation param0, List<UnihexProvider.OverrideRange> param1) {
            this.hexFile = param0;
            this.sizeOverrides = param1;
        }

        @Override
        public GlyphProviderType type() {
            return GlyphProviderType.UNIHEX;
        }

        @Override
        public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
            return Either.left(this::load);
        }

        private GlyphProvider load(ResourceManager param0) throws IOException {
            UnihexProvider var3;
            try (InputStream var0 = param0.open(this.hexFile)) {
                var3 = this.loadData(var0);
            }

            return var3;
        }

        private UnihexProvider loadData(InputStream param0) throws IOException {
            CodepointMap<UnihexProvider.LineData> var0 = new CodepointMap<>(
                param0x -> new UnihexProvider.LineData[param0x], param0x -> new UnihexProvider.LineData[param0x][]
            );
            UnihexProvider.ReaderOutput var1 = var0::put;

            UnihexProvider var17;
            try (ZipInputStream var2 = new ZipInputStream(param0)) {
                ZipEntry var3;
                while((var3 = var2.getNextEntry()) != null) {
                    String var4 = var3.getName();
                    if (var4.endsWith(".hex")) {
                        UnihexProvider.LOGGER.info("Found {}, loading", var4);
                        UnihexProvider.readFromStream(new FastBufferedInputStream(var2), var1);
                    }
                }

                CodepointMap<UnihexProvider.Glyph> var5 = new CodepointMap<>(
                    param0x -> new UnihexProvider.Glyph[param0x], param0x -> new UnihexProvider.Glyph[param0x][]
                );

                for(UnihexProvider.OverrideRange var6 : this.sizeOverrides) {
                    int var7 = var6.from;
                    int var8 = var6.to;
                    UnihexProvider.Dimensions var9 = var6.dimensions;

                    for(int var10 = var7; var10 <= var8; ++var10) {
                        UnihexProvider.LineData var11 = var0.remove(var10);
                        if (var11 != null) {
                            var5.put(var10, new UnihexProvider.Glyph(var11, var9.left, var9.right));
                        }
                    }
                }

                var0.forEach((param1, param2) -> {
                    int var0x = param2.calculateWidth();
                    int var1x = UnihexProvider.Dimensions.left(var0x);
                    int var2x = UnihexProvider.Dimensions.right(var0x);
                    var5.put(param1, new UnihexProvider.Glyph(param2, var1x, var2x));
                });
                var17 = new UnihexProvider(var5);
            }

            return var17;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Dimensions(int left, int right) {
        public static final MapCodec<UnihexProvider.Dimensions> MAP_CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        Codec.INT.fieldOf("left").forGetter(UnihexProvider.Dimensions::left),
                        Codec.INT.fieldOf("right").forGetter(UnihexProvider.Dimensions::right)
                    )
                    .apply(param0, UnihexProvider.Dimensions::new)
        );
        public static final Codec<UnihexProvider.Dimensions> CODEC = MAP_CODEC.codec();

        public int pack() {
            return pack(this.left, this.right);
        }

        public static int pack(int param0, int param1) {
            return (param0 & 0xFF) << 8 | param1 & 0xFF;
        }

        public static int left(int param0) {
            return (byte)(param0 >> 8);
        }

        public static int right(int param0) {
            return (byte)param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record Glyph(UnihexProvider.LineData contents, int left, int right) implements GlyphInfo {
        public int width() {
            return this.right - this.left + 1;
        }

        @Override
        public float getAdvance() {
            return (float)(this.width() / 2 + 1);
        }

        @Override
        public float getShadowOffset() {
            return 0.5F;
        }

        @Override
        public float getBoldOffset() {
            return 0.5F;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> param0) {
            return param0.apply(new SheetGlyphInfo() {
                @Override
                public float getOversample() {
                    return 2.0F;
                }

                @Override
                public int getPixelWidth() {
                    return Glyph.this.width();
                }

                @Override
                public int getPixelHeight() {
                    return 16;
                }

                @Override
                public void upload(int param0, int param1) {
                    IntBuffer var0 = MemoryUtil.memAllocInt(Glyph.this.width() * 16);
                    UnihexProvider.unpackBitsToBytes(var0, Glyph.this.contents, Glyph.this.left, Glyph.this.right);
                    var0.rewind();
                    GlStateManager.upload(0, param0, param1, Glyph.this.width(), 16, NativeImage.Format.RGBA, var0, MemoryUtil::memFree);
                }

                @Override
                public boolean isColored() {
                    return true;
                }
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record IntContents(int[] contents, int bitWidth) implements UnihexProvider.LineData {
        private static final int SIZE_24 = 24;

        @Override
        public int line(int param0) {
            return this.contents[param0];
        }

        static UnihexProvider.LineData read24(int param0, ByteList param1) {
            int[] var0 = new int[16];
            int var1 = 0;
            int var2 = 0;

            for(int var3 = 0; var3 < 16; ++var3) {
                int var4 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var5 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var6 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var7 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var8 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var9 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var10 = var4 << 20 | var5 << 16 | var6 << 12 | var7 << 8 | var8 << 4 | var9;
                var0[var3] = var10 << 8;
                var1 |= var10;
            }

            return new UnihexProvider.IntContents(var0, 24);
        }

        public static UnihexProvider.LineData read32(int param0, ByteList param1) {
            int[] var0 = new int[16];
            int var1 = 0;
            int var2 = 0;

            for(int var3 = 0; var3 < 16; ++var3) {
                int var4 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var5 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var6 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var7 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var8 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var9 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var10 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var11 = UnihexProvider.decodeHex(param0, param1, var2++);
                int var12 = var4 << 28 | var5 << 24 | var6 << 20 | var7 << 16 | var8 << 12 | var9 << 8 | var10 << 4 | var11;
                var0[var3] = var12;
                var1 |= var12;
            }

            return new UnihexProvider.IntContents(var0, 32);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface LineData {
        int line(int var1);

        int bitWidth();

        default int mask() {
            int var0 = 0;

            for(int var1 = 0; var1 < 16; ++var1) {
                var0 |= this.line(var1);
            }

            return var0;
        }

        default int calculateWidth() {
            int var0 = this.mask();
            int var1 = this.bitWidth();
            int var2;
            int var3;
            if (var0 == 0) {
                var2 = 0;
                var3 = var1;
            } else {
                var2 = Integer.numberOfLeadingZeros(var0);
                var3 = 32 - Integer.numberOfTrailingZeros(var0) - 1;
            }

            return UnihexProvider.Dimensions.pack(var2, var3);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record OverrideRange(int from, int to, UnihexProvider.Dimensions dimensions) {
        private static final Codec<UnihexProvider.OverrideRange> RAW_CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.CODEPOINT.fieldOf("from").forGetter(UnihexProvider.OverrideRange::from),
                        ExtraCodecs.CODEPOINT.fieldOf("to").forGetter(UnihexProvider.OverrideRange::to),
                        UnihexProvider.Dimensions.MAP_CODEC.forGetter(UnihexProvider.OverrideRange::dimensions)
                    )
                    .apply(param0, UnihexProvider.OverrideRange::new)
        );
        public static final Codec<UnihexProvider.OverrideRange> CODEC = ExtraCodecs.validate(
            RAW_CODEC,
            param0 -> param0.from >= param0.to ? DataResult.error(() -> "Invalid range: [" + param0.from + ";" + param0.to + "]") : DataResult.success(param0)
        );
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface ReaderOutput {
        void accept(int var1, UnihexProvider.LineData var2);
    }

    @OnlyIn(Dist.CLIENT)
    static record ShortContents(short[] contents) implements UnihexProvider.LineData {
        @Override
        public int line(int param0) {
            return this.contents[param0] << 16;
        }

        static UnihexProvider.LineData read(int param0, ByteList param1) {
            short[] var0 = new short[16];
            int var1 = 0;

            for(int var2 = 0; var2 < 16; ++var2) {
                int var3 = UnihexProvider.decodeHex(param0, param1, var1++);
                int var4 = UnihexProvider.decodeHex(param0, param1, var1++);
                int var5 = UnihexProvider.decodeHex(param0, param1, var1++);
                int var6 = UnihexProvider.decodeHex(param0, param1, var1++);
                short var7 = (short)(var3 << 12 | var4 << 8 | var5 << 4 | var6);
                var0[var2] = var7;
            }

            return new UnihexProvider.ShortContents(var0);
        }

        @Override
        public int bitWidth() {
            return 16;
        }
    }
}
