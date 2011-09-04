package iron9light.coffeescriptMavenPlugin;

import org.apache.maven.plugin.MojoFailureException;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Compile coffeescript files to javascript files, and recompile as soon as a change occurs.
 *
 * @author iron9light
 * @goal watch
 */
public class CoffeeScriptWatchMojo extends CoffeeScriptMojoBase {
    /**
     * Delete javascript file when the coffeescript file deleted.
     *
     * @parameter default-value="true"
     */
    private Boolean allowedDelete;

    private static final WatchEvent.Kind<?>[] watchEvents = {StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE};

    @Override
    protected void doExecute(CoffeeScriptCompiler compiler, Path sourceDirectory, Path outputDirectory) throws Exception {
        try {
            compileCoffeeFilesInDir(compiler, sourceDirectory, outputDirectory);
        } catch (MojoFailureException ignored) {
        }
        watch(compiler, sourceDirectory, outputDirectory);
    }

    private void watch(final CoffeeScriptCompiler compiler, final Path sourceDirectory, final Path outputDirectory) throws IOException, InterruptedException {
        WatchService watchService = startWatching(sourceDirectory);
        for (boolean changed = true; ; ) {
            if (changed) {
                getLog().info("wait for changing...");
                changed = false;
            }

            WatchKey watchKey = watchService.take();
            Path dir = (Path) watchKey.watchable();

            for (WatchEvent<?> event : watchKey.pollEvents()) {
                Path file = dir.resolve((Path) event.context());
                getLog().debug(String.format("watched %s - %s", event.kind().name(), file));
//                // try to delete js folder when you delete cs folder. Not work is sub deep > 1.
//                if (allowedDelete && event.kind().name().equals(StandardWatchEventKinds.ENTRY_DELETE.name())) {
//                    Path jsDir = outputDirectory.resolve(sourceDirectory.relativize(file));
//                    try {
//                        if (Files.isDirectory(jsDir) && Files.deleteIfExists(jsDir)) {
//                            getLog().info(String.format("delete folder %s with %s", jsDir, file));
//                            continue;
//                        }
//                    } catch (DirectoryNotEmptyException | SecurityException ignored) {
//                        continue;
//                    }
//                } else
                if (Files.isDirectory(file)) {
                    getLog().debug("is directory");
                    if (event.kind().name().equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
                        // watch created folder.
                        file.register(watchService, watchEvents);
                        getLog().debug(String.format("watch %s", file));
                    }
                    continue;
                }

                if (!isCoffeeFile(file)) {
                    getLog().debug(String.format("skip non-coffeescript"));
                    continue;
                }

                String coffeeFileName = sourceDirectory.relativize(file).toString();
                String jsFileName = getJsFileName(coffeeFileName);
                Path jsFile = outputDirectory.resolve(jsFileName);

                if (event.kind().name().equals(StandardWatchEventKinds.ENTRY_DELETE.name())) {
                    if (allowedDelete && Files.deleteIfExists(jsFile)) {
                        getLog().info(String.format("deleted %s with %s", jsFileName, coffeeFileName));
                        changed = true;
                    }
                } else if (event.kind().name().equals(StandardWatchEventKinds.ENTRY_MODIFY.name()) || event.kind().name().equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
                    compileCoffeeFile(compiler, file, jsFile, coffeeFileName, jsFileName);
                    changed = true;
                }
            }

            watchKey.reset();
        }
    }

    private WatchService startWatching(Path sourceDirectory) throws IOException {
        final WatchService watchService = sourceDirectory.getFileSystem().newWatchService();

        Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watchService, watchEvents);
                getLog().debug(String.format("watch %s", dir));
                return FileVisitResult.CONTINUE;
            }
        });
        return watchService;
    }
}
