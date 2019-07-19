package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.Font;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class EnchantmentNames {
    private static final EnchantmentNames INSTANCE = new EnchantmentNames();
    private final Random random = new Random();
    private final String[] words = "the elder scrolls klaatu berata niktu xyzzy bless curse light darkness fire air earth water hot dry cold wet ignite snuff embiggen twist shorten stretch fiddle destroy imbue galvanize enchant free limited range of towards inside sphere cube self other ball mental physical grow shrink demon elemental spirit animal creature beast humanoid undead fresh stale phnglui mglwnafh cthulhu rlyeh wgahnagl fhtagnbaguette"
        .split(" ");

    private EnchantmentNames() {
    }

    public static EnchantmentNames getInstance() {
        return INSTANCE;
    }

    public String getRandomName(Font param0, int param1) {
        int var0 = this.random.nextInt(2) + 3;
        String var1 = "";

        for(int var2 = 0; var2 < var0; ++var2) {
            if (var2 > 0) {
                var1 = var1 + " ";
            }

            var1 = var1 + this.words[this.random.nextInt(this.words.length)];
        }

        List<String> var3 = param0.split(var1, param1);
        return StringUtils.join((Iterable<?>)(var3.size() >= 2 ? var3.subList(0, 2) : var3), " ");
    }

    public void initSeed(long param0) {
        this.random.setSeed(param0);
    }
}
