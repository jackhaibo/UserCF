package xietongguolv;

public class Ralated_user implements Comparable<Ralated_user>{
	private int id;
	private double simlarity=0.0;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Double getSimlarity() {
		return simlarity;
	}
	public void setSimlarity(double simlarity) {
		this.simlarity = simlarity;
	}
	@Override
	public int compareTo(Ralated_user o) {
		return this.getSimlarity().compareTo(o.getSimlarity());
	}
	 

}
