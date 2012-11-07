package org.pentaho.groovysupport.ui.spoon.repo;

import java.io.File;

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.spoon.Spoon;

public class RepoHelper {
	
	private static RepoHelper instance = null;
	
	public static RepoHelper getInstance() {
		if(instance == null) instance = new RepoHelper();
		return instance;
	}
	
	public TransMeta getTrans(String filename) {
		try {
			Spoon spoon = Spoon.getInstance();
    		Repository repo = spoon.getRepository();
    		String dir = new File(filename).getParent().replace(File.separator, RepositoryDirectory.DIRECTORY_SEPARATOR);
    		String transname = new File(filename).getName();
    		if(dir == null) dir = RepositoryDirectory.DIRECTORY_SEPARATOR;
    		RepositoryDirectoryInterface repoDir = repo.findDirectory(dir);
    		return repo.loadTransformation(transname, repoDir, null, false, null);
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
	}
	
	public JobMeta getJob(String filename) {
		try {
			Spoon spoon = Spoon.getInstance();
    		Repository repo = spoon.getRepository();
    		String dir = new File(filename).getParent().replace(File.separator, RepositoryDirectory.DIRECTORY_SEPARATOR);
    		String jobname = new File(filename).getName();
    		if(dir == null) dir = RepositoryDirectory.DIRECTORY_SEPARATOR;
    		RepositoryDirectoryInterface repoDir = repo.findDirectory(dir);
    		return repo.loadJob(jobname, repoDir, null, null);
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
	}
	
	public RepositoryDirectoryList ls(String path) {
		String lsPath = path;
		
		if(path == null) { 
			lsPath = RepositoryDirectory.DIRECTORY_SEPARATOR;
		}
		else if (!path.startsWith(RepositoryDirectory.DIRECTORY_SEPARATOR)) {
			lsPath = RepositoryDirectory.DIRECTORY_SEPARATOR + path;
		}
		try {
			RepositoryDirectoryList dirList = new RepositoryDirectoryList(new RepositoryDirectoryDecorator(Spoon.getInstance().getRepository().findDirectory(lsPath)));
			return dirList.ls(lsPath);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
	}
	
	public void list(String path) {
		String lsPath = (path == null) ? RepositoryDirectory.DIRECTORY_SEPARATOR : path;
		try {
			RepositoryDirectoryDecorator rootDir = new RepositoryDirectoryDecorator(Spoon.getInstance().getRepository().findDirectory(lsPath));
			rootDir.list(lsPath);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}    		
	}
	
	public RepositoryDirectoryDecorator dir(String path) {
		String lsPath = (path == null) ? RepositoryDirectory.DIRECTORY_SEPARATOR : path;
		try {
			return new RepositoryDirectoryDecorator(Spoon.getInstance().getRepository().findDirectory(lsPath)).dir(lsPath);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
	}
	
	public RepositoryDirectoryDecorator mkdir(String path) {
		try {
			return new RepositoryDirectoryDecorator(Spoon.getInstance().getRepository().findDirectory(RepositoryDirectory.DIRECTORY_SEPARATOR)).mkdir(path);
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
			return null;
		}		
	}
	
	public void rmdir(String path) {
		try {
			new RepositoryDirectoryDecorator(Spoon.getInstance().getRepository().findDirectory(RepositoryDirectory.DIRECTORY_SEPARATOR)).rmdir(path);
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
		}		
	}
}	