package simplelogger;

public class SimpleLogger {
	
	int level = 1; //1 see everything, 2 hid trace, 3 hid trace and debug, 4 hid trace, debug and warn
	private static SimpleLogger singleObj;
	
	private SimpleLogger(int level) {
		this.level = level;
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
		System.out.println("TRACE: "+msg);
	}
	
	public void debug(String msg){
		if (level <= 2) System.out.println("DEBUG: "+msg);
	}

	public void info(String msg){
		if (level <= 3) System.out.println("INFO: "+msg);
	}
	
	public void warn(String msg){
		if (level <= 4) System.out.println("WARN: "+msg);
	}

	public void error(String msg){
		System.out.println("ERROR: "+msg);
	}

	public void fatal(String msg){
		System.out.println("FATAL: "+msg);
	}

}