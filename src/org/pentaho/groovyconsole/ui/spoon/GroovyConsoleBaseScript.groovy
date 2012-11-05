package org.pentaho.groovyconsole.ui.spoon

import groovy.lang.Binding
import groovy.lang.Script
import org.pentaho.vfs.ui.*

abstract class GroovyConsoleBaseScript extends Script {

	public GroovyConsoleBaseScript() {
		super()
	}

	public GroovyConsoleBaseScript(Binding binding) {
		super(binding)
	}
	
}
