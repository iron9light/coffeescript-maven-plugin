package iron9light.coffeescriptMavenPlugin;

import com.google.common.base.Charsets;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class CoffeeScriptCompiler {

    private final Scriptable globalScope;
    private boolean bare;
    public String version;

    public CoffeeScriptCompiler(URL url, boolean bare) {

        this.bare = bare;
        InputSupplier<InputStreamReader> supplier = Resources.newReaderSupplier(url, Charsets.UTF_8);
        Context context = Context.enter();
        context.setOptimizationLevel(-1); // Without this, Rhino hits a 64K bytecode limit and fails
        try {
            globalScope = context.initStandardObjects();
            context.evaluateReader(globalScope, supplier.getInput(), "coffee-script.js", 0, null);
            version = (String) context.evaluateString(globalScope, "CoffeeScript.VERSION;", "source", 0, null);
        } catch (IOException e1) {
            throw new CoffeeScriptException(e1.getMessage());
        } finally {
            Context.exit();
        }

    }

    public String compile(String coffeeScriptSource) {
        Context context = Context.enter();
        try {
            Scriptable compileScope = context.newObject(globalScope);
            compileScope.setParentScope(globalScope);
            compileScope.put("coffeeScript", compileScope, coffeeScriptSource);
            try {

                String options = bare ? "{bare: true}" : "{}";

                return (String) context.evaluateString(
                        compileScope,
                        String.format("CoffeeScript.compile(coffeeScript, %s);", options),
                        "source", 0, null);
            } catch (JavaScriptException e) {
                throw new CoffeeScriptException(e.getMessage());
            }
        } finally {
            Context.exit();
        }
    }

}
