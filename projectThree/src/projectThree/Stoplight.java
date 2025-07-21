package projectThree;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 * @author Joshua Leal
 * Project 3
 * Date: 03/04/2025
 * 
 * Represents a stoplight in the traffic simulation. This class controls the behavior 
 * of a traffic light, including the timing of the light cycle, pausing and resuming,
 * and the management of stopped cars when the light is red. Each stoplight has a 
 * timer length, number, and GUI shape.
 */

public class Stoplight extends Thread {

	private static int totalOfStoplights;
	private static int numberOfStoplights = 0;
	private static long yellowLightTimerLength = 1000; 
	private static volatile ConcurrentLinkedQueue<Car> stoppedCars = new ConcurrentLinkedQueue<Car>();
	
	private long timerLength;
	private int stoplightNumber;
	private VBox stoplightShape;
	private String color;
	private Label timeLabel;
	private boolean isPaused = false;

	
	
	public Stoplight(int amountOfStoplights) {
		timerLength = ((new Random()).nextInt(4) + 5) * 1000; //timer range: 5-8 seconds
		int randomColor = (new Random()).nextInt(2);
		if(randomColor == 0)
			color = "Red";
		else
			color = "Green";
		stoplightNumber = ++numberOfStoplights; 
		Stoplight.totalOfStoplights = amountOfStoplights;
		stoplightShape = createStoplightShape();
	}
	
	public static int getTotalOfStoplights() {
		return Stoplight.totalOfStoplights;
	}
	
	public static void resetStoplightNumber() {
		numberOfStoplights = 0;
	}
	
//	Returns the timer length for yellow stoplights in milliseconds.	
	public static long getYellowLightTimerLength() {
		return Stoplight.yellowLightTimerLength;
	}

	public static void addCar(Car car) {
		stoppedCars.add(car);
	}

//	Returns the timer length for the stoplight in milliseconds.
	public long getTimerLength() {
		return timerLength;
	}

	public int getStoplightNumber() {
		return stoplightNumber;
	}
	
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	public Label getTimeLabel() {
		return timeLabel;
	}
	
	public void setTimeLabel(Label label) {
		timeLabel = label;
	}
	
//	Creates the GUI version of the stoplight, including the color circle, time label, and structure line.
//  The stoplight's position is calculated based on the total number of stoplights in the simulation.
	public VBox createStoplightShape() {
		VBox stoplightShape = new VBox();
		Label timeLabel = new Label("" + timerLength);
		timeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 8");
		setTimeLabel(timeLabel);
		Circle circle = new Circle(5);
		Color fxColor;
		if(this.getColor().equals("Red"))
			fxColor = Color.RED;
		else if(this.getColor().equals("Yellow"))
			fxColor = Color.YELLOW;
		else
			fxColor = Color.GREEN;
		circle.setFill(fxColor);
		circle.setCenterX(50);
		circle.setCenterY(50);
		
		Line line = new Line(3, 5, 3, 24);
		line.setStrokeWidth(2);
		line.setStroke(fxColor);
		double spaceBetween = 500 / Stoplight.getTotalOfStoplights();
		int locationX = (int) (spaceBetween * getStoplightNumber()); 
		int locationY = 27;
		stoplightShape.getChildren().addAll(timeLabel, circle, line);
		stoplightShape.setAlignment(Pos.CENTER);
		stoplightShape.setLayoutX(locationX);
		stoplightShape.setLayoutY(locationY);
		return stoplightShape;
	}
	
	public VBox getStoplightShape() {
		return stoplightShape;
	}
	
	
//	Handles the timing for the light cycle (green, yellow, red), and updates the 
//	light's color and remaining time in a loop. It also manages pausing and resuming 
//	the stoplight and resuming the car stopped at its location.
	@Override
	public void run() { 
		long startTime = System.currentTimeMillis();
		long elapsedTime = 0;
		long pausedStartTime = 0;
		long pausedTime = 0;
		boolean isRunning = true;
		while(isRunning) {
			synchronized(this) {
				while(isPaused) {
					if(pausedStartTime == 0)
						pausedStartTime = System.currentTimeMillis();
					try {
						this.wait();
					} catch (InterruptedException e) {}
				}
			}

			elapsedTime = System.currentTimeMillis() - startTime;
			
			if(pausedStartTime != 0) {
				long pauseLength = System.currentTimeMillis() - pausedStartTime;
				pausedTime += pauseLength;
				pausedStartTime = 0;
			}			
			
			long remainingTime;
			if(getColor().equals("Green") || getColor().equals("Red"))
				remainingTime = getTimerLength() - elapsedTime + pausedTime;
			else
				remainingTime = getYellowLightTimerLength() - elapsedTime + pausedTime;			
			
			if(remainingTime <= 0) {
				if(getColor().equals("Green"))
					setColor("Yellow");
				else if(getColor().equals("Yellow"))
					setColor("Red");
				else {
					setColor("Green");
					for(Car car: stoppedCars) {
						car.setSpeed(50);
						car.play();
						stoppedCars.remove(car);
					}
				}
				startTime = System.currentTimeMillis();
				elapsedTime = 0;
				pausedTime = 0;
			}
			
			try {
				if(getTimeLabel().getText().equals("Completed"))
					throw new InterruptedException();
				Thread.sleep(10);
			} catch (InterruptedException e) {
				return;
			}

			// Updates GUI
			Platform.runLater(() -> {				
				changeStoplightColor();
				String time = String.format("%02d:%02d", 
						getSeconds(remainingTime), 
						getMilliseconds(remainingTime));
				getTimeLabel().setText(time);
			});
		}
	}

	public void changeStoplightColor() {
		VBox sl = getStoplightShape();
		Circle circle = (Circle) sl.getChildren().get(1);
		Line line = (Line) sl.getChildren().get(2);
		Color fxColor;
		if(getColor().equals("Red"))
			fxColor = Color.RED;
		else if(getColor().equals("Yellow"))
			fxColor = Color.YELLOW;
		else
			fxColor = Color.GREEN;
		circle.setFill(fxColor);
		line.setStroke(fxColor);
	}

	public synchronized void pause() {
		isPaused = true;
	}
	
	public synchronized void play() {
		isPaused = false;
		notify();
	}

	public int getSeconds(long millis) {
        return (int) TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
    }

    public int getMilliseconds(long millis) {
        return (int) (millis % 1000)/10; // Get remaining milliseconds
    }

}
