package net.minecraft.client.gui.screens.inventory;

import java.util.Random;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantmentNames {
    private static final ResourceLocation ALT_FONT = new ResourceLocation("minecraft", "alt");
    private static final Style ROOT_STYLE = Style.EMPTY.withFont(ALT_FONT);
    private static final EnchantmentNames INSTANCE = new EnchantmentNames();
    private final Random random = new Random();
    private final String[] words = new String[]{
        "the",
        "elder",
        "scrolls",
        "klaatu",
        "berata",
        "niktu",
        "xyzzy",
        "bless",
        "curse",
        "light",
        "darkness",
        "fire",
        "air",
        "earth",
        "water",
        "hot",
        "dry",
        "cold",
        "wet",
        "ignite",
        "snuff",
        "embiggen",
        "twist",
        "shorten",
        "stretch",
        "fiddle",
        "destroy",
        "imbue",
        "galvanize",
        "enchant",
        "free",
        "limited",
        "range",
        "of",
        "towards",
        "inside",
        "sphere",
        "cube",
        "self",
        "other",
        "ball",
        "mental",
        "physical",
        "grow",
        "shrink",
        "demon",
        "elemental",
        "spirit",
        "animal",
        "creature",
        "beast",
        "humanoid",
        "undead",
        "fresh",
        "stale",
        "phnglui",
        "mglwnafh",
        "cthulhu",
        "rlyeh",
        "wgahnagl",
        "fhtagn",
        "baguette"
    };

    private EnchantmentNames() {
    }

    public static EnchantmentNames getInstance() {
        return INSTANCE;
    }

    public MutableComponent getRandomName(Font param0, int param1) {
        StringBuilder var0 = new StringBuilder();
        int var1 = this.random.nextInt(2) + 3;

        for(int var2 = 0; var2 < var1; ++var2) {
            if (var2 != 0) {
                var0.append(" ");
            }

            var0.append(Util.getRandom(this.words, this.random));
        }

        return param0.getSplitter().headByWidth(new TextComponent(var0.toString()).withStyle(ROOT_STYLE), param1, Style.EMPTY);
    }

    public void initSeed(long param0) {
        this.random.setSeed(param0);
    }
}
