package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GpuWarnlistManager extends SimplePreparableReloadListener<GpuWarnlistManager.Preparations> {
    private static final ResourceLocation GPU_WARNLIST_LOCATION = new ResourceLocation("gpu_warnlist.json");
    private ImmutableMap<String, String> warnings = ImmutableMap.of();

    public boolean hasWarnings() {
        return !this.warnings.isEmpty();
    }

    @Nullable
    public String getRendererWarnings() {
        return this.warnings.get("renderer");
    }

    @Nullable
    public String getVersionWarnings() {
        return this.warnings.get("version");
    }

    @Nullable
    public String getVendorWarnings() {
        return this.warnings.get("vendor");
    }

    protected GpuWarnlistManager.Preparations prepare(ResourceManager param0, ProfilerFiller param1) {
        List<Pattern> var0 = Lists.newArrayList();
        List<Pattern> var1 = Lists.newArrayList();
        List<Pattern> var2 = Lists.newArrayList();
        param1.startTick();
        JsonObject var3 = parseJson(param0, param1);
        if (var3 != null) {
            param1.push("compile_regex");
            compilePatterns(var3.getAsJsonArray("renderer"), var0);
            compilePatterns(var3.getAsJsonArray("version"), var1);
            compilePatterns(var3.getAsJsonArray("vendor"), var2);
            param1.pop();
        }

        param1.endTick();
        return new GpuWarnlistManager.Preparations(var0, var1, var2);
    }

    protected void apply(GpuWarnlistManager.Preparations param0, ResourceManager param1, ProfilerFiller param2) {
        this.warnings = param0.apply();
    }

    private static void compilePatterns(JsonArray param0, List<Pattern> param1) {
        param0.forEach(param1x -> param1.add(Pattern.compile(param1x.getAsString(), 2)));
    }

    @Nullable
    private static JsonObject parseJson(ResourceManager param0, ProfilerFiller param1) {
        param1.push("parse_json");
        JsonObject var0 = null;

        try (
            Resource var1 = param0.getResource(GPU_WARNLIST_LOCATION);
            BufferedReader var2 = new BufferedReader(new InputStreamReader(var1.getInputStream(), StandardCharsets.UTF_8));
        ) {
            var0 = new JsonParser().parse(var2).getAsJsonObject();
        } catch (IOException var35) {
        }

        param1.pop();
        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Preparations {
        private final List<Pattern> rendererPatterns;
        private final List<Pattern> versionPatterns;
        private final List<Pattern> vendorPatterns;

        private Preparations(List<Pattern> param0, List<Pattern> param1, List<Pattern> param2) {
            this.rendererPatterns = param0;
            this.versionPatterns = param1;
            this.vendorPatterns = param2;
        }

        private static String matchAny(List<Pattern> param0, String param1) {
            List<String> var0 = Lists.newArrayList();

            for(Pattern var1 : param0) {
                Matcher var2 = var1.matcher(param1);

                while(var2.find()) {
                    var0.add(var2.group());
                }
            }

            return String.join(", ", var0);
        }

        private ImmutableMap<String, String> apply() {
            Builder<String, String> var0 = new Builder<>();
            String var1 = matchAny(this.rendererPatterns, GlUtil.getRenderer());
            if (!var1.isEmpty()) {
                var0.put("renderer", var1);
            }

            String var2 = matchAny(this.versionPatterns, GlUtil.getOpenGLVersion());
            if (!var2.isEmpty()) {
                var0.put("version", var2);
            }

            String var3 = matchAny(this.vendorPatterns, GlUtil.getVendor());
            if (!var3.isEmpty()) {
                var0.put("vendor", var3);
            }

            return var0.build();
        }
    }
}
