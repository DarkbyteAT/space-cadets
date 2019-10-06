package me.atlne.spirograph;

import java.util.Vector;

//Curve defined parametrically by:
//x = (R-r)*cos(t) + O*cos(((R-r)/r)*t)
//y = (R-r)*sin(t) - O*sin(((R-r)/r)*t)
public class Hypocycloid {

	//Stores R value (radius of big spirograph circle)
	public double R;
	//Stores r value (radius of small, moving circle)
	public double r;
	//Stores pen offset from centre of small circle called O
	public double O;
	//Stores time step to take each iteration (lower = more detail)
	public double dt;
	
	//Constructor for hypocycloid class, takes in parameters
	public Hypocycloid(double R, double r, double O, double dt) {
		this.R = R;
		this.r = r;
		this.O = O;
		this.dt = dt;
	}
	
	//Calculates position of point at given time "t"
	//Index 0 = x-pos, 1 = y-pos
	public double[] calculatePoint(double t) {
		return new double[] {
			((R - r) * Math.cos(t)) + (O * Math.cos(((R-r) / r) * t)),
			((R - r) * Math.sin(t)) + (O * Math.sin(((R-r) / r) * t))
		};
	}
	
	//Calculates a vector (list) of all of the positions between two times
	public Vector<double[]> calculatePoints(double startT, double endT) {
		//Creates vector to store result
		Vector<double[]> result = new Vector<>();
		
		//Uses for loop to iterate over all t values between startT and endT with a difference of dt
		for(double t = startT; t < endT; t += dt) {
			result.add(calculatePoint(t));
		}
		
		//Returns the resulting vector of points
		return result;
	}
}