package iron9light.coffeescriptMavenPlugin;

import com.google.common.base.Charsets;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public abstract class CoffeeScriptMojoBase extends AbstractMojo {
    /**
     * Source Directory.
     *
     * @parameter default-value="${basedir}/src/main/webapp"
     */
    private File srcDir;

    /**
     * Output Directory.
     *
     * @parameter default-value="${basedir}/src/main/webapp"
     */
    private File outputDir;

    /**
     * Bare mode.
     *
     * @parameter default-value="false"
     */
    private Boolean bare;

    /**
     * Only compile modified files.
     *
     * @parameter default-value="false"
     */
    private Boolean modifiedOnly;

    /**
     * CoffeeScript compiler file url.
     * It supports both url string and file path string.
     * e.g. http://coffeescript.org/extras/coffee-script.js or ${basedir}/lib/coffee-script.js
     *
     * @parameter
     */
    private String compilerUrl;

    private static Charset charset = Charsets.UTF_8;

    private static String newline = System.getProperty("line.separator");

    private static URL defaultCoffeeScriptUrl = CoffeeScriptMojoBase.class.getResource("/coffee-script.js");

    public void execute() throws MojoExecutionException, MojoFailureException {
        CoffeeScriptCompiler compiler = new CoffeeScriptCompiler(getCoffeeScriptUrl(), bare);
        getLog().info(String.format("Coffeescript version: %s", compiler.version));

        if (!srcDir.exists()) {
            throw new MojoExecutionException("Source directory not fount: " + srcDir.getPath());
        }

        try {
            Path sourceDirectory = srcDir.toPath();
            Path outputDirectory = outputDir.toPath();

            doExecute(compiler, sourceDirectory, outputDirectory);
        } catch (MojoExecutionException | MojoFailureException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
    }

    abstract protected void doExecute(CoffeeScriptCompiler compiler, Path sourceDirectory, Path outputDirectory) throws Exception;

    protected void compileCoffeeFilesInDir(final CoffeeScriptCompiler compiler, final Path sourceDirectory, final Path outputDirectory) throws IOException, MojoFailureException {
        long startTime = System.currentTimeMillis();
        List<Path> coffeeFiles = findCoffeeFilesInDir(sourceDirectory);
        List<String> failedFileNames = new ArrayList<>();
        for (Path coffeeFile : coffeeFiles) {
            String coffeeFileName = sourceDirectory.relativize(coffeeFile).toString();
            String jsFileName = getJsFileName(coffeeFileName);
            Path jsFile = outputDirectory.resolve(jsFileName);
            if (!compileCoffeeFile(compiler, coffeeFile, jsFile, coffeeFileName, jsFileName)) {
                failedFileNames.add(coffeeFileName);
            }
        }

        long escapedTime = System.currentTimeMillis() - startTime;
        int compiledCount = coffeeFiles.size() - failedFileNames.size();
        if (compiledCount > 0) {
            getLog().info(String.format("successful compiled (or skipped) %d coffeescript(s) in %.3fs", compiledCount, escapedTime / 1000.0));
        }

        if (!failedFileNames.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder(String.format("fail to compile %d coffeescript(s):", failedFileNames.size()));
            for (String failedFileName : failedFileNames) {
                stringBuilder.append(newline);
                stringBuilder.append(failedFileName);
            }

            String failMessage = stringBuilder.toString();

            getLog().error(failMessage);

            throw new MojoFailureException(failMessage);
        }
    }

    private List<Path> findCoffeeFilesInDir(Path sourceDirectory) throws IOException {
        final List<Path> coffeeFiles = new ArrayList<>();
        Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (isCoffeeFile(file)) {
                    coffeeFiles.add(file);
                }

                return FileVisitResult.CONTINUE;
            }
        });
        return coffeeFiles;
    }

    protected String getJsFileName(String coffeeFileName) {
        return coffeeFileName.substring(0, coffeeFileName.length() - ".coffee".length()) + ".js";
    }

    protected boolean isCoffeeFile(Path file) {
        return file.toString().endsWith(".coffee");
    }

    protected boolean compileCoffeeFile(CoffeeScriptCompiler compiler, Path coffeeFile, Path jsFile, String coffeeFileName, String jsFileName) throws IOException {
        Path jsParent = jsFile.getParent();
        if (!Files.exists(jsParent)) {
            Files.createDirectories(jsParent);
        } else if (Files.isDirectory(jsFile)) {
            getLog().warn(String.format("Cannot compile to %s, as there is a Directory with the same name", jsFileName));
            return false;
        } else if (modifiedOnly && Files.exists(jsFile)) {
            if (Files.getLastModifiedTime(jsFile).compareTo(Files.getLastModifiedTime(coffeeFile)) > 0) {
                getLog().info(String.format("skip %s", coffeeFileName));
                return true;
            }
        }

        String coffeeSource = readAllString(coffeeFile);

        try {
            String jsSource = compiler.compile(coffeeSource);
            writeString(jsFile, jsSource);
            getLog().info(String.format("Compiled: %s [%s]", coffeeFileName, new java.util.Date()));
        } catch (CoffeeScriptException ex) {
            getLog().error(String.format("Error: %s %s [%s]", coffeeFileName, ex.getMessage(), new java.util.Date()));
            return false;
        }

        return true;
    }

    private void writeString(Path path, String jsSource) throws IOException {
        com.google.common.io.Files.write(jsSource, path.toFile(), charset);
    }

    private String readAllString(Path path) throws IOException {
        return com.google.common.io.Files.toString(path.toFile(), charset);
    }

    private URL getCoffeeScriptUrl() {
        getLog().debug(String.format("compilerUrl: %s", compilerUrl));

        if (compilerUrl == null || compilerUrl.isEmpty()) {
            getLog().debug("CompilerUrl is null or empty, use default.");
            return defaultCoffeeScriptUrl;
        }

        URL url;
        try {
            getLog().debug("Trying to parse compilerUrl as URL.");
            url = new URL(compilerUrl);
        } catch (MalformedURLException e) {
            try {
                getLog().debug("Failed parsing compilerUrl as URL. Trying to parse it as Path.");
                url = Paths.get(compilerUrl).toUri().toURL();
            } catch (MalformedURLException e1) {
                getLog().debug("Failed parsing compilerUrl, use default.");
                return defaultCoffeeScriptUrl;
            }
        }

        getLog().info(String.format("Load CoffeeScript compiler from %s", url));

        return url;
    }
}
