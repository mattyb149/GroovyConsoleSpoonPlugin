package org.pentaho.groovysupport.ui.spoon;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.pentaho.di.core.gui.SpoonFactory;
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
	      container.loadOverlay("org/pentaho/groovysupport/ui/spoon/spoon_overlays.xul", bundle);
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
					GroovyConsoleMain gcm = new GroovyConsoleMain(
							this.getClass().getClassLoader(),
							new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile());
					
					gcm.run();
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
}
