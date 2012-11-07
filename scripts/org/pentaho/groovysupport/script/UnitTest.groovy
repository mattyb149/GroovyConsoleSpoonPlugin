package org.pentaho.groovysupport.script

import org.pentaho.di.core.*
import org.pentaho.di.core.exception.*

// Unit Test helpers
class UnitTest {
	
	/**
     *  Check the 2 lists comparing the rows in order.
     *  If they are not the same fail the test. 
     */
    public static void checkRows(List<RowMetaAndData> rows1, List<RowMetaAndData> rows2)
    {
        int idx = 1;
        if ( rows1.size() != rows2.size() )
        {
            fail("Number of rows is not the same: " + 
                   rows1.size() + " and " + rows2.size());
        }
        Iterator<RowMetaAndData> it1 = rows1.iterator();
        Iterator<RowMetaAndData> it2 = rows2.iterator();
        
        while ( it1.hasNext() && it2.hasNext() )
        {
            RowMetaAndData rm1 = it1.next();
            RowMetaAndData rm2 = it2.next();
            
            Object[] r1 = rm1.data
            Object[] r2 = rm2.data
            
            if ( rm1.size() != rm2.size() )
            {
                fail("row nr $idx is not equal");
            }
            def fields = new Integer[rm1.size()];
            (0..rm1.size()-1).each { fields[it] = it }
            
            try {
                if ( rm1.getRowMeta().compare(r1, r2, fields) != 0 )
                {
                    fail("row nr $idx is not equal");
                }
            } catch (KettleValueException e) {
                fail("row nr $idx is not equal");
            }                
            idx++;
        }
    }
	
	static main(args) {
		
	}
}
