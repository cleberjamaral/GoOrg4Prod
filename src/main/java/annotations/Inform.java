package annotations;

import java.text.DecimalFormat;

import organisation.goal.GoalNode;

public class Inform extends Annotation {
	
    protected GoalNode recipient;
    protected double value;

	public GoalNode getRecipient() {
		return recipient;
	}

	public void setRecipient(GoalNode recipient) {
		this.recipient = recipient;
	}

	public Inform(String id, GoalNode recipient, double amount) {
        super(id);
        this.value = amount;
		this.recipient = recipient;
	}
	
	public Object getValue() {
		return (double) value;
	}
	
	public void setValue(Object amount) {
		this.value = (double) amount;
	}

	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("#.##");
		return this.getClass().getSimpleName().substring(0, 1) + "[" + this.id + ":" + this.recipient.getGoalName()
				+ ":" + df.format(value) + "]";

	}
	
	public Inform clone() {
		Inform clone = new Inform(this.id, recipient, (double) this.value);
	
	    return clone;
	}
}
