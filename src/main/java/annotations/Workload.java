package annotations;

import java.text.DecimalFormat;

/**
 * @author cleber
 *
 */
public class Workload extends Annotation {

    protected double value;

	public Workload(String id, double effort) {
        super(id);
        this.value = effort;
	}
	
	public Object getValue() {
		return (double) this.value;
	}
	
	public void setValue(Object value) {
		this.value = (double) value;
	}
    
    @Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("#.##");
		return this.getClass().getSimpleName().substring(0, 1) + "[" + this.id + ":" + df.format(value) + "]";
    }
    
	public Workload clone() {
		Workload clone = new Workload(this.id, (double) this.value);
	
	    return clone;
	}

}
