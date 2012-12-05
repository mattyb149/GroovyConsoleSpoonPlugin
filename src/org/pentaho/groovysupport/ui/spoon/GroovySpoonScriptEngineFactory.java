package org.pentaho.groovysupport.ui.spoon;

import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class GroovySpoonScriptEngineFactory implements ScriptEngineFactory {
	
	private static final String languageName = "Groovy";
	private static final String languageVersion = "1.8.x";
	private static final List<String> extensions = Arrays.asList("groovy");
	private static final List<String> mimeTypes = Arrays.asList("application/x-groovy");
	private static final List<String> names = Arrays.asList("groovyspoon","GroovySpoon");
	
	@Override
	public String getEngineName() {
		return "groovyspoon";
	}

	@Override
	public String getEngineVersion() {
		return "1.0";
	}

	@Override
	public List<String> getExtensions() {
		return extensions;
	}

	@Override
	public List<String> getMimeTypes() {
		return mimeTypes;
	}

	@Override
	public List<String> getNames() {
		return names;
	}

	@Override
	public String getLanguageName() {
		return languageName;
	}

	@Override
	public String getLanguageVersion() {
		return languageVersion;
	}

	@Override
	public Object getParameter(String key) {
		if(key.equals(ScriptEngine.ENGINE)) {
			return getEngineName();
		}
		else if(key.equals(ScriptEngine.ENGINE_VERSION)) {
			return getEngineVersion();
		}
		else if(key.equals(ScriptEngine.NAME)) {
			return getNames();
		}
		else if(key.equals(ScriptEngine.LANGUAGE)) {
			return getLanguageName();
		}
		else if(key.equals(ScriptEngine.LANGUAGE_VERSION)) {
			return getLanguageVersion();
		}
		// Add any implementation-specific params here
		
		return null;
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		String ret = obj;
	      ret += "." + m + "(";
	      for (int i = 0; i < args.length; i++) {
	          ret += args[i];
	          if (i == args.length - 1) {
	              ret += ")";
	          } else {
	              ret += ",";
	          }
	      }
	      return ret;
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return "println '" + toDisplay + "'";
	}

	@Override
	public String getProgram(String... statements) {
		StringBuilder sb = new StringBuilder();
		for(String statement : statements) {
			sb.append(statement);
			sb.append(";"); // Not required in Groovy but helpful to the parser
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new GroovySpoonScriptEngine();
	}
}
