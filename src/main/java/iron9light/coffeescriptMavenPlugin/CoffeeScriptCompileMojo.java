package iron9light.coffeescriptMavenPlugin;

import java.nio.file.Path;

/**
 * Compile coffeescript files to javascript files.
 *
 * @author iron9light
 * @phase generate-resources
 * @goal compile
 */
public class CoffeeScriptCompileMojo extends CoffeeScriptMojoBase {
    @Override protected void doExecute(CoffeeScriptCompiler compiler, Path sourceDirectory, Path outputDirectory) throws Exception {
        compileCoffeeFilesInDir(compiler, sourceDirectory, outputDirectory);
    }
}
