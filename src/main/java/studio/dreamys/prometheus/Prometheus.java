package studio.dreamys.prometheus;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Prometheus implements ClientModInitializer {
    FilenameFilter isJar = (dir, name) -> name.endsWith(".jar");
    @Override
    public void onInitializeClient() {
        System.out.println("Test from prometheus");
        if (!FabricLoader.getInstance().isModLoaded("essential-container") && FabricLoader.getInstance().isModLoaded("essential")) {
            System.out.println("Essential is already loaded");
            return;
        }
        File modsDir = new File("mods");
        assert modsDir.exists();
        Path essentialDir = modsDir.toPath().toAbsolutePath().getParent().resolve("essential");
        System.out.println("Essential Directory: " + essentialDir.toAbsolutePath());
        Path symlinkJarPath = modsDir.toPath();
        // Create symlink
        if (!essentialDir.toFile().exists()) {
            return; // Essential not found, let it create itself
        }
        for (File file : Objects.requireNonNull(essentialDir.toFile().listFiles(isJar))) {
            try {
                Path symlinkJar = symlinkJarPath.resolve(file.getName());
                if (symlinkJar.toFile().exists()) {
                    System.out.println("Symlink already exists: " + symlinkJar.toAbsolutePath());
                    break;
                }
                Files.createSymbolicLink(symlinkJar, file.toPath());
                System.out.println("Created symlink: " + symlinkJar.toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}