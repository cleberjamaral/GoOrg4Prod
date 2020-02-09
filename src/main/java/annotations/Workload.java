package annotations;

public class Workload extends Annotation {

	public Workload(String id, double effort) {
		super(id, effort);
	}
	
	@Override
	public Object getValue() {
		return (double) this.value;
	}
	
	@Override
	public void setValue(Object value) {
		this.value = (double) value;
	}
	
	public Workload clone() {
		Workload clone = new Workload(this.id, (double) this.value);
	
	    return clone;
	}

}
