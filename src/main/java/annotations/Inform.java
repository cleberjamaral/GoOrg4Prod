package annotations;

public class Inform extends Annotation{

	public Inform(String id, double amount) {
		super(id, amount);
	}
	
	@Override
	public Object getValue() {
		return (double) value;
	}
	
	@Override
	public void setValue(Object amount) {
		this.value = (double) amount;
	}

	public Inform clone() {
		Inform clone = new Inform(this.id, (double) this.value);
	
	    return clone;
	}
}
