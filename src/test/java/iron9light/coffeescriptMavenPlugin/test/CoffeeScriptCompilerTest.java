package iron9light.coffeescriptMavenPlugin.test;

import iron9light.coffeescriptMavenPlugin.CoffeeScriptCompiler;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * @author iron9light
 */
public class CoffeeScriptCompilerTest {
    @Test
    public void testVersion() {
        CoffeeScriptCompiler compiler = new CoffeeScriptCompiler(false);
        assertThat(compiler.version, equalTo("1.1.2"));
    }
}
