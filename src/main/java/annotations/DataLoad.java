package annotations;

import organisation.goal.GoalNode;

public class DataLoad extends Annotation {
	
	protected GoalNode sender;

	public GoalNode getSender() {
		return sender;
	}

	public void setSender(GoalNode sender) {
		this.sender = sender;
	}

	public DataLoad(String id, GoalNode sender, double amount) {
		super(id, amount);
		this.sender = sender;
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
		return this.getClass().getSimpleName() + "[id=" + this.id + ", sender=" + this.sender.getGoalName()
				+ ",value=" + String.format("%.1f", this.value) + "]";
	}
	
	public DataLoad clone() {
		DataLoad clone = new DataLoad(this.id, sender, (double) this.value);
	
	    return clone;
	}
}
