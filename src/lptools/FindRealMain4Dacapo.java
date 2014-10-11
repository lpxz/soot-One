/**
 * 
 */
package lptools;

/**
 * @author Peng Liu from Purdue
 *
 * <lpxz.ust.hk@gmail.com>
 */




import soot.*;
import soot.jimple.*;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;
import soot.tagkit.LineNumberTag;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;



public class FindRealMain4Dacapo {

	
	    
//	public static final String excludeInclude =  "-i org.apache.derby. -x java. -x javax -x sun -x com -x jrockit"
//				+ " -x edu -x checkers -x org.xmlpull -x org.apache.xml"
//				+ " -x org.apache.xpath -x soot -x org.jgrapht";
	

	public static void main(String[] args) throws Exception{

		// Visitor.setObserverClass("edu.hkust.leap.monitor.Monitor");
			//-keep-line-number -app -w -p cg.spark enabled -f c -p cg reflection-log:/home/lpxz/work/dacapo/out/avrora-small/refl.log -cp /home/lpxz/pool/jdk1.6.0_26/jre/lib/resources.jar:/home/lpxz/pool/jdk1.6.0_26/jre/lib/rt.jar:/home/lpxz/pool/jdk1.6.0_26/jre/lib/sunrsasign.jar:/home/lpxz/pool/jdk1.6.0_26/jre/lib/jsse.jar:/home/lpxz/pool/jdk1.6.0_26/jre/lib/jce.jar:/home/lpxz/pool/jdk1.6.0_26/jre/lib/charsets.jar:/home/lpxz/pool/jdk1.6.0_26/jre/lib/modules/jdk.boot.jar:/home/lpxz/pool/jdk1.6.0_26/jre/classes:/home/lpxz/eclipse/workspacePA_icse/predict-inst/bin:/home/lpxz/eclipse/workspacePA_icse/predict-engine/bin:/home/lpxz/eclipse/workspacePA_icse/predict-engine/lib/commons-cli-1.2.jar:/home/lpxz/eclipse/workspacePA_icse/predict-engine/lib/ant.jar:/home/lpxz/eclipse/workspacePA_icse/predict-engine/lib/jigsaw-sexpr.jar:/home/lpxz/eclipse/workspacePA_icse/predict-engine/lib/h2-1.3.171.jar:/home/lpxz/eclipse/workspacePA_icse/predict-engine/lib/xercesImpl.jar:/home/lpxz/eclipse/workspacePA_icse/predict-engine/lib/ant-launcher.jar:/home/lpxz/eclipse/workspacePA_icse/predict-inst/lib/soot-jeff.jar:/home/lpxz/work/dacapo/out/avrora-small -main-class Harness Harness -i org.apache -i org.w3c

			String subjectname = args[0];
			
			outputFormat= "c";
			cpath="/home/lpxz/work/dacapo/out/"+subjectname+"-small";// TODO change folder directory.
			mainClass = "Harness";
	    	String excludeOption = "";
			String includeOption = " -i org.apache -i org.w3c";    		
			String reflString = " -p cg reflection-log:"+cpath+"/refl.log";    		
			String jceJar = "/home/lpxz/java_standard/jre/lib/jce.jar";
			String rtJar = "/home/lpxz/java_standard/jre/lib/rt.jar";
			// for passing the type checker, pass only in analysis, not in transform.
			//-p jb.tr enabled:false -p cg.spark ignore-types:true 
			
			String interString = "-keep-line-number -app -w -p cg.spark enabled -f " + outputFormat	//-allow-phantom-refs -no-bodies-for-excluded 
				    + reflString
					+ " -cp "
					+ System.getProperty("sun.boot.class.path") + ":" + System.getProperty("java.class.path") + ":" +cpath// powerful classpath.
//					+" -d " + Main.getTempSubDirectory(config.DIR_RECORD)
					+ " -main-class "
					+ mainClass
					+ " "
					+ mainClass
					+excludeOption
					+includeOption
					+ " -d ./sootOutput/"+subjectname;
				
			Object[] finalArgs = interString.split(" ");		
			System.out.println(interString);
			
			System.out.println("output record version to: " + path);
			FindRealMain4Dacapo finder = new FindRealMain4Dacapo();
			finder.transformRecordVersion(interString); //
	}
	
	
	

	

	private FileWriter printer;
	public static String cpath;
	public static String path;
	public static String outputFormat;
	public static String mainClass;

	public void print(String str) {
		System.err.println(str);
		try {
			printer.write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	

	public static String monitorClass = "lptools.Monitor";
	private void transformRecordVersion(String interString) throws Exception{
		String[] finalArgs = interString.split(" ");
		
		soot.Main mainObject =soot.Main.v();
		Method[] theMethods = mainObject.getClass().getDeclaredMethods();
		Method theMethod =null; 
		for(Method tmp : theMethods)
		{
			if(tmp.getName().equals("processCmdLine"))
			{
				theMethod = tmp;
			}
		}
		theMethod.setAccessible(true);
		theMethod.invoke(mainObject, new Object[]{finalArgs}); // soot.Main.v().processCmdLine(finalArgs);
		
		
		setWPOptions();
		
		Scene.v().loadClassAndSupport(monitorClass);// must be in front
		
		Scene.v().loadNecessaryClasses();
		
		
		Pack jtp = PackManager.v().getPack("jtp");
		jtp.add(new Transform("jtp.Recorder",
						new BodyTransformer() {
							
							@Override
							protected void internalTransform(Body b, String phaseName, Map options) {
								//TODO put them into the dacapo blacklist.
								
//								// help eclipse
//								if(!b.getMethod().getDeclaringClass().getName().equals("org.eclipse.core.runtime.adaptor.EclipseStarter"))
//								{
//									return;
//								}
								
//								if(b.getMethod().getName().equals("main"))
								{
					
									 if(b.getMethod().getName().equals(SootMethod.constructorName) || b.getMethod().getName().equals(SootMethod.staticInitializerName) )
										 return;
									 for(String exclude: excludeList)
									 {
										 if(b.getMethod().getDeclaringClass().getName().startsWith(exclude))// do not touch harmful stuff
										 {
											 return;
										 }
									 }								 
									 
									 PatchingChain<Unit> units =b.getUnits();
									 Stmt s =((JimpleBody)b).getFirstNonIdentityStmt();


									 
//									 if(b.getMethod().getDeclaringClass().getName().equals("org.apache.fop.cli.Main")&& b.getMethod().getName().equals("startFOP"))
//									 {
//										 for(Unit u: b.getUnits())
//										 {
//											 LineNumberTag tag = (LineNumberTag)u.getTag("LineNumberTag");
//											 System.out.println(u + " " + tag.getLineNumber());
//											 
//										 }
//										 System.exit(1);
//										 
//									 }
									 
                                    // externalized sysout. need to set cp for monitor.class									 
//									 SootMethodRef mr = Scene.v().getMethod(
//												"<" + monitorClass + ": void " + "methodBegin" + "(java.lang.String)>")
//												.makeRef();
//
//									 units.insertBefore(Jimple.v().newInvokeStmt(
//													Jimple.v().newStaticInvokeExpr(mr, StringConstant.v(b.getMethod().getDeclaringClass() + "." + b.getMethod().getSignature()))), s);

									 
									 
									 

										
									 	
									 // inlined sysout(arg);  for eclipse, which cannot load lptools with its own classloader
									 Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
								     b.getLocals().add(tmpRef);
								     

								     // insert "tmpRef = java.lang.System.out;" 
								     units.insertBefore(Jimple.v().newAssignStmt( 
								                      tmpRef, Jimple.v().newStaticFieldRef( 
								                      Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), s);
									
								     SootClass javaIoPrintStream = Scene.v().getSootClass("java.io.PrintStream");
								     SootMethod toCall = javaIoPrintStream.getMethod("void println(java.lang.String)");   
								     
								   
//								     b.getMethod().getDeclaringClass() + "." + b.getMethod().getName()
								      units.insertBefore(Jimple.v().newInvokeStmt(
							                      Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), StringConstant.v(b.getMethod().getDeclaringClass() + "." + b.getMethod().getSignature()))), s);
//								   
								}
								
							}
						}));

		PackManager.v().runPacks();// 1
		PackManager.v().writeOutputExcept(excludeList);
		G.reset();

		System.err.println("***** Runtime version generated *****");
	}

//	private void transformReplayVersion() {
//
//		String argsOfmtrt = "-f "
//				+ outputFormat
//				+ " -pp -cp "
//				+ cpath
//				+ " -main-class "
//				+ mainClass
//				+ " "
//				+ mainClass
//				+ " -d "
//				+ path;
//
//		String interString = argsOfmtrt;
//		String[] finalArgs = interString.split(" ");
//		soot.Main.v().processCmdLine(finalArgs);
//
//		Options.v().set_app(true);
//
//		Visitor.setObserverClass(Parameters.REPLAYCLASS);
//		Scene.v().loadClassAndSupport(Visitor.observerClass);
//		Scene.v().loadNecessaryClasses();
//
//		Pack jtp = PackManager.v().getPack("jtp");
//		jtp
//				.add(new Transform("jtp.Replayer",
//						new AddInterestEventTransformer()));
//
//		PackManager.v().runPacks();// 1
//		PackManager.v().writeOutput();
//		G.reset();
//
//		System.err.println("--- Replay version generated ---");
//	}
	
	
	  public  static LinkedList<String> excludeList = new LinkedList<String> ();
	  public  static LinkedList<String> includeList = new LinkedList<String> ();

	    static
	    {
	    // the following packages are excluded in Soot by default
	    //	java., sun., javax., com.sun., com.ibm., org.xml., org.w3c., apple.awt., com.apple.
	    
	    //IMPORTANT: do not forget  to use writeOutputExcept() to make sure you do not dump the excluded classes. 	
	    excludeList.add("edu.hkust.");
	    excludeList.add("lptools.");
	    
	    excludeList.add("java.");
	    excludeList.add("javax.");
	    excludeList.add("sun.");
	    excludeList.add("sunw.");
	    excludeList.add("com.sun.");
	    excludeList.add("com.ibm.");
	    excludeList.add("com.apple.");
	    excludeList.add("apple.awt.");
//	    excludeList.add("org.xml.");
	    excludeList.add("org.h2.");
	    excludeList.add("jdbm.");
	    excludeList.add("aj.");
	    
	  

	    // avrora:
	    // it conflicts with the use of System.out.println().
	    // not sure what is going on.
	    // guess that it changes the system.out online to some other printstream and enforces the lock protection.
	    // currently, I insert a simple call which dumps the methods to a file, rather than directly using system.out.
	    
	    excludeList.add("org.dacapo.harness.");
	    excludeList.add("org.dacapo.parser.");
	    excludeList.add("org.apache.crimson.");
	    excludeList.add("org.apache.batik.dom.util.");// cool! after banning it, we get batik running now.
	    excludeList.add("org.eclipse.equinox");
	    excludeList.add("org.apache.xmlgraphics.util.Service");
	    excludeList.add("org.apache.derbyTesting.system.oe.");// testing utility


	    // helping type checker to avoid these:
	    PhantomSupport.addPhantom("org.python.core");// phantom means we do not even load the class bodies, avoid type checker errors :)
	    // exclude means we load the classes but do not transform them. 
	    // originally, phantom corresponds to only those loading failures.
	    PhantomSupport.addPhantom("$Proxy$HASHED");


	    
	    
        // great, we handle all dacapo benchmarks now!	    
	    
	    
//	    includeList.add("org.w3c.");//for jigsaw
//	    includeList.add("avrora.");//for avrora
	    
	    includeList.add("org.apache.lucene.");//for lucene
	    includeList.add("org.apache.xalan.");//for lucene
	    includeList.add("org.apache.xml.");//for lucene
	    includeList.add("org.apache.xerces.");//for lucene
	    
	    }
	    
	    

	public static void setWPOptions() {
		
		soot.Main.v().autoSetOptions();

		  PhaseOptions.v().setPhaseOption("cg.spark", "enabled:true");
	      PhaseOptions.v().setPhaseOption("cg.spark", "propagator:worklist");
	      PhaseOptions.v().setPhaseOption("cg.spark", "simple-edges-bidirectional:false");
	      PhaseOptions.v().setPhaseOption("cg.spark", "on-fly-cg:true");
	      PhaseOptions.v().setPhaseOption("cg.spark", "set-impl:double");
	      PhaseOptions.v().setPhaseOption("cg.spark", "double-set-old:hybrid");
	      PhaseOptions.v().setPhaseOption("cg.spark", "double-set-new:hybrid");
	     
	      

//		 Map map = PhaseOptions.v().getPhaseOptions("cg.spark");
//		 
//		 for(Object ke : map.keySet())
//		 {
//			 System.out.println("key:value " + ke + " "  + map.get(ke));
//			 
//		 }
		 
		 
		 
		 

	      
		
		Options.v().set_exclude(excludeList);
		Options.v().set_include(includeList); 
		
		PhaseOptions.v().setPhaseOption("jb", "enabled:true");
		Options.v().set_keep_line_number(true);
		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().set_whole_program(true);
		Options.v().set_app(true);

	
	}

}

