package simplelogger;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * @author cleber
 *
 */
public class SimpleLogger {
	
	int level = 3; //1 see everything, 2 hide trace, 3 hide trace and debug, 4 hide trace, debug and warn
	private static SimpleLogger singleObj;
	boolean printToFile = true;
	
	private SimpleLogger(int level) {
		// Maintain a change if it was done
		if (level != 1) 
			this.level = level;
		if (printToFile) {
			PrintStream fileOut;
			try {
				fileOut = new PrintStream("./log.log");
				System.setOut(fileOut);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
    public static SimpleLogger getInstance(int level){
        if(singleObj == null){
        	singleObj = new SimpleLogger(level);
        }
        return singleObj;
    }	
	
    public static SimpleLogger getInstance(){
        if(singleObj == null){
        	singleObj = new SimpleLogger(1);
        }
        return singleObj;
    }	

    public void trace(String msg){
    	if (level <= 1) System.out.println("TRACE: "+msg);
	}
	
	public void debug(String msg){
		if (level <= 2) System.out.println("DEBUG: "+msg);
	}

	public void info(String msg){
		if (level <= 3) System.out.println("INFO : "+msg);
	}
	
	public void warn(String msg){
		if (level <= 4) System.out.println("WARN : "+msg);
	}

	public void error(String msg){
		System.out.println("ERROR: "+msg);
	}

	public void fatal(String msg){
		System.out.println("FATAL: "+msg);
	}

}