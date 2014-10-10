/**
 * 
 */
package soot;

import java.util.LinkedList;

/**
 * @author Peng Liu from Purdue
 *
 * <lpxz.ust.hk@gmail.com>
 */
public class PhantomSupport {

	/**
	 * @param args
	 */
	
	 public  static LinkedList<String> phantomList = new LinkedList<String> ();
	


	 public static void addPhantom(String str)// user specified.
	 {
		 phantomList.add(str);
	 }
	 public static boolean isPhantom(SootClass sc)
	 {
		 for(String str: phantomList)
		 {
			 if(sc.getName().startsWith(str))
				 return true;
		 }
		 return false;
	 }
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
