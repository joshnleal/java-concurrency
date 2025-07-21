package projectThree;

import java.util.ArrayList;
import java.util.Random;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * @author Joshua Leal
 * Project 3
 * Date: 03/04/2025
 * 
 * Represents a car in the traffic simulation.
 * The car moves across the screen, interacting with stoplights,
 * and can pause or resume movement. Each car has a speed, current position,
 * and GUI shape.
 */

public class Car extends Thread {

	private static int numberOfCars = 0;
	private static ArrayList<Stoplight> stoplights;
	
	final private int carNumber;
	private int speed;
	private VBox carShape;
	public int currentPosition = 0;
	private boolean isPaused = false;
	
	
	public Car() {
		carNumber = numberOfCars++;
		speed = 50; // km/h
		carShape = createCarShape();
	}
	
//	Creates the GUI version of the car. A StackPane with a rectangle for the car and 
//	a label showing the car's number, speed, and position. The car's initial position
//	on the screen is randomly determined.
	public VBox createCarShape() {
		VBox carLayout = new VBox(getCarNumber() * 10); 
		StackPane carShape = new StackPane();
		int locationX = (new Random()).nextInt(650); 
		int locationY = 50;
		Rectangle square = new Rectangle(20, 20, Color.ALICEBLUE);
		Text carNumber = new Text("" + (this.getCarNumber() + 1));
		carShape.getChildren().addAll(square, carNumber);
		Label carInfoLabel = new Label((this.getCarNumber() + 1) + ": " + getSpeed() + " km/h, " + currentPosition + "km");
		carInfoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10");
		carLayout.getChildren().addAll(carShape, carInfoLabel);
		carLayout.setLayoutX(locationX);
		carLayout.setLayoutY(locationY);
		return carLayout;
	}

	public int getCarNumber() {
		return carNumber;
	}

	public int getSpeed() {
		return speed;
	}
	
	public void setSpeed(int newSpeed) {
		speed = newSpeed;
	}
	
	public VBox getCarShape() {
		return carShape;
	}
	
//	Sets the list of stoplights in the simulation.
//  This is necessary for the car to interact with the stoplights.
	public static void setStoplights(ArrayList<Stoplight> stoplights) {
		Car.stoplights = stoplights;
	}
	
	public static void resetNumberOfCars() {
		numberOfCars = 0;
	}
	
	public synchronized void pause() {
		isPaused = true;
	}
	
	public synchronized void play() {
		if(this.getSpeed() != 0) {
			isPaused = false;
			notify();
		}
	}

//	The car keeps moving forward while interacting with stoplights.
//  It checks the car's position against stoplights and stops if a red light is encountered.
//  It also updates the car's position and speed in the graphical interface.
	@Override
	public void run() {
		boolean isRunning = true;
		while(isRunning) {
			synchronized(this) {
				while(isPaused) {
					try {
						this.wait();
					} catch (InterruptedException e) {}
				}
			}
			
            int viewportWidth = 660;
            
            double pixelsToMeters = (double) Distance.getTotalDistance()/viewportWidth;
			
            // Update real-world distance (meters)
            currentPosition = (int) (getCarShape().getLayoutX() * pixelsToMeters); // Convert km/h to m/ms
            if(getCarShape().getLayoutX() > 610) 
				getCarShape().setLayoutX(carShape.getLayoutX() - 650);
            currentPosition += (50 * pixelsToMeters); // Buffer for smoother position display
			for(Stoplight stoplight: stoplights) {
				double carXPosition = getCarShape().getLayoutX();
				double stoplightXPosition = stoplight.getStoplightShape().getLayoutX() - 40;
				double range = 10;
				if(Math.abs(carXPosition - stoplightXPosition) <= range) {
					if(stoplight.getColor().equals("Red")) {
						if(carXPosition < stoplightXPosition) {
							setSpeed(0);
							Stoplight.addCar(this);
							pause();
						} else {
							setSpeed(50);
						}
					} 
				}
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}

			// Updates GUI
			Platform.runLater(() -> {
				getCarShape().setLayoutX(getCarShape().getLayoutX() + 2);	
				Label carLabel = (Label) getCarShape().getChildren().get(1);
				carLabel.setText((getCarNumber() + 1) + ": " + getSpeed() + " km/h, " + currentPosition + "km");
			});
		}
	}

}
