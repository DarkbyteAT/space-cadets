package me.atlne.circledetect;

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
	public static final int[][] SOBEL_X= {
			{-1, 0, 1},
			{-2, 0, 2},
			{-1, 0, 1}},
								SOBEL_Y = {
			{-1, -2, -1},
			{0, 0, 0},
			{1, 2, 1}};
	//Stores vote threshold for circle detection
	public static final float VOTE_THRESHOLD = 0.15f;
	//Stores image scale width and height
	public static final int SCALE_WIDTH = 256;
	
	//Stores loaded image
	private Image image;
	//Stores image view for default image, grayscale image, Sobel-applied image, circled image, and final result
	@FXML
	private ImageView defaultImage, grayscaleImage, sobelImage, circlesImage, resultImage;

	@FXML
	public void loadImage() {
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
		Image scaledImage = new WritableImage(editImage.getPixelReader(), width, height);
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
				sobelResult[x][y] = geometricMean(applyKernel(scaledImage, SOBEL_X, x, y), applyKernel(scaledImage, SOBEL_Y, x, y));
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
		//Gets pixel writer for circle image
		PixelWriter circleWriter = circleDetectImage.getPixelWriter();
		
		//Runs circle writer on another thread
		Platform.runLater(() -> {
			//Iterates over every radius of circles to find
			for(int rad = 20; rad <= 60; rad++) {
				//Iterates over every pixel in sobel image
				for(int x = 0; x < SCALE_WIDTH; x++) {
					//Gets circle centre on x
					int centreX = x + rad;
				
					//If circle centre on x invalid, skips to next x value
					if(centreX < 0 || centreX >= SCALE_WIDTH) {
						continue;
					}
				
					for(int y = 0; y < newHeight; y++) {
						//Gets circle centre on y
						int centreY = y + rad;
					
						//If circle centre on y invalid, skips to next y value
						if(centreY < 0 || centreY >= newHeight) {
							continue;
						}
					
						//Assuming x and y values valid, checks for circle of test radius around point
						//Uses votes to count brightness of pixels in circle around point
						float votes = 0;
						//Stores number of iterations performed
						int i = 0;
					
						//Iterates over each point in circle by angle per 8 degree increment
						for(double theta = 0; theta < Math.PI * 2; theta += Math.PI / 22.5) {
							//Gets current point on circle
							int currentX = (int) Math.round(centreX + rad * Math.cos(theta)), currentY = (int) Math.round(centreY + rad * Math.sin(theta));
						
							//Checks if point is valid
							if(!(currentX < 0 || currentX >= SCALE_WIDTH) && !(currentY < 0 || currentY >= newHeight)) {
								//Adds vote from point
								votes += sobelResult[currentX][currentY];
								i++;
							}
						}
					
						//Divides votes by number of test points iterated over
						votes /= i;
					
						//If votes > threshold, draws circle
						if(votes > VOTE_THRESHOLD) {
							System.out.format("%d, %d, %d%n", centreX, centreY, rad);
							
							//Iterates over each point in circle by angle per degree increment
							for(double theta = 0; theta < Math.PI * 2; theta += Math.PI / 180) {
								//Gets current point on circle
								int currentX = (int) Math.round(centreX + rad * Math.cos(theta)), currentY = (int) Math.round(centreY + rad * Math.sin(theta));
								//Checks if point is valid
								if(!(currentX < 0 || currentX >= SCALE_WIDTH) && !(currentY < 0 || currentY >= newHeight)) {
									//Draws pixel at point
									circleWriter.setColor(currentX, currentY, new Color(votes, votes, votes, 1));
								}
							}
						}
					}
				}
			}
		
			//Sets circle detection image view to circle image created
			circlesImage.setImage(circleDetectImage);
		});
	}
	
	//Applies a kernel convolution to pixel given from image
	public float applyKernel(Image image, int[][] kernel, int x, int y) {
		//Gets image's pixel reader
		PixelReader reader = image.getPixelReader();
		//Gets image width and height
		int width = (int) image.getWidth(), height = (int) image.getHeight();
		//Stores total brightness of pixels
		float brightness = 0;
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
	public float geometricMean(float a, float b) {
		return (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
	}
}