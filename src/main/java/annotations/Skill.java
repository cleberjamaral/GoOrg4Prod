package annotations;

public class Skill extends Annotation {

	public Skill(String id) {
		super(id);
	}
	
	public Skill clone() {
		Skill clone = new Skill(this.id);
	
	    return clone;
	}

}
