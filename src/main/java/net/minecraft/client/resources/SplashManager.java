package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SplashManager extends SimplePreparableReloadListener<List<String>> {
    private static final ResourceLocation SPLASHES_LOCATION = new ResourceLocation("texts/splashes.txt");
    private static final RandomSource RANDOM = RandomSource.create();
    private final List<String> splashes = Lists.newArrayList();
    private final User user;

    public SplashManager(User param0) {
        this.user = param0;
    }

    protected List<String> prepare(ResourceManager param0, ProfilerFiller param1) {
        try {
            List var4;
            try (BufferedReader var0 = Minecraft.getInstance().getResourceManager().openAsReader(SPLASHES_LOCATION)) {
                var4 = var0.lines().map(String::trim).filter(param0x -> param0x.hashCode() != 125780783).collect(Collectors.toList());
            }

            return var4;
        } catch (IOException var8) {
            return Collections.emptyList();
        }
    }

    protected void apply(List<String> param0, ResourceManager param1, ProfilerFiller param2) {
        this.splashes.clear();
        this.splashes.addAll(param0);
    }

    @Nullable
    public SplashRenderer getSplash() {
        Calendar var0 = Calendar.getInstance();
        var0.setTime(new Date());
        if (var0.get(2) + 1 == 12 && var0.get(5) == 24) {
            return SplashRenderer.CHRISTMAS;
        } else if (var0.get(2) + 1 == 1 && var0.get(5) == 1) {
            return SplashRenderer.NEW_YEAR;
        } else if (var0.get(2) + 1 == 10 && var0.get(5) == 31) {
            return SplashRenderer.HALLOWEEN;
        } else if (this.splashes.isEmpty()) {
            return null;
        } else {
            return this.user != null && RANDOM.nextInt(this.splashes.size()) == 42
                ? new SplashRenderer(this.user.getName().toUpperCase(Locale.ROOT) + " IS YOU")
                : new SplashRenderer(this.splashes.get(RANDOM.nextInt(this.splashes.size())));
        }
    }
}
