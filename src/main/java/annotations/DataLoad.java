package annotations;

import java.text.DecimalFormat;

import organisation.goal.GoalNode;

public class DataLoad extends Annotation {
	
	protected GoalNode sender;
	protected String senderName;
	protected double value;

	public GoalNode getSender() {
		return sender;
	}

	public void setSender(GoalNode sender) {
		this.sender = sender;
		this.senderName = sender.getGoalName();
	}

	public DataLoad(String id, GoalNode sender, double amount) {
        super(id);
        this.value = amount;
		this.sender = sender;
	}
	
	/**
	 * A dataload with a senderName in string is a previous state
	 * when the sender goal is still unknown
	 * 
	 * @param id of the annotation
	 * @param recipientName the name of the goal that will be informed
	 * @param amount of data
	 */
	public DataLoad(String id, String senderName, double amount) {
        super(id);
        this.value = amount;
        this.sender = null;
		this.senderName = senderName;
	}
	
	public Object getValue() {
		return (double) value;
	}
	
	public void setValue(Object amount) {
		this.value = (double) amount;
	}

    public String getSenderName() {
		return senderName;
	}
    
	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("#.##");
		return this.getClass().getSimpleName().substring(0, 1) + "[" + this.id + ":" + this.sender.getGoalName()
				+ ":" + df.format(value) + "]";
	}
	
	public DataLoad clone() {
		DataLoad clone = new DataLoad(this.id, this.sender.getGoalName(), (double) this.value);
	
	    return clone;
	}

	/**
	 * A dataload is equal to another if the id and sender are equal
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
		DataLoad other = (DataLoad) obj;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;
		if (sender == null) {
			if (other.getSender() != null)
				return false;
		} else if (!getSenderName().equals(other.getSenderName()))
			return false;
		return true;
	}

}
