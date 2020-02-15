package annotations;

import organisation.goal.GoalNode;

public class Inform extends Annotation {
	
	protected GoalNode recipient;

	public Inform(String id, GoalNode recipient, double amount) {
		super(id, amount);
		this.recipient = recipient;
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
		Inform clone = new Inform(this.id, recipient, (double) this.value);
	
	    return clone;
	}
}
