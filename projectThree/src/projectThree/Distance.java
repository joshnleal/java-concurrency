package projectThree;

import javafx.scene.control.Label;

/**
 * @author Joshua Leal
 * Project 3
 * Date: 03/04/2025
 * 
 * Represents the total distance of the simulation based on the number of stoplights.
 * The distance is calculated based on a predefined distance between stoplights.
 * This class also provides a method to create a label displaying the total distance.
 */

public class Distance {
	private final static int distanceBetweenStoplights = 1000;
	private static int totalDistance;
	
	public Distance(int numberOfStoplights) {
		totalDistance = distanceBetweenStoplights * numberOfStoplights + 1000;
	}
	
	public Label createDistanceLabel() {
		Label distanceLabel = new Label("Total Distance: " + getTotalDistance() + " km");
		distanceLabel.setStyle("-fx-text-fill: white");
		distanceLabel.setLayoutX(5);
		distanceLabel.setLayoutY(5);
		return distanceLabel;
	}
	
	public static int getTotalDistance() {
		return Distance.totalDistance;
	}
}
