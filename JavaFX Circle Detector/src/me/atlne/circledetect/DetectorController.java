package me.atlne.circledetect;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class DetectorController {
	
	//Stores the sobel operator for the x and y directions
	public static final double[][] SOBEL_X= {
			{-1, 0, 1},
			{-2, 0, 2},
			{-1, 0, 1}},
								SOBEL_Y = {
			{-1, -2, -1},
			{0, 0, 0},
			{1, 2, 1}};
	//Stores edge threshold for circle detection and brightness threshold for circle
	public static final float EDGE_THRESHOLD = 0.05f, CIRCLE_THRESHOLD = 0.7f;
	//Stores image scale width and height
	public static final int SCALE_WIDTH = 256;
	//Stores min and max radii for circles to detect in px
	public static final int MIN_RADIUS = 10, MAX_RADIUS = 168;
	
	//Stores loaded image
	private Image image;
	//Stores image view for default image, grayscale image, Gaussian-blurred image, Sobel-applied image, circled image, and final result
	@FXML
	private ImageView defaultImage, grayscaleImage, sobelImage, circlesImage, resultImage;

	@FXML
	public void loadImage() throws InterruptedException {
		//Gets file from file chooser
		FileChooser chooser = new FileChooser();
		chooser.setSelectedExtensionFilter(new ExtensionFilter("Image files", "png", "jpg", "bmp", "gif"));
		chooser.setTitle("Select image file...");
		image = new Image(chooser.showOpenDialog(null).toURI().toString());
		//Stores image's width and height, and diagonal size
		int width = (int) image.getWidth(), height = (int) image.getHeight();
		
		//Sets default image view to show image
		defaultImage.setImage(image);
		//Gets pixel reader for image and creates writeable version of image
		PixelReader reader = image.getPixelReader();
		WritableImage editImage = new WritableImage(reader, (int) image.getWidth(), (int) image.getHeight());
		//Gets pixel writer from edit image to apply grayscale to image
		PixelWriter writer = editImage.getPixelWriter();
		
		//Iterates over all pixels in image and converts them to grayscale
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				writer.setColor(x, y, reader.getColor(x, y).grayscale());
			}
		}
		
		//Sets grayscale image view to new image
		grayscaleImage.setImage(editImage);
		//Creates new scaled down version of grayscale image
		WritableImage scaledImage = new WritableImage(editImage.getPixelReader(), width, height);
		//Creates a temporary image view for scaling
		ImageView temp = new ImageView(scaledImage);
		temp.setPreserveRatio(true);
		temp.setFitWidth(SCALE_WIDTH);
		
		scaledImage = temp.snapshot(null, null);
		//Calculates new height for image
		int newHeight = (int) scaledImage.getHeight();
		
		//Creates new arrays to store sobel operator results
		float[][] sobelResult = new float[SCALE_WIDTH][newHeight];
		//Creates new image for sobel-applied result
		WritableImage sobelResultImage = new WritableImage(SCALE_WIDTH, newHeight);
		
		//Iterates over every pixel in grayscale image to calculate sobel for x, y, and final result
		for(int x = 0; x < SCALE_WIDTH; x++) {
			for(int y = 0; y < newHeight; y++) {
				//G = sqrt(G_x^2 + G-y^2)
				sobelResult[x][y] = (float) geometricMean(applyKernel(scaledImage, SOBEL_X, x, y), applyKernel(scaledImage, SOBEL_Y, x, y));
			}
		}
		
		//Gets max gradient value in resulting sobel matrix
		float maxG = max2DArray(sobelResult);
		//Gets pixel writer for sobel image
		PixelWriter sobelWriter = sobelResultImage.getPixelWriter();
		
		//Iterates over each sobel result and adds pixel to result image
		for(int x = 0; x < SCALE_WIDTH; x++) {
			for(int y = 0; y < newHeight; y++) {
				//Pixel brightness determined by percentage of max gradient in image
				sobelResult[x][y] /= maxG;
				sobelWriter.setColor(x, y, new Color(sobelResult[x][y], sobelResult[x][y], sobelResult[x][y], 1));
			}
		}
		
		//Sets sobel image view to final sobel image
		sobelImage.setImage(sobelResultImage);
		//Creates circle detector image to detect circles from radius of 15 to 40 pixels
		WritableImage circleDetectImage = new WritableImage(SCALE_WIDTH, newHeight);
		//Stores thread-safe list of all detected circles
		Vector<DetectedCircle> detectedCircles = new Vector<>();
		//Creates service to manage threads for each radius
		CountDownLatch threadCounter = new CountDownLatch(MAX_RADIUS - MIN_RADIUS);
		
		//Runs circle writer on another thread
		//Iterates over every radius of circles to find
		for(int rad = MIN_RADIUS; rad <= MAX_RADIUS; rad++) {
			//Stores final version of radius to use within thread
			final int RAD = rad;
			
			//Creates new thread to do circle detection for radius on
			new Thread(() -> {
				//Calls JavaFX to do thread-unsafe operation
				Platform.runLater(() -> {
					//Iterates over every pixel in sobel image
					for(int x = 0; x < SCALE_WIDTH; x++) {
						//Gets circle centre on x
						int centreX = x + RAD;
					
						//If circle centre on x invalid, skips to next x value
						if(centreX < 0 || centreX >= SCALE_WIDTH) {
							continue;
						}
					
						for(int y = 0; y < newHeight; y++) {
							//Gets circle centre on y
							int centreY = y + RAD;
						
							//If circle centre on y invalid, skips to next y value
							if(centreY < 0 || centreY >= newHeight) {
								continue;
							}
							
							//CIRCLE DETECTION
							//Stores number of pixels checked
							int pixChecked = 0;
							//Stores total pixel brightness of pixels
							float brightness = 0;
							
							//Iterates over every 8 degrees in circle
							for(int thetaDeg = 0; thetaDeg < 360; thetaDeg += 8) {
								//Converts angle to RADians
								double thetaRad = Math.toRadians(thetaDeg);
								//Gets x and y position of point to draw
								int pX = (int) Math.round(x + (RAD * Math.cos(thetaRad))), pY = (int) Math.round(y + (RAD * Math.sin(thetaRad)));
								
								//Checks if both x and y valid
								if(pX >= 0 && pX < SCALE_WIDTH && pY >= 0 && pY < newHeight) {
									//Checks if pixel is valid edge (over threshold)
									if(sobelResult[pX][pY] >= EDGE_THRESHOLD) {
										//Adds pixel to total brightness
										brightness++;
									}
									
									//Adds pixel to number checked
									pixChecked++;
								}
							}
							
							//Calculates average brightness of pixels in circle
							brightness /= pixChecked;
							
							//If brightness over threshold, adds to detected circle list
							if(brightness >= CIRCLE_THRESHOLD) {
								detectedCircles.add(new DetectedCircle(x, y, RAD, brightness));
								//Adds radius to y to avoid re-detecting same circle
								y += RAD;
							}
						}
					}
					
					System.out.println(RAD);
					//Declares thread complete
					threadCounter.countDown();
				});
			}).start();
		}
		
		//Runs after circle detection complete
		Platform.runLater(() -> {
			System.out.println("Drawing circles");
			//Iterates over every circle detected and draws to circle detect image with 5 pixels thickness
			for(DetectedCircle c : detectedCircles) {
				c.draw(circleDetectImage, 5);
			}
			
			//Sets circle detection image view to circle image created
			circlesImage.setImage(circleDetectImage);
			
			//Creates final image
			WritableImage finalImage = new WritableImage(image.getPixelReader(), width, height);
			//Gets pixel writer for final image
			PixelWriter finalImageWriter = finalImage.getPixelWriter();
			//Creates scaled circle image
			WritableImage scaledCircleImage;
			//Sets temp image view boundaries
			temp.setFitWidth(width);
			temp.setImage(circleDetectImage);
			scaledCircleImage = temp.snapshot(null, null);
			
			//Gets pixel reader for scaled circle image
			PixelReader scaledCircleReader = scaledCircleImage.getPixelReader();
			
			//Iterates over every pixel in scaled circle image and adds to final image
			for(int x = 0; x < width; x++) {
				for(int y = 0; y < height; y++) {
					if(!scaledCircleReader.getColor(x, y).equals(Color.WHITE)) {
						finalImageWriter.setColor(x, y, scaledCircleReader.getColor(x, y));
					} else {
						finalImageWriter.setColor(x, y, reader.getColor(x, y));
					}
				}
			}
			
			resultImage.setImage(finalImage);
		});
	}
	
	//Applies a kernel convolution to pixel given from image
	public double applyKernel(Image image, double[][] kernel, int x, int y) {
		//Gets image's pixel reader
		PixelReader reader = image.getPixelReader();
		//Gets image width and height
		int width = (int) image.getWidth(), height = (int) image.getHeight();
		//Stores total brightness of pixels
		double brightness = 0;
		//Stores number of valid pixels found in iterations
		int n = 0;
		
		//Gets all pixels within kernel range of image by iteration
		//Iterates over each index of kernel values
		for(int kX = 0; kX < kernel.length; kX++) {
			//Gets currently convoluted over pixel's x position
			int currentX = (x - (kernel.length / 2)) + kX;
			
			//Checks if x value isn't valid
			if(currentX < 0 || currentX >= width) {
				//Continues to next x value
				continue;
			}
			
			for(int kY = 0; kY < kernel[kX].length; kY++) {
				//Gets pixel to convolute over's y position
				int currentY = (y - (kernel.length / 2)) + kY;
				
				//Checks if current y value isn't valid
				if(currentY < 0 || currentY >= height) {
					//Continues to next y value
					continue;
				}
				
				//At this point, both x and y values should be valid so adds brightness to total
				brightness += reader.getColor(currentX, currentY).getBrightness() * kernel[kX][kY];
				n++;
			}
		}
		
		//Averages brightness values by number of pixels checked
		//Returns final kernel value
		return brightness / n;
	}
	
	//Finds max value in 2D array
	public float max2DArray(float[][] a) {
		//Stores max value
		float max = 0;
		
		//Iterates over all elements of 2D array
		for(float[] b : a) {
			for(float c : b) {
				if(c > max) {
					max = c;
				}
			}
		}
		
		//Returns max found
		return max;
	}
	
	//Finds geometric mean of two values
	public double geometricMean(double d, double e) {
		return Math.sqrt(Math.pow(d, 2) + Math.pow(e, 2));
	}
	
	//Linearly interpolates between two colours by given decimal amount
	public static Color lerp(Color start, Color end, float amount) {
		return new Color(start.getRed() + (amount * (end.getRed() - start.getRed())),
				start.getGreen() + (amount * (end.getGreen() - start.getGreen())),
				start.getBlue() + (amount * (end.getBlue() - start.getBlue())),
				start.getOpacity() + (amount * (end.getOpacity() - start.getOpacity())));
	}
}