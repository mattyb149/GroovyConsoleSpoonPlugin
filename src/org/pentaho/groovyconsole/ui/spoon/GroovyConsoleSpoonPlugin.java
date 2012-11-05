package org.pentaho.groovyconsole.ui.spoon;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.ui.Console;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.ISpoonMenuController;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

@SpoonPlugin(id = "GroovyConsoleSpoonPlugin", image = "")
@SpoonPluginCategories({"spoon"})
public class GroovyConsoleSpoonPlugin extends AbstractXulEventHandler implements ISpoonMenuController, SpoonPluginInterface, SpoonLifecycleListener {

	public static final SortedMap<String, DatabaseInterface> connectionMap = new TreeMap<String, DatabaseInterface>();
	public static final Map<String, String> connectionNametoID = new HashMap<String, String>();

	  // The connectionMap allows us to keep track of the connection
	  // type we are working with and the correlating database interface

	  static {
	    PluginRegistry registry = PluginRegistry.getInstance();
	    
	    List<PluginInterface> plugins = registry.getPlugins(DatabasePluginType.class);
	    for (PluginInterface plugin : plugins) {
	      try {
	        DatabaseInterface databaseInterface = (DatabaseInterface)registry.loadClass(plugin);
	        databaseInterface.setPluginId(plugin.getIds()[0]);
	        databaseInterface.setName(plugin.getName());
	        connectionMap.put(plugin.getName(), databaseInterface);
	        connectionNametoID.put(plugin.getName(), plugin.getIds()[0]);
	      } 
	      catch (KettlePluginException cnfe) {
	         System.out.println("Could not create connection entry for "+plugin.getName()+".  "+cnfe.getCause().getClass().getName());
	         LogChannel.GENERAL.logError("Could not create connection entry for "+plugin.getName()+".  "+cnfe.getCause().getClass().getName()); 
	       }
	      catch (Exception e) {
	        throw new RuntimeException("Error creating class for: "+plugin, e);
	      }
	    }
	    
	  }
	  
	ResourceBundle bundle = new ResourceBundle() {
	    @Override
	    public Enumeration<String> getKeys() {
	      return null;
	    }

	    @Override
	    protected Object handleGetObject(String key) {
	      return BaseMessages.getString(GroovyConsoleSpoonPlugin.class, key);
	    }
	  };

	  public GroovyConsoleSpoonPlugin() {
		  Spoon spoon = ((Spoon)SpoonFactory.getInstance());
	      spoon.addSpoonMenuController(this);
	  }
	  
	  public String getName() {
		  return "groovyConsolePlugin"; //$NON-NLS-1$
	  }
		  
	@Override
	public void onEvent(SpoonLifeCycleEvent evt) {
		// Empty method		
	}

	@Override
	public void applyToContainer(String category, XulDomContainer container) throws XulException {
		container.registerClassLoader(getClass().getClassLoader());
	    if(category.equals("spoon")){
	      container.loadOverlay("org/pentaho/groovyconsole/ui/spoon/spoon_overlays.xul", bundle);
	      container.addEventHandler(this);
	    }
	}

	@Override
	public SpoonLifecycleListener getLifecycleListener() {
		return this;
	}

	@Override
	public SpoonPerspective getPerspective() {
		return SpoonPerspectiveManager.getInstance().getActivePerspective();
	}

	@Override
	public void updateMenu(Document doc) {
		// Empty method		
	}
	
	public void showGroovyConsole() {
		final Spoon spoon = Spoon.getInstance();
		
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				try
				{
					Console console = new Console();
					console.setVisualizeScriptResults(true);
					
					CompilerConfiguration cc = new CompilerConfiguration();
					cc.setScriptBaseClass(GroovyConsoleBaseScript.class.getName());
					
					// Get original shell context (for display transforms, etc.)
					Binding binding = console.getShell().getContext();
										
					// Create a shell in which to run script(s) that create the desired Binding context
					GroovyShell primingShell = new GroovyShell(this.getClass().getClassLoader(), binding, cc);
					primingShell.evaluate(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("staging.groovy")));
					
					// Load any staging scripts placed in the directory with the plugin
					File pluginFolder = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
					for(String script : pluginFolder.list(new GroovyExtFilter())) {
						try {
							primingShell.evaluate(new FileReader(pluginFolder.getAbsolutePath() + File.separator + script));
						}
						catch(Exception cfe) {
							System.out.println("Error loading script: "+script);
						}
					}
					
					// Create a new shell using the staged context, then create a console from the shell (also include a var for the shell)
					GroovyShell groovyShell = new GroovyShell(this.getClass().getClassLoader(), primingShell.getContext(), cc);
					console.setShell(groovyShell);
					console.setVariable("gshell",groovyShell);
					console.setVariable("thisConsole",console);
				    console.run();
				}
				catch(Exception e)
				{
					throw new InvocationTargetException(e, "Error displaying Groovy Console: "+e.toString());
				}
			}
		};
		
		try
		{
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(spoon.getShell());

			pmd.run(true, true, op);
		}
		catch (Exception e)
		{
		    showErrorDialog(e, "Error with Progress Monitor Dialog", "Error with Progress Monitor Dialog");
		}		
	}
	
	/**
     * Show an error dialog
     * 
     * @param e The exception to display
     * @param title The dialog title
     * @param message The message to display
    */
    private void showErrorDialog(Exception e, String title, String message)
    {
        new ErrorDialog(Spoon.getInstance().getShell(), title, message, e);
    }
    
    // Groovy file extension filter
	public static class GroovyExtFilter implements FilenameFilter {
 
		public boolean accept(File dir, String name) {
			return (name.endsWith(".groovy"));
		}
	}
}
