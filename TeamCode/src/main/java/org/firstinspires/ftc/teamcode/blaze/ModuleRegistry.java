package org.firstinspires.ftc.teamcode.blaze;

import android.content.Context;

import org.firstinspires.ftc.teamcode.blaze.Anotations.BlazeModule;
import org.firstinspires.ftc.teamcode.blaze.Anotations.InitWithTelemetry;
import org.firstinspires.ftc.teamcode.blaze.Anotations.InitWithoutTelemetry;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.BlazingModule;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class ModuleRegistry {

    private static class ModuleInfo {
        final Class<? extends BlazingModule> type;
        final String name;
        BlazingModule instance;

        ModuleInfo(Class<? extends BlazingModule> type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    private static final Map<String, ModuleInfo> registry = new LinkedHashMap<>();

    private static final String PACKAGE_NAME = "org.firstinspires.ftc.teamcode";
    private static final String CACHE_FILE_NAME = "blaze_modules.cache";

    private ModuleRegistry() {}

    /**
     * Сканирует все @HotModule и регистрирует классы
     */
    public static void scanAndRegisterAll(HardwareMap hardwareMap) {
        registry.clear();
        Context context = hardwareMap.appContext;
        File cacheFile = new File(context.getFilesDir(), CACHE_FILE_NAME);
        File apkFile = new File(context.getPackageCodePath());

        List<String> validClasses = new ArrayList<>();
        boolean cacheHit = false;

        // 1. Проверяем, есть ли валидный кэш (файл существует и APK не новее его)
        if (cacheFile.exists() && cacheFile.lastModified() >= apkFile.lastModified()) {
            cacheHit = true;
            BlazeLogger.addDefaultLog(ModuleRegistry.class.getSimpleName(),"Found cache file,reading ");
            try (BufferedReader reader = new BufferedReader(new FileReader(cacheFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        BlazeLogger.addDefaultLog(ModuleRegistry.class.getSimpleName(),"Found cached module: " + line.trim().substring(line.lastIndexOf(".") + 1));
                        validClasses.add(line.trim());
                    }
                }
            }  catch (IOException e) {
                BlazeLogger.addErrorLog(ModuleRegistry.class.getSimpleName(), "Cache read failed, falling back to scan: " + e.getMessage());
                cacheHit = false;
            } catch (Exception e) { // Ловим любые неожиданные ошибки парсинга
                BlazeLogger.addErrorLog(ModuleRegistry.class.getSimpleName(), "Cache parsing error, falling back to scan: " + e.getMessage());
                cacheHit = false;
            }
        }

        long msTimeStart = System.currentTimeMillis();
        // 2. Если кэша нет или он устарел — делаем медленное сканирование APK
        if (!cacheHit) {
            BlazeLogger.addDefaultLog(ModuleRegistry.class.getSimpleName(),"No cache/cache invalid,searching ");
            validClasses.clear();
            dalvik.system.DexFile dexFile = null;
            try {
                String apkPath = context.getPackageCodePath();
                dexFile = new dalvik.system.DexFile(apkPath);
                Enumeration<String> entries = dexFile.entries();
                ClassLoader classLoader = context.getClassLoader();

                while (entries.hasMoreElements()) {
                    String className = entries.nextElement();
                    if (!className.startsWith(PACKAGE_NAME)) continue;

                    try {
                        Class<?> clazz = Class.forName(className, false, classLoader);
                        if (clazz.isAnnotationPresent(BlazeModule.class) &&
                                BlazingModule.class.isAssignableFrom(clazz) &&
                                !Modifier.isAbstract(clazz.getModifiers())) {
                            BlazeLogger.addDefaultLog(ModuleRegistry.class.getSimpleName(),"Found module  " + className.trim().substring(className.lastIndexOf(".") + 1));
                            validClasses.add(className);
                        }
                    } catch (Throwable ignored) {}
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to scan modules: " + e.getMessage(), e);
            } finally {
                if (dexFile != null) {
                    try { dexFile.close(); } catch (IOException ignored) {}
                }
            }
            long timeScan = System.currentTimeMillis() - msTimeStart;
            BlazeLogger.addDefaultLog(ModuleRegistry.class.getSimpleName(),"Scanned all modules,took " + timeScan + " ms");

            // 3. Сохраняем результат в файл для следующих запусков
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFile))) {
                for (String className : validClasses) {
                    writer.write(className);
                    writer.newLine();
                }
            } catch (IOException e) {
                // Игнорируем ошибки записи, в следующий раз просто просканируем снова
            }
        }

        // 4. Регистрируем найденные классы (быстрая операция)
        ClassLoader classLoader = context.getClassLoader();
        for (String className : validClasses) {
            try {
                Class<?> clazz = Class.forName(className, false, classLoader);
                if (clazz.isAnnotationPresent(BlazeModule.class)){
                    BlazeModule annotation = clazz.getAnnotation(BlazeModule.class);
                    String moduleName = annotation.name();
                    if (moduleName.isEmpty()) {
                        moduleName = clazz.getSimpleName();
                    }
                    BlazeLogger.addDefaultLog(ModuleRegistry.class.getSimpleName(), "Registring module  " + moduleName);
                    if (registry.containsKey(moduleName)) {
                        throw new RuntimeException(
                                "Duplicate module name: '" + moduleName + "'\n" +
                                        "  First: " + registry.get(moduleName).type.getSimpleName() + "\n" +
                                        "  Second: " + clazz.getSimpleName()
                        );
                    }
                    registry.put(moduleName, new ModuleInfo((Class<? extends BlazingModule>) clazz, moduleName));
                }
            } catch (Throwable ignored) {}
        }
    }

    public static void removeCache(HardwareMap hardwareConfig){
        Context context = hardwareConfig.appContext;
        File cacheFile = new File(context.getFilesDir(), CACHE_FILE_NAME);
        cacheFile.delete();
    }

    /**
     * Создаёт все зарегистрированные модули
     */
    public static void createAll(MultiDashTelemetry telemetry) {
        for (ModuleInfo info : registry.values()) {
            if (info.instance != null) continue;
            try {
                java.lang.reflect.Constructor<?> constructor = info.type.getDeclaredConstructor();
                constructor.setAccessible(true);

                info.instance = (BlazingModule) constructor.newInstance();
                String moduleName = info.name;

                if (info.type.getAnnotation(InitWithTelemetry.class) != null && telemetry != null){
                    BlazeLogger.addDefaultLog(ModuleRegistry.class.getSimpleName(),"Creating module " + info.name + " with telemetry");
                    info.instance.autowireSelf(moduleName,telemetry);
                } else if (info.type.getAnnotation(InitWithoutTelemetry.class) != null || telemetry == null) {
                    BlazeLogger.addDefaultLog(ModuleRegistry.class.getSimpleName(),"Creating module " + info.name + " without telemetry");
                    info.instance.autowireSelf(moduleName,null);
                }


            } catch (Exception e) {
                Throwable realCause = e;
                if (e instanceof java.lang.reflect.InvocationTargetException) {
                    realCause = ((java.lang.reflect.InvocationTargetException) e).getTargetException();
                }

                String message = realCause.getMessage() != null ? realCause.getMessage() : realCause.toString();

                throw new RuntimeException(
                        "Failed to create module: " + info.name +
                                " (type: " + info.type.getSimpleName() + ")\n" +
                                "Reason: " + message, realCause
                );
            }
        }
    }



    public static void registerInstance(String name, BlazingModule instance) {
        BlazeLogger.addDefaultLog(ModuleRegistry.class.getSimpleName(),"Put custom instance " + instance.getName());
        ModuleInfo info = registry.get(name);
        if (info == null) {
            throw new RuntimeException("Cannot register instance: Module '" + name + "' is not found in registry. Check @HotModule annotation.");
        }
        if (info.instance != null) {
            throw new RuntimeException("Module '" + name + "' already has an instance. Cannot overwrite.");
        }
        info.instance = instance;
    }

    public static BlazingModule get(String name) {
        ModuleInfo info = registry.get(name);
        if (info == null || info.instance == null) {
            throw new RuntimeException("Module not found or not initialized: " + name);
        }
        return info.instance;
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlazingModule> T get(Class<T> type) {
        BlazeLogger.addDefaultLog(ModuleRegistry.class.getSimpleName(),"Got get for " + type);
        for (ModuleInfo info : registry.values()) {
            if (info.instance != null && type.isInstance(info.instance)) {
                return (T) info.instance;
            }
        }
        throw new RuntimeException("Module not found: " + type.getSimpleName());
    }

    public static HashMap<String, BlazingModule> getAll() {
        HashMap<String, BlazingModule> modules = new HashMap<>();
        for (ModuleInfo info : registry.values()) {
            if (info.instance != null) {
                modules.put(info.name, info.instance);
            }
        }
        return modules;
    }

    public static void clear() {
        for (ModuleInfo info : registry.values()) {
            info.instance = null;
        }
        registry.clear();
    }

    public static int count() {
        return registry.size();
    }
}