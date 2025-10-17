package studio.dreamys.prometheus.util;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// HUGE shoutout to essential's LoaderInternals for this entire class

public class LoaderInternals {
    protected ClassLoader modClassLoader;
    private final Logger logger;
    public LoaderInternals(ClassLoader modClassLoader, Logger logger) {
        this.modClassLoader = modClassLoader;
        this.logger = logger;
    }
    private Class<?> findImplClass(String name) throws ClassNotFoundException {
        try {
            return Class.forName("net.fabricmc.loader.impl." + name);
        } catch (ClassNotFoundException var3) {
            return Class.forName("net.fabricmc.loader." + name);
        }
    }

    public void addToClassLoader(URL url) {
        try {
            addToClassLoaderViaFabricLauncherBase(url);
        } catch (Throwable t) {
            logger.warn("Failed to add URL to classpath via FabricLauncherBase:", t);

            try {
                addToClassLoaderViaReflection(url);
            } catch (Throwable t2) {
                logger.warn("Failed to add URL to classpath via classloader reflection:", t2);
                throw new RuntimeException("Failed to add jar to ClassLoader. See preceding exception(s).");
            }
        }
    }

    public void addToClassLoaderViaFabricLauncherBase(URL url) {
        FabricLauncherBase.getLauncher().propose(url);
    }

    public void addToClassLoaderViaReflection(URL url) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ClassLoader classLoader = this.modClassLoader;
        if (classLoader == null) {
            throw new IllegalStateException("Failed to traverse class loader hierarchy to find mod class loader.");
        } else {
            Method method = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
        }
    }

    public ModMetadata parseModMetadata(Path modPath, Path fabricJson) throws Exception {
        Class<?> ModMetadataParser = this.findImplClass("metadata.ModMetadataParser");

        try {
            return (ModMetadata)ModMetadataParser.getDeclaredMethod("parseMetadata", Logger.class, Path.class).invoke((Object)null, this.logger, fabricJson);
        } catch (NoSuchMethodException var31) {
            ModMetadata var35;
            try (InputStream in = Files.newInputStream(fabricJson)) {
                try {
                    ModMetadata var7 = (ModMetadata)ModMetadataParser.getDeclaredMethod("parseMetadata", InputStream.class, String.class, List.class).invoke((Object)null, in, modPath.toString(), Collections.emptyList());
                    return var7;
                } catch (NoSuchMethodException var27) {
                }

                try {
                    Class<?> VersionOverrides = this.findImplClass("metadata.VersionOverrides");
                    Class<?> DependencyOverrides = this.findImplClass("metadata.DependencyOverrides");
                    Object versionOverrides = VersionOverrides.getConstructor().newInstance();
                    Object dependencyOverrides = DependencyOverrides.getConstructor(Path.class).newInstance(Paths.get("_invalid_"));
                    var35 = (ModMetadata)ModMetadataParser.getDeclaredMethod("parseMetadata", InputStream.class, String.class, List.class, VersionOverrides, DependencyOverrides).invoke((Object)null, in, modPath.toString(), Collections.emptyList(), versionOverrides, dependencyOverrides);
                } catch (NoSuchMethodException var28) {
                    Class<?> VersionOverrides = this.findImplClass("metadata.VersionOverrides");
                    Class<?> DependencyOverrides = this.findImplClass("metadata.DependencyOverrides");
                    Object versionOverrides = VersionOverrides.getConstructor().newInstance();
                    Object dependencyOverrides = DependencyOverrides.getConstructor(Path.class).newInstance(Paths.get("_invalid_"));
                    ModMetadata var13 = (ModMetadata)ModMetadataParser.getDeclaredMethod("parseMetadata", InputStream.class, String.class, List.class, VersionOverrides, DependencyOverrides, Boolean.TYPE).invoke((Object)null, in, modPath.toString(), Collections.emptyList(), versionOverrides, dependencyOverrides, FabricLoader.getInstance().isDevelopmentEnvironment());
                    return var13;
                }
            }

            return var35;
        }
    }

    public void remapMod(ModMetadata metadata, Path inputPath, Path outputPath) throws Exception {
        Class<?> ModCandidate;
        try {
            ModCandidate = this.findImplClass("discovery.ModCandidateImpl");
        } catch (ClassNotFoundException var41) {
            ModCandidate = this.findImplClass("discovery.ModCandidate");
        }

        Class<?> ModResolver = this.findImplClass("discovery.ModResolver");
        Class<?> RuntimeModRemapper = this.findImplClass("discovery.RuntimeModRemapper");
        Object candidate = this.createCandidate(inputPath, inputPath.toUri().toURL(), metadata);

        try {
            Method getInMemoryFs = ModResolver.getDeclaredMethod("getInMemoryFs");
            Method remap = RuntimeModRemapper.getDeclaredMethod("remap", Collection.class, FileSystem.class);
            Method getOriginUrl = ModCandidate.getDeclaredMethod("getOriginUrl");
            FileSystem fileSystem = (FileSystem)getInMemoryFs.invoke((Object)null);
            Object result = remap.invoke((Object)null, Collections.singleton(candidate), fileSystem);
            Object remappedCandidate = ((Collection)result).iterator().next();
            URL remappedUrl = (URL)getOriginUrl.invoke(remappedCandidate);

            try (InputStream in = remappedUrl.openStream()) {
                Files.copy(in, outputPath, new CopyOption[0]);
            }
        } catch (NoSuchMethodException var43) {
            Method remap = RuntimeModRemapper.getDeclaredMethod("remap", Collection.class, Path.class, Path.class);
            Path tmpDir = Files.createTempDirectory("remap-tmp");
            Path outDir = Files.createTempDirectory("remap-out");

            try {
                remap.invoke((Object)null, Collections.singleton(candidate), tmpDir, outDir);

                Path resultPath;
                try {
                    Method getPath = ModCandidate.getDeclaredMethod("getPath");
                    resultPath = (Path)getPath.invoke(candidate);
                } catch (NoSuchMethodException var37) {
                    Method getPaths = ModCandidate.getDeclaredMethod("getPaths");
                    List<Path> paths = (List)getPaths.invoke(candidate);
                    resultPath = (Path)paths.get(0);
                }

                Files.move(resultPath, outputPath);
            } finally {
                MoreFiles.deleteRecursively(tmpDir, new RecursiveDeleteOption[]{RecursiveDeleteOption.ALLOW_INSECURE});
                MoreFiles.deleteRecursively(outDir, new RecursiveDeleteOption[]{RecursiveDeleteOption.ALLOW_INSECURE});
            }
        }

    }

    private Object createCandidate(Path path, URL url, Object metadata) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Class<?> LoaderModMetadata = this.findImplClass("metadata.LoaderModMetadata");

        Class<?> ModCandidate;
        try {
            ModCandidate = this.findImplClass("discovery.ModCandidateImpl");
        } catch (ClassNotFoundException var11) {
            ModCandidate = this.findImplClass("discovery.ModCandidate");
        }

        try {
            return ModCandidate.getConstructor(LoaderModMetadata, URL.class, Integer.TYPE, Boolean.TYPE).newInstance(metadata, url, 0, true);
        } catch (NoSuchMethodException var10) {
            try {
                Method createCandidate = ModCandidate.getDeclaredMethod("createPlain", Path.class, LoaderModMetadata, Boolean.TYPE, Collection.class);
                createCandidate.setAccessible(true);
                return createCandidate.invoke((Object)null, path, metadata, true, Collections.emptyList());
            } catch (NoSuchMethodException var9) {
                Method createCandidate = ModCandidate.getDeclaredMethod("createPlain", List.class, LoaderModMetadata, Boolean.TYPE, Collection.class);
                createCandidate.setAccessible(true);
                return createCandidate.invoke((Object)null, Collections.singletonList(path), metadata, true, Collections.emptyList());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void injectFakeMod(Path path, URL url, ModMetadata metadata) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        FabricLoader fabricLoader = FabricLoader.getInstance();
        Class<? extends FabricLoader> fabricLoaderClass = fabricLoader.getClass();

        Class<?> ModContainerImpl;
        try {
            ModContainerImpl = this.findImplClass("ModContainerImpl");
        } catch (ClassNotFoundException var28) {
            ModContainerImpl = this.findImplClass("ModContainer");
        }

        Class<?> LoaderModMetadata = this.findImplClass("metadata.LoaderModMetadata");
        Class<?> EntrypointMetadata = this.findImplClass("metadata.EntrypointMetadata");
        Field modMapField = fabricLoaderClass.getDeclaredField("modMap");
        Field modsField = fabricLoaderClass.getDeclaredField("mods");
        Field entrypointStorageField = fabricLoaderClass.getDeclaredField("entrypointStorage");
        Field adapterMapField = fabricLoaderClass.getDeclaredField("adapterMap");
        modMapField.setAccessible(true);
        modsField.setAccessible(true);
        entrypointStorageField.setAccessible(true);
        adapterMapField.setAccessible(true);
        List<Object> mods = (List)modsField.get(fabricLoader);
        Map<String, Object> modMap = (Map)modMapField.get(fabricLoader);
        Object entrypointStorage = entrypointStorageField.get(fabricLoader);
        Map<String, LanguageAdapter> adapterMap = (Map)adapterMapField.get(fabricLoader);
        Method getMixinConfigs = LoaderModMetadata.getDeclaredMethod("getMixinConfigs", EnvType.class);

        for(String mixinConfig : (Collection<String>) getMixinConfigs.invoke(metadata, EnvType.CLIENT)) {
            Mixins.addConfiguration(mixinConfig);
        }

        Object modContainer;
        try {
            modContainer = ModContainerImpl.getConstructor(LoaderModMetadata, URL.class).newInstance(metadata, url);
        } catch (NoSuchMethodException var27) {
            try {
                modContainer = ModContainerImpl.getConstructor(LoaderModMetadata, Path.class).newInstance(metadata, path);
            } catch (NoSuchMethodException var26) {
                Object modCandidate = this.createCandidate(path, url, metadata);
                modContainer = ModContainerImpl.getConstructor(modCandidate.getClass()).newInstance(modCandidate);
            }
        }

        mods.add(modContainer);
        modMap.put(metadata.getId(), modContainer);
        Method addMethod = entrypointStorage.getClass().getDeclaredMethod("add", ModContainerImpl, String.class, EntrypointMetadata, Map.class);
        addMethod.setAccessible(true);
        Method getEntrypointKeys = LoaderModMetadata.getDeclaredMethod("getEntrypointKeys");
        Method getEntrypoints = LoaderModMetadata.getDeclaredMethod("getEntrypoints", String.class);

        for(String key : (Collection<String>) getEntrypointKeys.invoke(metadata)) {
            for(Object entrypointMetadata : (List)getEntrypoints.invoke(metadata, key)) {
                addMethod.invoke(entrypointStorage, modContainer, key, entrypointMetadata, adapterMap);
            }
        }

    }
}