package properties;

public class Workload {
	private String symbol;
	private int workload;

	public Workload(String symbol, int workload) {
		super();
		this.symbol = symbol;
		this.workload = workload;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	public int getWorkload() {
		return workload;
	}
	
	public void setWorkload(int workload) {
		this.workload = workload;
	}	
}
