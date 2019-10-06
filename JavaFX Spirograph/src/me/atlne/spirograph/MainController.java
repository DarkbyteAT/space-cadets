package me.atlne.spirograph;

import java.util.Vector;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class MainController {
	
	//Stores instance of canvas to draw on
	@FXML
	private Canvas canvas;
	//Stores sliders on screen
	@FXML
	private Slider R, r, O, dt;
	//Stores colour pickers on screen
	@FXML
	private ColorPicker startColour, endColour;
	
	//Sets colours to random values at start of program
	@FXML
	public void initialize() {
		startColour.setValue(new Color(Math.random(), Math.random(), Math.random(), 1));
		endColour.setValue(new Color(Math.random(), Math.random(), Math.random(), 1));
		//Draws hypocycloid curve
		draw();
	}
	
	//Draws hypocycloid to canvas
	@FXML
	public void draw() {
		//Gets graphics object to draw pixels
		GraphicsContext g = canvas.getGraphicsContext2D();
		//Clears canvas
		g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		//Gets pixel writer for canvas from graphics object
		PixelWriter p = g.getPixelWriter();
		//Creates hypocycloid curve
		Hypocycloid curve = new Hypocycloid(R.getValue(), r.getValue(), O.getValue(), 1f / dt.getValue());
		//Stores points on curve using max period of the curves
		Vector<double[]> points = curve.calculatePoints(0, 128 * Math.PI);
		//Stores start and end colours for curve
		Color start = startColour.getValue(), end = endColour.getValue();
		
		//Iterates over points on hypocycloid for one cycle (max_t = 2pi * (r / R-r))
		for(int i = 0; i < points.size(); i++) {
			//Draws point offset from centre of canvas
			p.setColor((int) ((canvas.getWidth() / 2) + Math.round(points.get(i)[0])),
					(int) ((canvas.getHeight() / 2) + Math.round(points.get(i)[1])),
					lerp(start, end, (float) i / (float) points.size()));
		}
		
		//Finalises drawing to canvas
		g.stroke();
	}
	
	//Linearly interpolates between two colours by given decimal amount
	public Color lerp(Color start, Color end, float amount) {
		return new Color(start.getRed() + (amount * (end.getRed() - start.getRed())),
				start.getGreen() + (amount * (end.getGreen() - start.getGreen())),
				start.getBlue() + (amount * (end.getBlue() - start.getBlue())),
				start.getOpacity() + (amount * (end.getOpacity() - start.getOpacity())));
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
}