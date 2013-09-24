package org.pentaho.groovysupport.ui.spoon;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

public class GroovyShellMain {
	
	private ClassLoader classLoader;
	private File pluginFolder;
	
	public GroovyShellMain() {
		classLoader = GroovyShellMain.class.getClassLoader();
		pluginFolder = new File(GroovyShellMain.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
	}
	
	public GroovyShellMain(ClassLoader cl, File pf) {
		classLoader = cl;
		pluginFolder = pf;
	}
	
	public GroovyShell createShell(Binding binding) {
		
		// Set default imports
		ImportCustomizer ic = new ImportCustomizer();
		ic.addStarImports(
			"org.pentaho.di.repository",
			"org.pentaho.di.trans",
			"org.pentaho.di.job",
			"org.pentaho.di.ui.spoon",
			"org.pentaho.di.ui.spoon.delegates",
			"org.pentaho.di.ui.spoon.trans",
			"org.pentaho.di.trans.step",
			"org.pentaho.di.cluster",
			"org.pentaho.di.core",
			"org.pentaho.di.core.database",
			"org.pentaho.di.core.logging",
			"org.pentaho.di.core.plugins",
      "org.pentaho.di.core.row",
      "org.pentaho.di.core.vfs",
      "org.pentaho.di.core.exception",
      "org.pentaho.groovysupport.ui.spoon",
      "org.pentaho.groovysupport.ui.spoon.repo",
      "org.pentaho.groovysupport.script"
		);
		CompilerConfiguration cc = new CompilerConfiguration();
		cc.addCompilationCustomizers(ic);
		cc.setScriptBaseClass(GroovyConsoleBaseScript.class.getName());
		
		// Create a shell in which to run script(s) that create the desired Binding context
		GroovyShell primingShell = new GroovyShell(classLoader, binding, cc);
		
		// Evaluate all groovy scripts in the JAR
		String scriptName = "";
		try {
			JarFile jarFile = new JarFile(URLDecoder.decode(GroovyShellMain.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"));
			for(Enumeration<JarEntry> em = jarFile.entries(); em.hasMoreElements();) {  
				scriptName = em.nextElement().toString();
		        if(scriptName.endsWith(".groovy")) {
		        	ZipEntry entry = jarFile.getEntry(scriptName);
		        	InputStream inStream= jarFile.getInputStream(entry);
		        	primingShell.evaluate(new InputStreamReader(inStream));
		        }
			}
		} catch (IOException e) {
			System.out.println("Error loading script: "+scriptName);
		}		
		
		
		// Load any staging scripts placed in the directory with the plugin
		String[] scriptList = pluginFolder.list(new GroovyExtFilter());
		if(scriptList != null) {
			for(String script : scriptList) {
				try {
					primingShell.evaluate(new FileReader(pluginFolder.getAbsolutePath() + File.separator + script));
				}
				catch(Exception cfe) {
					System.out.println("Error loading script: "+script);
				}
			}
		}
		
		// Create a new shell using the staged context, then create a console from the shell (also include a var for the shell)
		return new GroovyShell(this.getClass().getClassLoader(), primingShell.getContext(), cc);
	}
	
	public static void main(String[] args) {
		
		File pluginFolderFile = new File(GroovyShellMain.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
		GroovyShellMain gcm = new GroovyShellMain(GroovyShellMain.class.getClassLoader(), pluginFolderFile);
		try {
			if(args.length > 0) {
				File file = new File(args[0]);
				gcm.createShell(new Binding()).evaluate(file);
			}
			else {
				System.err.println("Usage: <filename>"); // TODO
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	// Groovy file extension filter
	public static class GroovyExtFilter implements FilenameFilter {
 
		public boolean accept(File dir, String name) {
			return (name.endsWith(".groovy"));
		}
	}
}
