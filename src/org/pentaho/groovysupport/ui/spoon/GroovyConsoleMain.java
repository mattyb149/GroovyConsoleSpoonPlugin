package org.pentaho.groovysupport.ui.spoon;

import java.io.File;

import groovy.ui.Console;

public class GroovyConsoleMain {
	private GroovyShellMain groovyShell;
	
	public GroovyConsoleMain(ClassLoader cl, File pf) {
		try {
			groovyShell = new GroovyShellMain(cl, pf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void run() {
		Console console = new Console();
		console.setVisualizeScriptResults(true);
		
		// Get original shell context (for display transforms, etc.)
		console.setShell(groovyShell.createShell(console.getShell().getContext()));
		console.setVariable("gshell",groovyShell);
		console.setVariable("thisConsole",console);
	    console.run();
	}
	
	public static void main(String[] args) {
		
		File pluginFolderFile = new File(GroovyShellMain.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
		GroovyConsoleMain gcm = new GroovyConsoleMain(GroovyShellMain.class.getClassLoader(), pluginFolderFile);
		try {
			// TODO - args
			gcm.run();
			// TODO - wait for closed event
			while(true) {Thread.sleep(200);}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
