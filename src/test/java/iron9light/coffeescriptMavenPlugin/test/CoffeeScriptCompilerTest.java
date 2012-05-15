package iron9light.coffeescriptMavenPlugin.test;

import iron9light.coffeescriptMavenPlugin.CoffeeScriptCompiler;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author iron9light
 */
public class CoffeeScriptCompilerTest {
    @Test
    public void testVersion() throws MalformedURLException {
        URL url = getClass().getResource("/coffee-script.js");
        CoffeeScriptCompiler compiler = new CoffeeScriptCompiler(url, false);
        assertThat(compiler.version, equalTo("1.3.2"));
    }
}
