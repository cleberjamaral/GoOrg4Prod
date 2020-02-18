package annotations;

public class Capability extends Annotation {

	public Capability(String id, double effort) {
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
	
	public Capability clone() {
		Capability clone = new Capability(this.id, (double) this.value);
	
	    return clone;
	}

}
