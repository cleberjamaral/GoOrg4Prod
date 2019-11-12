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
	
	public void setId(String id) {
		this.id = id;
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Workload other = (Workload) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public Workload clone() {
		Workload clone = new Workload(this.id, this.effort);
	
	    return clone;
	}
}
