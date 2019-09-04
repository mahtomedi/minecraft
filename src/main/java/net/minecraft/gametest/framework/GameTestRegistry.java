package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;

public class GameTestRegistry {
    private static final Collection<TestFunction> testFunctions = Lists.newArrayList();
    private static final Set<String> testClassNames = Sets.newHashSet();
    private static final Map<String, Consumer<ServerLevel>> beforeBatchFunctions = Maps.newHashMap();

    public static Collection<TestFunction> getTestFunctionsForClassName(String param0) {
        return testFunctions.stream().filter(param1 -> isTestFunctionPartOfClass(param1, param0)).collect(Collectors.toList());
    }

    public static Collection<TestFunction> getAllTestFunctions() {
        return testFunctions;
    }

    public static Collection<String> getAllTestClassNames() {
        return testClassNames;
    }

    public static boolean isTestClass(String param0) {
        return testClassNames.contains(param0);
    }

    @Nullable
    public static Consumer<ServerLevel> getBeforeBatchFunction(String param0) {
        return beforeBatchFunctions.get(param0);
    }

    public static Optional<TestFunction> findTestFunction(String param0) {
        return getAllTestFunctions().stream().filter(param1 -> param1.getTestName().equalsIgnoreCase(param0)).findFirst();
    }

    public static TestFunction getTestFunction(String param0) {
        Optional<TestFunction> var0 = findTestFunction(param0);
        if (!var0.isPresent()) {
            throw new IllegalArgumentException("Can't find the test function for " + param0);
        } else {
            return var0.get();
        }
    }

    private static boolean isTestFunctionPartOfClass(TestFunction param0, String param1) {
        return param0.getTestName().toLowerCase().startsWith(param1.toLowerCase() + ".");
    }
}
