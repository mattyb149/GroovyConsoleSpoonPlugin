package org.pentaho.groovysupport.ui.spoon;

import groovy.lang.Binding;
import groovy.lang.GString;
import groovy.lang.GroovyShell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

public class GroovySpoonScriptEngine implements ScriptEngine, ScriptContext, Compilable {
	
	private static final List<Integer> scopes = Arrays.asList(new Integer(ScriptContext.ENGINE_SCOPE), new Integer(ScriptContext.GLOBAL_SCOPE));
	private static final GroovySpoonScriptEngineFactory engineFactory = new GroovySpoonScriptEngineFactory();
	
	private GroovySpoonBindings bindingEngine = new GroovySpoonBindings();
	private GroovySpoonBindings bindingGlobal = new GroovySpoonBindings();
	private Writer stdoutWriter = new PrintWriter(System.out);
	private Writer stderrWriter = new PrintWriter(System.err);
	private Reader stdinReader = new InputStreamReader(System.in);
	
	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		
		setContext(context);
		GroovyShell gs = new GroovyShellMain().createShell(bindingEngine);
		Object result = gs.evaluate(script);
		
		// Update contexts
		bindingEngine = new GroovySpoonBindings(gs.getContext());
		context.setBindings(bindingEngine, ScriptContext.ENGINE_SCOPE);
		return result;
	}

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		setContext(context);
		GroovyShell gs = new GroovyShellMain().createShell(bindingEngine);
		Object result = gs.evaluate(reader);

		// Update contexts
		bindingEngine = new GroovySpoonBindings(gs.getContext());
		context.setBindings(bindingEngine, ScriptContext.ENGINE_SCOPE);
		return result;
	}

	@Override
	public Object eval(String script) throws ScriptException {
		if(bindingEngine == null) bindingEngine = new GroovySpoonBindings();
		GroovyShell gs = new GroovyShellMain().createShell(bindingEngine);
		Object result = gs.evaluate(script);
		bindingEngine = new GroovySpoonBindings(gs.getContext());
		return result;
	}

	@Override
	public Object eval(Reader reader) throws ScriptException {
		if(bindingEngine == null) bindingEngine = new GroovySpoonBindings();
		GroovyShell gs = new GroovyShellMain().createShell(bindingEngine);
		Object result = gs.evaluate(reader);
		bindingEngine = new GroovySpoonBindings(gs.getContext());
		return result;
	}

	@Override
	public Object eval(String script, Bindings b) throws ScriptException {
		bindingEngine = new GroovySpoonBindings(b);
		GroovyShell gs = new GroovyShellMain().createShell(bindingEngine);
		Object result = gs.evaluate(script);
		
		// Update contexts
		bindingEngine = new GroovySpoonBindings(gs.getContext());
		b.putAll(bindingEngine);
		return result;
	}

	@Override
	public Object eval(Reader reader, Bindings b) throws ScriptException {
		bindingEngine = new GroovySpoonBindings(b);
		GroovyShell gs = new GroovyShellMain().createShell(bindingEngine);
		Object result = gs.evaluate(reader);
		
		// Update contexts
		bindingEngine = new GroovySpoonBindings(gs.getContext());
		b.putAll(bindingEngine);
		return result;
	}

	@Override
	public void put(String key, Object value) {
		bindingEngine.setProperty(key, value);
	}

	@Override
	public Object get(String key) {
		Object prop = null;
		if(bindingEngine != null) {
			prop = bindingEngine.getVariable(key);
		}
		if(prop == null && bindingGlobal != null) {
			prop = bindingGlobal.getVariable(key);
		}
		return prop;
	}

	@Override
	public Bindings getBindings(int scope) {
		Binding b = null;
		switch(scope) {
		case ScriptContext.ENGINE_SCOPE:
			b = bindingEngine;
			break;
		case ScriptContext.GLOBAL_SCOPE:
			b = bindingGlobal;
			break;
		default:
			break;
		}
		
		return (Bindings) b;
	}

	@Override
	public void setBindings(Bindings bindings, int scope) {
		
		switch(scope) {
		case ScriptContext.ENGINE_SCOPE:
			bindingEngine = new GroovySpoonBindings(bindings);
			break;
		case ScriptContext.GLOBAL_SCOPE:
			bindingGlobal = new GroovySpoonBindings(bindings);
			break;
		default:
			break;
		}
	}

	@Override
	public Bindings createBindings() {
		if(bindingEngine == null) bindingEngine = new GroovySpoonBindings();
		if(bindingGlobal == null) bindingGlobal = new GroovySpoonBindings();
		
		return bindingEngine;
	}

	@Override
	public ScriptContext getContext() {
		return this;
	}

	@Override
	public void setContext(ScriptContext context) {
		
		// Copy in bindings
		for(Integer i : getScopes()) {
			this.setBindings(context.getBindings(i), i);
		}
		
		// Copy in writers/readers
		this.setWriter(context.getWriter());
		this.setErrorWriter(context.getErrorWriter());
		this.setReader(context.getReader());
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return engineFactory;
	}	
	
	@Override
	public void setAttribute(String name, Object value, int scope) {
		
		switch(scope) {
		case ScriptContext.ENGINE_SCOPE:
			bindingEngine.setVariable(name, value);
			break;
		case ScriptContext.GLOBAL_SCOPE:
			bindingGlobal.setVariable(name, value);
			break;
		default:
			break;
		}
		
	}

	@Override
	public Object getAttribute(String name, int scope) {
		Object o = null; 
		switch(scope) {
		case ScriptContext.ENGINE_SCOPE:
			o = bindingEngine.getVariable(name);
			break;
		case ScriptContext.GLOBAL_SCOPE:
			o = bindingGlobal.getVariable(name);
			break;
		default:
			break;
		}
		return o;
	}

	@Override
	public Object removeAttribute(String name, int scope) {
		Object o = null; 
		switch(scope) {
		case ScriptContext.ENGINE_SCOPE:
			o = bindingEngine.getVariable(name);
			bindingEngine.setVariable(name, null);
			break;
		case ScriptContext.GLOBAL_SCOPE:
			o = bindingGlobal.getVariable(name);
			bindingGlobal.setVariable(name, null);
			break;
		default:
			break;
		}
		return o;
	}

	@Override
	public Object getAttribute(String name) {
		Object o = null;
		if(bindingEngine != null) {
			o = bindingEngine.getVariable(name);
		}
		if(o == null && bindingGlobal != null) {
			o = bindingGlobal.getVariable(name);
		}
		return o;
	}

	@Override
	public int getAttributesScope(String name) {
		int scope = -1;
		Object o = null;
		if(bindingGlobal != null) {
			o = bindingGlobal.getVariable(name);
			if(o != null) {
				scope = ScriptContext.GLOBAL_SCOPE;
			}
		}
		if(bindingEngine != null) {
			Object engineObj = bindingEngine.getVariable(name);
			if(engineObj != null)  {
				o = engineObj;
				scope = ScriptContext.ENGINE_SCOPE;
			}
		}
		
		return scope;
	}

	@Override
	public Writer getWriter() {
		return stdoutWriter;
	}

	@Override
	public Writer getErrorWriter() {
		return stderrWriter;
	}

	@Override
	public void setWriter(Writer writer) {
		stdoutWriter = writer;		
	}

	@Override
	public void setErrorWriter(Writer writer) {
		stderrWriter = writer;		
	}

	@Override
	public Reader getReader() {
		return stdinReader;
	}

	@Override
	public void setReader(Reader reader) {
		stdinReader = reader;		
	}

	@Override
	public List<Integer> getScopes() {
		return scopes;
	}
	
	@Override
	public CompiledScript compile(String script) throws ScriptException {
		return new CompiledGroovyScript(script, this);
	}

	@Override
	public CompiledScript compile(Reader script) throws ScriptException {
		return new CompiledGroovyScript(script, this);
	}
	
	/**************************************************************************
	 * An implementation of Binding and Bindings to make ScriptEngine<->Groovy
	 * stuff easier.
	 * 
	 * @author MBurgess
	 * 
	 **************************************************************************/	
	private class GroovySpoonBindings extends Binding implements Bindings {
		
		public GroovySpoonBindings() {
			super();
		}

		public GroovySpoonBindings(Bindings bindings) {
			this();
			for(String key : bindings.keySet()) {
				this.setVariable(key, bindings.get(key));
			}
		}

		public GroovySpoonBindings(Binding context) {
			this();
			
			@SuppressWarnings("rawtypes")
			Map vars = context.getVariables();
			
			for(Object key : vars.keySet()) {
				Object val = vars.get(key);
				// Convert GStrings to Java strings
				setVariable(key.toString(), (val instanceof GString) ? val.toString() : val);
			}
		}

		@Override
		public int size() {
			return getVariables().size();
		}

		@Override
		public boolean isEmpty() {
			return getVariables().isEmpty();
		}

		@Override
		public boolean containsValue(Object value) {
			return getVariables().containsValue(value);
		}

		@Override
		public void clear() {
			getVariables().clear();			
		}

		@SuppressWarnings("unchecked")
		@Override
		public Set<String> keySet() {
			return super.getVariables().keySet();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Collection<Object> values() {
			return getVariables().values();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			return getVariables().entrySet();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object put(String name, Object value) {
			return getVariables().put(name, value);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void putAll(Map<? extends String, ? extends Object> toMerge) {
			getVariables().putAll(toMerge);
		}

		@Override
		public boolean containsKey(Object key) {
			return getVariables().containsKey(key);
		}

		@Override
		public Object get(Object key) {
			return getVariables().get(key);
		}

		@Override
		public Object remove(Object key) {
			return getVariables().remove(key);
		}		
	}
	
	private class CompiledGroovyScript extends CompiledScript {
		
		GroovySpoonScriptEngine engine = null;
		String script = "";
		
		
		@SuppressWarnings("unused")
		public CompiledGroovyScript() {
			engine = new GroovySpoonScriptEngine();			
		}
		
		public CompiledGroovyScript(GroovySpoonScriptEngine se) {
			engine = se;
		}
		
		public CompiledGroovyScript(String s, GroovySpoonScriptEngine se) {
			this(se);
			script = s;
		}
		
		public CompiledGroovyScript(Reader s, GroovySpoonScriptEngine se) {
			this(se);
			BufferedReader reader = new BufferedReader(s);
		    String         line = null;
		    StringBuilder  stringBuilder = new StringBuilder();
		    String         ls = System.getProperty("line.separator");

		    try {
			    while( ( line = reader.readLine() ) != null ) {
			        stringBuilder.append( line );
			        stringBuilder.append( ls );
			    }
		    }
		    catch(Throwable t) {
		    	t.printStackTrace(new PrintWriter(se.getErrorWriter()));
		    }

		    script = stringBuilder.toString();
		}

		@Override
		public Object eval(Bindings bindings) throws ScriptException {
			return (engine != null) ? engine.eval(script, bindings) : null;
		}

		@Override
		public Object eval(ScriptContext context) throws ScriptException {
			return (engine != null) ? engine.eval(script, context) : null;
		}

		@Override
		public ScriptEngine getEngine() {
			return engine;
		}
		
	}
}
