package annotations;

import java.text.DecimalFormat;

import organisation.goal.GoalNode;

public class Inform extends Annotation {
	
    protected GoalNode recipient;
    protected String recipientName;
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
		this.recipientName = recipient.getGoalName();
	}

	/**
	 * An inform with a recipientName in string is a previous state
	 * when the recipient goal is still unknown
	 * 
	 * @param id of the annotation
	 * @param recipientName the name of the goal that will be informed
	 * @param amount of data
	 */
	public Inform(String id, String recipientName, double amount) {
        super(id);
        this.value = amount;
        this.recipient = null;
		this.recipientName = recipientName;
	}
	
	public Object getValue() {
		return (double) value;
	}
	
	public void setValue(Object amount) {
		this.value = (double) amount;
	}

    public String getRecipientName() {
		return recipientName;
	}

	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
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
