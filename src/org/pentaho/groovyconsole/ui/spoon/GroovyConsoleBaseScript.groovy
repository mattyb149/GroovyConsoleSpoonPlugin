package org.pentaho.groovyconsole.ui.spoon

import groovy.lang.Binding
import groovy.lang.Script
import org.pentaho.di.trans.TransMeta

abstract class GroovyConsoleBaseScript extends Script {

	public GroovyConsoleBaseScript() {
		super()
	}

	public GroovyConsoleBaseScript(Binding binding) {
		super(binding)
	}
	
	
	/*def methodMissing(String name, args) {
		println "redirect method: $name"
	}
	
	def propertyMissing(String name) { 
		println "redirect property: $name"
	}*/
}
