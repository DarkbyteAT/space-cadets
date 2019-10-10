package me.atlne.circledetect;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class DetectedCircle {
	
	//Stores centre of circle
	public int x, y;
	//Stores radius of circle
	public int radius;
	//Stores certainty that circle exists
	public float certainty;
	
	public DetectedCircle(int x, int y, int radius, float certainty) {
		super();
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.certainty = certainty;
	}

	//Draws circle onto image
	public void draw(WritableImage image, int thickness) {
		//Gets pixel writer from image
		PixelWriter writer = image.getPixelWriter();
		//Gets width and height of image
		int width = (int) image.getWidth(), height = (int) image.getHeight();
		
		//Iterates over all radii between radius - thickness and radius + thickness
		for(int rad = radius - thickness; rad <= radius + thickness; rad++) {
			//Iterates over every angle in circle
			for(int thetaDeg = 0; thetaDeg < 360; thetaDeg++) {
				//Converts angle to radians
				double thetaRad = Math.toRadians(thetaDeg);
				//Gets x and y position of point to draw
				int pX = (int) Math.round(x + (radius * Math.cos(thetaRad))), pY = (int) Math.round(y + (radius * Math.sin(thetaRad)));
			
				//Checks if both x and y valid
				if(pX >= 0 && pX < width && pY >= 0 && pY < height) {
					//Draws pixel if valid
					writer.setColor(pX, pY, DetectorController.lerp(Color.RED, Color.BLUE, certainty));
				}
			}
		}
	}
}