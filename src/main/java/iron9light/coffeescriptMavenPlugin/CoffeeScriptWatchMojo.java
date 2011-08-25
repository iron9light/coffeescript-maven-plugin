package iron9light.coffeescriptMavenPlugin;

import java.io.IOException;
import java.nio.file.*;

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

    @Override protected void doExecute(CoffeeScriptCompiler compiler, Path sourceDirectory, Path outputDirectory) throws Exception {
        compileCoffeeFilesInDir(compiler, sourceDirectory, outputDirectory);
        watch(compiler, sourceDirectory, outputDirectory);
    }

    private void watch(final CoffeeScriptCompiler compiler, final Path sourceDirectory, final Path outputDirectory) throws IOException, InterruptedException {
        WatchService watchService = startWatching(sourceDirectory);
        for(;;) {
            getLog().info("wait for changing...");
            WatchKey watchKey = watchService.take();

            for(WatchEvent<?> event: watchKey.pollEvents()) {
                Path file = (Path)event.context();
                if(Files.isDirectory(file) || !isCoffeeFile(file)) {
                    continue;
                }

                String coffeeFileName = file.toString();
                String jsFileName = getJsFileName(coffeeFileName);
                Path jsFile = outputDirectory.resolve(jsFileName);

                if(event.kind().name().equals(StandardWatchEventKinds.ENTRY_DELETE.name())) {
                    if (Files.deleteIfExists(jsFile)) {
                        getLog().info(String.format("deleted %s with %s", jsFileName, coffeeFileName));
                    }
                } else if(event.kind().name().equals(StandardWatchEventKinds.ENTRY_MODIFY.name()) || event.kind().name().equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
                    compileCoffeeFile(compiler, sourceDirectory.resolve(file), jsFile, coffeeFileName, jsFileName);
                }
            }

            watchKey.reset();
        }
    }

    private WatchService startWatching(Path sourceDirectory) throws IOException {
        WatchService watchService = sourceDirectory.getFileSystem().newWatchService();
        WatchEvent.Kind<?>[] events = {StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE};
        sourceDirectory.register(watchService, events);
        return watchService;
    }
}
