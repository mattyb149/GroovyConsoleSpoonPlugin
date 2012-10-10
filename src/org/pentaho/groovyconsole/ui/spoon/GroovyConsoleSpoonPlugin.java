package org.pentaho.groovyconsole.ui.spoon;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.ui.Console;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.runtime.MethodClosure;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.pentaho.di.cluster.SlaveConnectionManager;
import org.pentaho.di.core.KettleVariablesList;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.ISpoonMenuController;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

@SpoonPlugin(id = "GroovyConsoleSpoonPlugin", image = "")
@SpoonPluginCategories({"spoon"})
public class GroovyConsoleSpoonPlugin extends AbstractXulEventHandler implements ISpoonMenuController, SpoonPluginInterface, SpoonLifecycleListener {

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
					ImportCustomizer ic = new ImportCustomizer();
					ic.addStarImports(
						"org.pentaho.di.trans",
						"org.pentaho.di.ui.spoon.delegates",
						"org.pentaho.di.trans.step",
			            "org.pentaho.di.core.row",
			            "org.pentaho.di.core",
			            "org.pentaho.di.core.exception"
					);
					CompilerConfiguration cc = new CompilerConfiguration();
					cc.addCompilationCustomizers(ic);
					cc.setScriptBaseClass(GroovyConsoleBaseScript.class.getName());
					Binding binding = new Binding();
					binding.setProperty("spoon", spoon);
					binding.setProperty("pluginRegistry", PluginRegistry.getInstance());
					binding.setProperty("kettleVFS", KettleVFS.getInstance());
					binding.setProperty("slaveConnectionManager", SlaveConnectionManager.getInstance());
					binding.setProperty("defaultVarMap", KettleVariablesList.getInstance().getDefaultValueMap());
					binding.setProperty("defaultVarDescMap", KettleVariablesList.getInstance().getDescriptionMap());
					binding.setVariable("methods", new MethodClosure(GroovyConsoleHelper.class,"methods"));
					binding.setVariable("printMethods", new MethodClosure(GroovyConsoleHelper.class,"printMethods"));
					binding.setVariable("props", new MethodClosure(GroovyConsoleHelper.class,"properties"));
					binding.setVariable("properties", new MethodClosure(GroovyConsoleHelper.class,"properties"));
					binding.setVariable("printProperties", new MethodClosure(GroovyConsoleHelper.class,"printProperties"));
					binding.setProperty("trans", spoon.getActiveTransformation());
					binding.setProperty("transGraph", spoon.getActiveTransGraph());
					binding.setVariable("activeTrans",new MethodClosure(GroovyConsoleHelper.class,"trans"));
					binding.setVariable("activeTransGraph",new MethodClosure(GroovyConsoleHelper.class,"transGraph"));
					binding.setVariable("database",new MethodClosure(GroovyConsoleHelper.class,"database"));
					binding.setVariable("step",new MethodClosure(GroovyConsoleHelper.class,"step"));
					binding.setVariable("createdb",new MethodClosure(GroovyConsoleHelper.class,"createDatabase"));
					GroovyShell groovyShell = new GroovyShell(this.getClass().getClassLoader(), binding, cc);
					console.setShell(groovyShell);
					console.setVariable("gshell",groovyShell);
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
		catch (InvocationTargetException e)
		{
		    showErrorDialog(e, "Error with Progress Monitor Dialog", "Error with Progress Monitor Dialog");
		}
		catch (InterruptedException e)
		{
		    showErrorDialog(e, "Error with Progress Monitor Dialog", "Error with Progress Monitor Dialog");
		}
		
	}
	
	/**
     * Showing an error dialog
     * 
     * @param e
    */
    private void showErrorDialog(Exception e, String title, String message)
    {
        new ErrorDialog(Spoon.getInstance().getShell(), title, message, e);
    }
    
    public static class GroovyConsoleHelper {
    	
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
    	
    	public static List<Method> methods(Object o) {
    		return Arrays.asList(o.getClass().getDeclaredMethods());
    	}
    	
    	public static List<Field> properties(Object o) {
    		return Arrays.asList(o.getClass().getDeclaredFields());
    	}
    	
    	public static void printMethods(Object o) {
    		for(Method m : o.getClass().getDeclaredMethods()) {
    			System.out.println(m);
    		}
    	}
    	
    	public static void printProperties(Object o) {
    		for(Field f : o.getClass().getDeclaredFields()) {
    			System.out.println(f);
    		}
    	}
    	
    	public static TransMeta trans() {
    		return Spoon.getInstance().getActiveTransformation();
    	}
    	
    	public static TransGraph transGraph() {
    		return Spoon.getInstance().getActiveTransGraph();
    	}
    	
    	public static DatabaseMeta database(String dbName) {
    		
    		for(DatabaseMeta dbm : Spoon.getInstance().getActiveDatabases()) {
    			if(dbm.getName().equalsIgnoreCase(dbName)) {
    				return dbm;
    			}
    		}
    		return null;
    	}
    	
    	public static StepMeta step(String stepName) {
    		
    		return Spoon.getInstance().getActiveTransformation().findStep(stepName);
    	}
    	
    	public static DatabaseMeta createDatabase(Map<String,String> args) {
    	    String name = args.get("name");
    	    String dbType = args.get("dbType");
    	    String dbName = args.get("dbName");
    	    String host = args.get("host");
    	    String port = args.get("port");
    	    String user = args.get("user");
    	    String password = args.get("password");
    		DatabaseMeta meta = new DatabaseMeta();
    		DatabaseInterface dbInterface = connectionMap.get(dbType);
    		meta.setDatabaseInterface(dbInterface);
    		meta.setDBName(dbName);
    		meta.setHostname(host);
    		meta.setDBPort(port);
    		meta.setUsername(user);
    		meta.setPassword(password);
    		meta.setName(name);
    		
    		return meta;
    	}
    }
	
}
