package org.pentaho.groovysupport.ui.spoon;

import groovy.lang.Binding;
import groovy.lang.Script;

abstract class GroovyConsoleBaseScript extends Script {

	public GroovyConsoleBaseScript() {
		super();
	}

	public GroovyConsoleBaseScript(Binding binding) {
		super(binding);
	}
	
}
