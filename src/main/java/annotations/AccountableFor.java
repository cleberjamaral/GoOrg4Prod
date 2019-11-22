package annotations;

import organisation.goal.GoalNode;

public class AccountableFor extends Annotation {

	public AccountableFor(GoalNode value) {
		super(value.getGoalName(), value);
	}

	@Override
	public GoalNode getValue() {
		return (GoalNode) this.value;
	}

	@Override
	public void setValue(Object value) {
		value = (GoalNode) this.value;		
	}

	public AccountableFor clone() {
		AccountableFor clone = new AccountableFor(this.getValue());

		return clone;
	}
}
