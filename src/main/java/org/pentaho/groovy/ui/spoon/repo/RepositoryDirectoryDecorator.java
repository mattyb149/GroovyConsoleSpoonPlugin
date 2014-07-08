package org.pentaho.groovy.ui.spoon.repo;

import java.util.List;

import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.ui.spoon.Spoon;

public class RepositoryDirectoryDecorator implements RepositoryDirectoryInterface {
	private RepositoryDirectoryInterface iface;
	
	public RepositoryDirectoryDecorator(RepositoryDirectoryInterface iface) {
		this.iface = iface;
	}

	@Override
	public String getName() {
		return iface.getName();
	}

	@Override
	public ObjectId getObjectId() {
		return iface.getObjectId();
	}

	@Override
	public List<RepositoryDirectoryInterface> getChildren() {
		return iface.getChildren();
	}

	@Override
	public void setChildren(List<RepositoryDirectoryInterface> children) {
		iface.setChildren(children);			
	}

	@Override
	public List<RepositoryElementMetaInterface> getRepositoryObjects() {
		return iface.getRepositoryObjects();
	}

	@Override
	public void setRepositoryObjects(List<RepositoryElementMetaInterface> children) {
		iface.setRepositoryObjects(children);
	}

	@Override
	public boolean isVisible() {
		return iface.isVisible();
	}

	@Override
	public String[] getPathArray() {
		return iface.getPathArray();
	}

	@Override
	public RepositoryDirectoryInterface findDirectory(String path) {
		return iface.findDirectory(path);
	}

	@Override
	public RepositoryDirectoryInterface findDirectory(ObjectId id_directory) {
		return iface.findDirectory(id_directory);
	}

	@Override
	public RepositoryDirectoryInterface findDirectory(String[] path) {
		return iface.findDirectory(path);
	}

	@Override
	public ObjectId[] getDirectoryIDs() {
		return iface.getDirectoryIDs();
	}

	@Override
	public String getPath() {
		return iface.getPath();
	}

	@Override
	public int getNrSubdirectories() {
		return iface.getNrSubdirectories();
	}

	@Override
	public RepositoryDirectory getSubdirectory(int i) {
		return iface.getSubdirectory(i);
	}

	@Override
	public boolean isRoot() {
		return iface.isRoot();
	}

	@Override
	public RepositoryDirectoryInterface findRoot() {
		return iface.findRoot();
	}

	@Override
	public void clear() {
		iface.clear();		
	}

	@Override
	public void addSubdirectory(RepositoryDirectoryInterface subdir) {
		iface.addSubdirectory(subdir);		
	}

	@Override
	public void setParent(RepositoryDirectoryInterface parent) {
		iface.setParent(parent);
	}

	@Override
	public RepositoryDirectoryInterface getParent() {
		return iface.getParent();
	}

	@Override
	public void setObjectId(ObjectId id) {
		iface.setObjectId(id);
	}

	@Override
	public void setName(String directoryname) {
		iface.setName(directoryname);
	}

	@Override
	public String getPathObjectCombination(String transName) {
		return iface.getPathObjectCombination(transName);
	}

	@Override
	public RepositoryDirectoryInterface findChild(String name) {
		return iface.findChild(name);
	}
	
	public RepositoryDirectoryList ls(String path) {
		String lsPath = (path == null) ? iface.getPath() : path;
		RepositoryDirectoryList dirList = null;
		try {
			RepositoryDirectoryInterface pathIface = Spoon.getInstance().getRepository().findDirectory(lsPath);
			if(pathIface != null) {
				RepositoryDirectoryDecorator rootDir = new RepositoryDirectoryDecorator(pathIface);
				dirList = new RepositoryDirectoryList(rootDir);
			    		
    			dirList.add(new RepositoryDirectoryDecorator(rootDir));
	    		if(rootDir.getChildren() != null) {
		    		for(RepositoryDirectoryInterface childDir : rootDir.getChildren()) {
		    			dirList.addAll(ls(childDir.getPath()));
		    		}
	    		}
    		}
    		return dirList;
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
			return dirList;
		}
	}
	
	public void list(String path) {
		String lsPath = (path == null) ? iface.getPath() : path;
		try {
			
    		for(RepositoryDirectoryInterface childDir : ls(lsPath)) {
    			System.out.println(childDir.getPath());
    		}
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	public RepositoryDirectoryDecorator mkdir(String path) {
		if(path == null) return null;
		try {
    		Repository repo = Spoon.getInstance().getRepository();
    		RepositoryDirectoryInterface parentDir = repo.findDirectory(RepositoryDirectory.DIRECTORY_SEPARATOR);
    		String relPath = path;
			if(path.startsWith(RepositoryDirectory.DIRECTORY_SEPARATOR)) {
				relPath = path.substring(RepositoryDirectory.DIRECTORY_SEPARATOR.length());
			}
			else {		
				parentDir = repo.findDirectory(iface.getPath());
			}
		
    		return new RepositoryDirectoryDecorator(repo.createRepositoryDirectory(parentDir, relPath));
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
	}
	
	public void rmdir(String path) {
		try {
    		Repository repo = Spoon.getInstance().getRepository();
    		String relPath = path;
    		if(path == null) {
    			relPath = iface.getPath();
    		}
    		else {
				if(!path.startsWith(RepositoryDirectory.DIRECTORY_SEPARATOR)) {
					relPath = iface.getPath();
					if(!relPath.endsWith(RepositoryDirectory.DIRECTORY_SEPARATOR)) {
						relPath += RepositoryDirectory.DIRECTORY_SEPARATOR;
					}
					relPath += path;
				}
    		}
			RepositoryDirectoryInterface delDir = repo.findDirectory(relPath);
		
    		repo.deleteRepositoryDirectory(delDir);
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	public RepositoryDirectoryDecorator dir(String path) {
		try {
			String lsPath = (path.startsWith(RepositoryDirectory.DIRECTORY_SEPARATOR)) ? path : iface.getPath() + RepositoryDirectory.DIRECTORY_SEPARATOR + path;
			RepositoryDirectoryInterface dir = Spoon.getInstance().getRepository().findDirectory(lsPath);
			return new RepositoryDirectoryDecorator(dir);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
	}
}
