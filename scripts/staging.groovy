import org.pentaho.di.core.plugins.*
import org.pentaho.di.trans.*
import org.pentaho.di.trans.step.*

// Groovyize PDI classes, add helper methods, etc.

GroovyConsoleBaseScript.metaClass.propertyMissing = { name ->
	if(GroovyConsoleBaseScript.metaClass.respondsTo(delegate,name)) return delegate."$name"()
	null
}

GroovyConsoleBaseScript.metaClass.trans = {
	if(!it)
		spoon.activeTransformation
	else
		spoon.findTransformation(it)
}

spoon = Spoon.instance
pluginRegistry = PluginRegistry.instance
kettleVFS = KettleVFS.instance
slaveConnectionManager = SlaveConnectionManager.instance
defaultVarMap = KettleVariablesList.instance.defaultValueMap
defaultVarDescMap = KettleVariablesList.instance.descriptionMap

GroovyConsoleBaseScript.metaClass.methods = {if(it) it.class.declaredMethods else this.class.declaredMethods}
GroovyConsoleBaseScript.metaClass.printMethods = { methods(it).each { println it } }
GroovyConsoleBaseScript.metaClass.props = {if(it) it.class.declaredFields else this.class.declaredFields}
GroovyConsoleBaseScript.metaClass.properties = {properties(it)}
GroovyConsoleBaseScript.metaClass.printProps = { props(it).each { println it } }
GroovyConsoleBaseScript.metaClass.activeTrans = {spoon.activeTransformation}
GroovyConsoleBaseScript.metaClass.transGraph = spoon.activeTransGraph
GroovyConsoleBaseScript.metaClass.activeTransGraph = {spoon.activeTransGraph}
GroovyConsoleBaseScript.metaClass.job = { 
	if(!it) 
		spoon.activeJob
	else
		spoon.findJob(it)
}
GroovyConsoleBaseScript.metaClass.activeJob = {spoon.activeJob}
GroovyConsoleBaseScript.metaClass.jobGraph = spoon.activeJobGraph
GroovyConsoleBaseScript.metaClass.activeJobGraph = {spoon.activeJobGraph}

GroovyConsoleBaseScript.metaClass.step = {spoon.activeTransformation.findStep(it)}
GroovyConsoleBaseScript.metaClass.steps = {spoon.activeTransformation.steps}
GroovyConsoleBaseScript.metaClass.entry = {spoon.activeJob.findJobEntry(it)}
GroovyConsoleBaseScript.metaClass.entries = {spoon.activeJob.jobCopies}

GroovyConsoleBaseScript.metaClass.runTrans = { transMeta ->

	tm = transMeta ?: spoon.activeTransformation
	if(tm != null) {
		spoon.executeTransformation(tm, true, false, false, false, false, new Date(), false)
		tg = spoon.activeTransGraph
		try {
			// Wait for trans to start
			while(!tg.isRunning()) {
				Thread.sleep(10)
			}
			// Wait for trans to end
			while(tg.isRunning()) {
				Thread.sleep(100)
			}
		}
		catch(InterruptedException ie) {}
	}
}

GroovyConsoleBaseScript.metaClass.runJob = { jobMeta ->
	jm = jobMeta ?: spoon.getActiveJob()
	if(jm != null) {
		spoon.executeJob(jm, true, false, new Date(), false, null, 0);
	}
}

// Database methods
GroovyConsoleBaseScript.metaClass.database = { dbName -> spoon.activeDatabases.find {it.name.equalsIgnoreCase(dbName)} }
GroovyConsoleBaseScript.metaClass.db = {database(it)}
GroovyConsoleBaseScript.metaClass.databases = {spoon.activeDatabases}
GroovyConsoleBaseScript.metaClass.dbs = {databases()}
GroovyConsoleBaseScript.metaClass.createDatabase = {args ->
	meta = new org.pentaho.di.core.database.DatabaseMeta()
	dbInterface = org.pentaho.groovyconsole.ui.spoon.GroovyConsoleSpoonPlugin.connectionMap.get(args.dbType)
	meta.setDatabaseInterface(dbInterface)
	meta.setDBName(args.dbName)
	meta.setHostname(args.host)
	meta.setDBPort(args.port)
	meta.setUsername(args.user)
	meta.setPassword(args.password)
	meta.setName(args.name)
	meta
}

// Repository methods
GroovyConsoleBaseScript.metaClass.repo = {spoon.repository}
GroovyConsoleBaseScript.metaClass.repofs = RepoHelper.instance
Spoon.metaClass.repo = {delegate.repository}
TransMeta.metaClass.propertyMissing << { name -> delegate.findStep(name) }
TransMeta.metaClass.run = {runTrans(delegate)}
JobMeta.metaClass.propertyMissing << { name -> delegate.findJobEntry(name) }
JobMeta.metaClass.run = {runJob(delegate)}

RepoHelper.metaClass.propertyMissing << { name ->
	try {
		if(RepoHelper.metaClass.respondsTo(delegate,name)) return delegate."$name"()
		file = new File(name)
		dir = file?.parent?.replace(File.separator, RepositoryDirectory.DIRECTORY_SEPARATOR) ?: RepositoryDirectory.DIRECTORY_SEPARATOR
		objName = file?.name
		repoDir = spoon.repository?.findDirectory(dir);
		
		// Try to load the name as a job first
		try{ return spoon.repository?.loadJob(objName, repoDir, null, false, null) } catch(Exception e) {}
				
		// then a transformation
		try{ return spoon.repository?.loadTransformation(objName, repoDir, null, false, null)} catch(Exception e) {}
		
		tryDir = spoon.repository?.findDirectory(name);
		return tryDir ? new org.pentaho.groovyconsole.ui.spoon.repo.RepositoryDirectoryDecorator(tryDir) : null
		
		// TODO - others?
	}
	catch(Exception e) {
		e.printStackTrace(System.err)
		return null
	}
}

RepositoryDirectoryDecorator.metaClass.propertyMissing << { name ->
	if(RepoHelper.metaClass.respondsTo(delegate,name)) return delegate."$name"()
	null
}

// Step methods
StepMeta.metaClass.openDialog = {
	final String name = delegate.name
	final smi = delegate.stepMetaInterface

	spoon.display.asyncExec(new Runnable() {
		public void run() {
		  try {
			def c = Class.forName(smi.dialogClassName)
			def d = c.newInstance(Spoon.instance.shell, smi, trans(), name)
			d.open()
		  } catch (Exception e) {
		   e.printStackTrace(System.err);
		  }
		}
	  });
}

StepMeta.metaClass.dlg = {openDialog()}

StepMeta.metaClass.propertyMissing << { name ->
	if(StepMeta.metaClass.respondsTo(delegate,name)) return delegate."$name"()
	null
}

// Plugin methods
plugins = pluginRegistry

PluginRegistry.metaClass.propertyMissing << { name ->
	def p = delegate.findPluginWithName(
		delegate.pluginTypes.find { pluginType ->
			delegate.findPluginWithName(pluginType, name)
		}, name)
	if(p) return p
	p = delegate.findPluginWithId(
		delegate.pluginTypes.find { pluginType ->
			delegate.findPluginWithId(pluginType, name)
		}, name)
}

Plugin.metaClass.makeNew = {
	def c = plugins.loadClass(delegate)
	c.setDefault()
	def pid = plugins.getPluginId(delegate.pluginType, c)
	def mdg = new org.pentaho.di.trans.step.StepMeta(pid, delegate.name, c)
}

Plugin.metaClass.addNew = {
	def mdg = delegate.makeNew()
	mdg.location= new org.pentaho.di.core.gui.Point(20,20)
	mdg.draw = true
	activeTrans().addStep(mdg)
	async {
		spoon.refreshTree()
		activeTransGraph().redraw();
	}
}

// Threading methods
async = {c ->
	
	spoon.display.asyncExec(new Runnable() {
		public void run() {
		  try {
			c()
		  } catch (Exception e) {
		   e.printStackTrace(System.err)
		  }
		}
	  })
}

sync = {c ->
	
	spoon.display.syncExec(new Runnable() {
		public void run() {
		  try {
			c()
		  } catch (Exception e) {
		   e.printStackTrace(System.err)
		  }
		}
	  })
}

Integer.metaClass.waitThenRun = {c ->
	async{
		Thread.sleep(delegate.intValue())
		c()
	}
}

// Access methods
makePublic = {obj, name ->
	// try methods first, then fields
	obj.class.declaredMethods.find {it.name.equals(name)}.each { it.accessible = true }
	obj.class.declaredFields.find {it.name.equals(name)}.each { it.accessible = true }
}

// Operator overloading
TransMeta.metaClass.plus = {x ->
	if(x instanceof StepMeta) {
		x.draw = true
		x.location= new org.pentaho.di.core.gui.Point(20,20)
		delegate.addStep(x)
		def tg = activeTransGraph()
		if(tg.managedObject == delegate) {
			async { 
				spoon.refreshTree()
				tg.redraw()
			}
		}
		delegate
	}
	else if(x instanceof Plugin) {
		def newstep = x.makeNew()
		if(newstep instanceof StepMeta) {
			delegate + newstep
		}
	}
	else if(x instanceof String) {
		// Try to resolve step plugin
		if(plugins[x] && plugins[x] instanceof StepPluginType) {
			delegate + x
		}
	}
	delegate
}

StepMeta.metaClass.rightShift = { x ->
	if(x instanceof StepMeta) {
		activeTrans().addTransHop(new TransHopMeta(delegate, x))
		async {
			spoon.refreshTree()
			activeTransGraph().redraw()
		}
	}
	// return right op so hops can be chained
	x
}

StepMeta.metaClass.leftShift = { x ->
	if(x instanceof StepMeta) {
		activeTrans().addTransHop(new TransHopMeta(x, delegate))
		async {
			spoon.refreshTree()
			activeTransGraph().redraw()
		}
	}
	// return right op so hops can be chained
	x
}

