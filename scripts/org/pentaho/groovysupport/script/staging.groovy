package org.pentaho.groovysupport.script 

import org.pentaho.di.cluster.*
import org.pentaho.di.core.*
import org.pentaho.di.core.database.*
import org.pentaho.di.core.exception.*
import org.pentaho.di.core.gui.*
import org.pentaho.di.core.logging.*
import org.pentaho.di.core.plugins.*
import org.pentaho.di.core.row.*
import org.pentaho.di.core.vfs.*
import org.pentaho.di.job.*
import org.pentaho.di.repository.*
import org.pentaho.di.trans.*
import org.pentaho.di.trans.step.*
import org.pentaho.di.ui.spoon.*
import org.pentaho.di.ui.spoon.delegates.*
import org.pentaho.di.ui.spoon.trans.*
import org.pentaho.vfs.ui.*
import org.pentaho.groovysupport.ui.spoon.*
import org.pentaho.groovysupport.ui.spoon.repo.*

// Groovyize PDI classes, add helper methods, etc.

GroovyConsoleBaseScript.metaClass.propertyMissing = { name ->
	if(GroovyConsoleBaseScript.metaClass.respondsTo(delegate,name)) return delegate?."$name"()
	null
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
GroovyConsoleBaseScript.metaClass.printProps = { props(it)?.each { println it } }
GroovyConsoleBaseScript.metaClass.activeTrans = {spoon?.activeTransformation}
GroovyConsoleBaseScript.metaClass.trans = { return it ? spoon?.findTransformation(it) : spoon?.activeTransformation }
GroovyConsoleBaseScript.metaClass.transGraph = {spoon?.activeTransGraph}
GroovyConsoleBaseScript.metaClass.activeTransGraph = {spoon?.activeTransGraph}
GroovyConsoleBaseScript.metaClass.job = { return it ?  spoon.findJob(it) : spoon.activeJob } 
GroovyConsoleBaseScript.metaClass.activeJob = {spoon?.activeJob}
GroovyConsoleBaseScript.metaClass.jobGraph = {spoon?.activeJobGraph}
GroovyConsoleBaseScript.metaClass.activeJobGraph = {spoon?.activeJobGraph}

GroovyConsoleBaseScript.metaClass.step = {spoon?.activeTransformation?.findStep(it)}
GroovyConsoleBaseScript.metaClass.steps = {spoon?.activeTransformation?.steps}
GroovyConsoleBaseScript.metaClass.entry = {spoon?.activeJob?.findJobEntry(it)}
GroovyConsoleBaseScript.metaClass.entries = {spoon?.activeJob?.jobCopies}

Spoon.metaClass.runTrans = { transMeta ->

	tm = transMeta ?: delegate.activeTransformation
	if(tm != null) {
		// Compatibility with 4.4.0 (no LogLevel param)
		if(delegate.metaClass.respondsTo(spoon.instance, 'executeTransformation',
			TransMeta, Boolean, Boolean, Boolean, Boolean, Boolean, Date, Boolean, LogLevel)) {
			delegate.executeTransformation(tm, true, false, false, false, false, new Date(), false, delegate.log?.logLevel)
		}
		else if(delegate.metaClass.respondsTo(spoon.instance, 'executeTransformation',
			TransMeta, Boolean, Boolean, Boolean, Boolean, Boolean, Date, Boolean)) {
			delegate.executeTransformation(tm, true, false, false, false, false, new Date(), false)
		}
		tg = delegate?.activeTransGraph
		if(tg) {
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
			catch(Exception e) {}
		}
	}
}

Spoon.metaClass.runJob = { jobMeta ->
	jm = jobMeta ?: spoon?.getActiveJob()
	if(jm != null) {
		spoon?.executeJob(jm, true, false, new Date(), false, null, 0);
	}
}

Trans.metaClass.run = { args ->
	if(!delegate.isReadyToStart()) delegate.prepareExecution(args)
	delegate.startThreads();
	delegate.waitUntilFinished();
	delegate
}

TransMeta.metaClass.run = { args ->
	
	def trans = new Trans(delegate)
	trans.run(args)
	delegate
}


// Database methods
GroovyConsoleBaseScript.metaClass.database = { dbName -> spoon?.activeDatabases?.find {it.name?.equalsIgnoreCase(dbName)} }
GroovyConsoleBaseScript.metaClass.db = {database(it)}
GroovyConsoleBaseScript.metaClass.databases = {spoon?.activeDatabases}
GroovyConsoleBaseScript.metaClass.dbs = {databases()}
GroovyConsoleBaseScript.metaClass.createDatabase = {args ->
	meta = new DatabaseMeta()
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
		repoDir = spoon?.repository?.findDirectory(dir);
		
		// Try to load the name as a job first
		try{ return spoon?.repository?.loadJob(objName, repoDir, null, false, null) } catch(Exception e) {}
				
		// then a transformation
		try{ return spoon?.repository?.loadTransformation(objName, repoDir, null, false, null)} catch(Exception e) {}
		
		tryDir = spoon?.repository?.findDirectory(name)
		return tryDir ? new RepositoryDirectoryDecorator(tryDir) : null
		
		// TODO - others?
	}
	catch(Exception e) {
		e.printStackTrace(System.err)
		return null
	}
}

RepositoryDirectoryDecorator.metaClass.propertyMissing << { name ->
	if(RepoHelper.metaClass.respondsTo(delegate,name)) return delegate?."$name"()
	null
}

// Step methods
StepMeta.metaClass.openDialog = {
	final String name = delegate.name
	final smi = delegate.stepMetaInterface

	spoon.display.asyncExec(new Runnable() {
		public void run() {
		  try {
			def c = Class.forName(smi?.dialogClassName)
			def d = c.newInstance(Spoon.instance?.shell, smi, trans(), name)
			d?.open()
		  } catch (Exception e) {
		   e.printStackTrace(System.err);
		  }
		}
	  });
}

StepMeta.metaClass.dlg = {openDialog()}

StepMeta.metaClass.propertyMissing << { name ->
	if(StepMeta.metaClass.respondsTo(delegate,name)) return delegate?."$name"()
	null
}

// Plugin methods
plugins = pluginRegistry

PluginRegistry.metaClass.propertyMissing << { name ->
	def p = delegate.findPluginWithName(
		delegate.pluginTypes?.find { pluginType ->
			delegate.findPluginWithName(pluginType, name)
		}, name)
	if(p) return p
	p = delegate.findPluginWithId(
		delegate.pluginTypes?.find { pluginType ->
			delegate.findPluginWithId(pluginType, name)
		}, name)
}

Plugin.metaClass.makeNew = {
	def c = plugins?.loadClass(delegate)
	c?.setDefault()
	def pid = plugins?.getPluginId(delegate.pluginType, c)
	def mdg = new StepMeta(pid, delegate.name, c)
}

Plugin.metaClass.addNew = {
	def mdg = delegate.makeNew()
	mdg?.location= new Point(20,20)
	mdg?.draw = true
	activeTrans()?.addStep(mdg)
	async {
		spoon?.refreshTree()
		activeTransGraph()?.redraw();
	}
}

// Threading methods
async = {c ->
	
	spoon?.display?.asyncExec(new Runnable() {
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
	
	spoon?.display?.syncExec(new Runnable() {
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
	obj.class.declaredMethods.find {it.name?.equals(name)}.each { it.accessible = true }
	obj.class.declaredFields.find {it.name?.equals(name)}.each { it.accessible = true }
}

// Operator overloading
TransMeta.metaClass.plus = {x ->
	if(x instanceof StepMeta) {
		x.draw = true
		x.location= new Point(20,20)
		delegate.addStep(x)
		def tg = spoon?.activeTransGraph
		if(tg?.managedObject == delegate) {
			async { 
				spoon?.refreshTree()
				tg?.redraw()
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
	else if(x instanceof DatabaseMeta) {
		delegate.addDatabase(x)
		delegate
	}
	delegate
}

StepMeta.metaClass.rightShift = { x ->
	if(x instanceof StepMeta) {
		delegate.parentTransMeta?.addTransHop(new TransHopMeta(delegate, x))
		async {
			spoon?.refreshTree()
			activeTransGraph()?.redraw()
		}
	}
	// return right op so hops can be chained
	x
}

StepMeta.metaClass.leftShift = { x ->
	if(x instanceof StepMeta) {
		delegate.parentTransMeta?.addTransHop(new TransHopMeta(x, delegate))
		async {
			spoon?.refreshTree()
			activeTransGraph()?.redraw()
		}
	}
	// return right op so hops can be chained
	x
}