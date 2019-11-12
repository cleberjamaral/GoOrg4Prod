package properties;

public class Workload {
	private String id;
	private int effort;

	public Workload(String id, int effort) {
		super();
		this.id = id;
		this.effort = effort;
	}
	
	public String getSymbol() {
		return id;
	}
	
	public void setSymbol(String symbol) {
		this.id = symbol;
	}
	
	public int getWorkload() {
		return effort;
	}
	
	public void setWorkload(int effort) {
		this.effort = effort;
	}
	
	@Override
	public String toString() {
		return "workload[id=" + id + ",effort=" + effort + "]";
	}
}
