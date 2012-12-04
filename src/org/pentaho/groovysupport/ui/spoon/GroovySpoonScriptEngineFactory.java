package org.pentaho.groovysupport.ui.spoon;

import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class GroovySpoonScriptEngineFactory implements ScriptEngineFactory {

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
		return Arrays.asList("groovy");
	}

	@Override
	public List<String> getMimeTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getNames() {
		
		return Arrays.asList("groovyspoon","GroovySpoon");
	}

	@Override
	public String getLanguageName() {
		return "Groovy";
	}

	@Override
	public String getLanguageVersion() {
		return "1.8.x";
	}

	@Override
	public Object getParameter(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProgram(String... statements) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new GroovySpoonScriptEngine();
	}

}
