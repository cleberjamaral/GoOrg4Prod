package annotations;

import java.text.DecimalFormat;

public abstract class Annotation {

	protected String id;
	protected Object value;
	
	public Annotation(String id, Object value) {
		this.id = id;
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public abstract Object getValue();
	
	public abstract void setValue(Object value);
	
	@Override
	public String toString() {
		if (value instanceof Double) {
			DecimalFormat df = new DecimalFormat("#.##");
			return this.getClass().getSimpleName() + "[id=" + id + ",value=" + df.format(value) + "]";
		}
		else 
			return this.getClass().getSimpleName() + "[id=" + id + ",value=" + value.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Annotation other = (Annotation) obj;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;
		return true;
	}

	public abstract Annotation clone();

}
