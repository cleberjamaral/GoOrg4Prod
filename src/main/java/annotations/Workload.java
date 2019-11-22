package annotations;

public class Workload {
	private String id;
	private double effort;

	public Workload(String id, double effort) {
		super();
		this.id = id;
		this.effort = effort;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public double getEffort() {
		return effort;
	}
	
	public void setEffort(double d) {
		this.effort = d;
	}
	
	@Override
	public String toString() {
		return "workload[id=" + id + ",effort=" + String.format("%.1f", effort) + "]";
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
