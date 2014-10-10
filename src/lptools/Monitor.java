/**
 * 
 */
package lptools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Peng Liu from Purdue
 *
 * <lpxz.ust.hk@gmail.com>
 */
public class Monitor {

    
	public static void methodBegin(String arg)
	{
	      PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter("executedMethods", true), true);
			  out.write(arg+"\n");
		      out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
