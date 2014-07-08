package org.pentaho.gremlin.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.pentaho.groovy.ui.spoon.GroovyExtFilter;

import com.tinkerpop.gremlin.groovy.console.Console;

public class GremlinGroovyConsoleMain {

  private StringBuilder initialScriptContents = null;

  public void run( String[] args ) {

    String initialScriptPath = null;

    try {
      initialScriptPath =
          new File( this.getClass().getClassLoader().getResource( "staging.groovy" ).toURI() ).getAbsolutePath();
    } catch ( Exception e ) {
      e.printStackTrace( System.err );
    }

    Console console = new Console( initialScriptPath );
  }

  public static void main( String[] args ) {

    File pluginFolderFile =
        new File( GremlinGroovyConsoleMain.class.getProtectionDomain().getCodeSource().getLocation().getPath() )
            .getParentFile();
    GremlinGroovyConsoleMain gcm = new GremlinGroovyConsoleMain( );
    try {
      gcm.run( args );
      // TODO - wait for closed event
      // while(true) {Thread.sleep(200);}
    } catch ( Exception e ) {
      e.printStackTrace();
      System.exit( 1 );
    }
  }

  /*public String buildInitialScript( String primerScript, File pluginFolder ) {
    StringBuilder sBuilder = new StringBuilder( ( primerScript == null ) ? "" : primerScript + System.lineSeparator() );

    // Evaluate all groovy scripts in the JAR
    String scriptName = "";
    JarFile jarFile = null;
    URL jarURL = GremlinGroovyConsoleMain.class.getProtectionDomain().getCodeSource().getLocation();
    boolean isJar = true;
    if ( jarURL.getProtocol().equalsIgnoreCase( "file" ) ) {
      File checkDir = new File( jarURL.getFile() );
      isJar = !checkDir.isDirectory();
    }
    if ( isJar ) {
      try {
        jarFile = new JarFile( URLDecoder.decode( jarURL.getPath(), "UTF-8" ) );
        for ( Enumeration<JarEntry> em = jarFile.entries(); em.hasMoreElements(); ) {
          JarEntry jarEntry = em.nextElement();
          if ( !jarEntry.isDirectory() ) {
            scriptName = jarEntry.toString();
            if ( scriptName.endsWith( ".groovy" ) ) {
              ZipEntry entry = jarFile.getEntry( scriptName );
              InputStream inStream = jarFile.getInputStream( entry );
              BufferedReader br = new BufferedReader( new InputStreamReader( inStream ) );
              addLinesToScript( sBuilder, br );
            }
          }
        }
      } catch ( IOException e ) {
        System.out.println( "Error loading script: " + scriptName + ", exception = " + e.getMessage() );
      } finally {
        if ( jarFile != null ) {
          try {
            jarFile.close();
          } catch ( IOException e ) {
            e.printStackTrace();
          }
        }
      }
    } else {
      // might be running right from the GroovyConsoleSpoonPlugin project, so use the resources dir
      File f = new File( "src/main/resources" );
      File[] scripts = f.listFiles( new FilenameFilter() {

        @Override
        public boolean accept( File dir, String name ) {
          return name.endsWith( ".groovy" );
        }
      } );
      if ( scripts != null ) {
        for ( File script : scripts ) {
          try {
            addLinesToScript( sBuilder, new BufferedReader( new InputStreamReader( new FileInputStream( script ) ) ) );
          } catch ( Exception e ) {
            e.printStackTrace();
          }
        }
      }
    }

    // Load any staging scripts placed in the directory with the plugin
    String[] scriptList = pluginFolder.list( new GroovyExtFilter() );
    if ( scriptList != null ) {
      for ( String script : scriptList ) {
        try {
          addLinesToScript( sBuilder, new BufferedReader( new FileReader( pluginFolder.getAbsolutePath()
              + File.separator + script ) ) );
        } catch ( Exception cfe ) {
          System.out.println( "Error loading script: " + script );
        }
      }
    }
    return sBuilder.toString();
  }*/

  protected void addLinesToScript( StringBuilder sBuilder, BufferedReader br ) {
    if ( sBuilder == null ) {
      return;
    }

    StringBuilder script = new StringBuilder();
    try {
      String line = br.readLine();
      while ( line != null ) {
        script.append( line );
        script.append( System.lineSeparator() );
        line = br.readLine();
      }

      br.close();
    } catch ( IOException ioe ) {
    }
    sBuilder.append( script );
  }
}
