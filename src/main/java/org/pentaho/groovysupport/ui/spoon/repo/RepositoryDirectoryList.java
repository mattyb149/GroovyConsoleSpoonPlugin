package org.pentaho.groovysupport.ui.spoon.repo;

import java.util.ArrayList;

import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.ui.spoon.Spoon;

public class RepositoryDirectoryList extends ArrayList<RepositoryDirectoryDecorator> {

	/** Serial UID */
	private static final long serialVersionUID = -6972139329970781623L;
	
	RepositoryDirectoryDecorator rootDir;
	
	public RepositoryDirectoryList(RepositoryDirectoryDecorator rootDir) {
		this.rootDir = rootDir;
	}
	
	public RepositoryDirectoryList ls(String path) {
		String lsPath = path;
		
		if(path == null) {
			lsPath = rootDir.getPath();
		}
		else if(!path.startsWith(RepositoryDirectory.DIRECTORY_SEPARATOR)) {
			lsPath = rootDir.getPath() + RepositoryDirectory.DIRECTORY_SEPARATOR + path;
		}
		
		try {
			RepositoryDirectoryDecorator rdd = new RepositoryDirectoryDecorator(Spoon.getInstance().getRepository().findDirectory(lsPath));
			return rdd.ls(null);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
		
	}
	
	public void list(String path) {
		String lsPath = path;
		
		if(path == null) {
			lsPath = rootDir.getPath();
		}
		else if(!path.startsWith(RepositoryDirectory.DIRECTORY_SEPARATOR)) {
			lsPath = rootDir.getPath() + RepositoryDirectory.DIRECTORY_SEPARATOR + path;
		}
		list(lsPath);
	}
}
