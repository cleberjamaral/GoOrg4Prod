package annotations;

import organisation.goal.GoalNode;

public class Inform extends Annotation {
	
	protected GoalNode recipient;

	public GoalNode getRecipient() {
		return recipient;
	}

	public void setRecipient(GoalNode recipient) {
		this.recipient = recipient;
	}

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

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[id=" + this.id + ", recipient=" + this.recipient.getGoalName()
				+ ",value=" + String.format("%.1f", this.value) + "]";
	}
	
	public Inform clone() {
		Inform clone = new Inform(this.id, recipient, (double) this.value);
	
	    return clone;
	}
}
