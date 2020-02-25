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
		this.recipientName = recipient.getGoalName();
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
		return this.getClass().getSimpleName().substring(0, 1) + "[" + this.id + ":" + this.recipientName
				+ ":" + df.format(value) + "]";
	}
	
	public Inform clone() {
		// using recipient goalname, later this.recipient must receive th correct reference
		Inform clone = new Inform(this.id, this.recipientName, (double) this.value);
	
	    return clone;
	}
	
	/**
	 * An inform is equal to another if the id and sender are equal
	 * i.e. the amount is not checked
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Inform other = (Inform) obj;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;
		if (recipient == null) {
			if (other.getRecipient() != null)
				return false;
		} else if (!getRecipientName().equals(other.getRecipientName()))
			return false;
		return true;
	}

}
