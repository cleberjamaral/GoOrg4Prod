package properties;

public class Throughput {
	private String id;
	private double amount;

	public Throughput(String id, double amount) {
		super();
		this.id = id;
		this.amount = amount;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public void setAmount(double d) {
		this.amount = d;
	}
	
	@Override
	public String toString() {
		return "workload[id=" + id + ",amount=" + String.format("%.1f", amount) + "]";
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
		Throughput other = (Throughput) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public Throughput clone() {
		Throughput clone = new Throughput(this.id, this.amount);
	
	    return clone;
	}
}
