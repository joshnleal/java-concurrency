package projectThree;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Joshua Leal
 * Project 3
 * Date: 03/04/2025
 *
 * This class provides the main functionality for simulating cars and stoplights
 * within a GUI built using JavaFX. It allows users to start and pause the 
 * simulation, configure the number of cars and stoplights, and control the 
 * simulation's execution. The program handles the logic of car movement, stoplight 
 * changes, and time tracking.
 * 
 * Features:
 * - Start/stop the simulation
 * - Pause/continue the simulation
 * - Set the number of cars and stoplights dynamically
 * - Visualize the simulation with a simple graphical representation
 */

public class Main extends Application{
	
	private static ArrayList<Stoplight> stoplights = new ArrayList<Stoplight>();
	private static ArrayList<Double> stoplightLocations = new ArrayList<Double>();
	private static ArrayList<Car> cars = new ArrayList<Car>();
	private static StopWatch sw;
	
	public static VBox layoutPane = new VBox(10);
	public static Pane viewport = new Pane();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Traffic Simulation");
        Scene scene = new Scene(getPane(primaryStage), 700, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
		
	}	

//	Creates the primary user interface with buttons for controlling
//	the simulation, and displays the viewport for visualizing the simulation.
	public static Pane getPane(Stage window) {
		layoutPane.setPadding(new Insets(0, 20, 0, 20));
		viewport.setMinHeight(120);
		viewport.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-background-color: black;");
		HBox buttonLayout = new HBox(5);
		Button startStopButton = new Button("Start");
		Button pauseContinueButton = new Button("Pause");
		buttonLayout.getChildren().addAll(startStopButton, pauseContinueButton);
		buttonLayout.setAlignment(Pos.CENTER);
		layoutPane.getChildren().addAll(viewport, buttonLayout);
		layoutPane.setAlignment(Pos.CENTER);
		
		startStopButton.setOnAction(e -> {
			if(startStopButton.getText().equals("Start")) {
				startStopButton.setText("Stop");
				getNumbersWindow();
				sw = new StopWatch();
				layoutPane.getChildren().add(0, sw.getTimeLabel());
				setUpViewportSimulation(viewport);
			} else {
				startStopButton.setText("Start");
				terminateSimulation();
			}
		});
		
		pauseContinueButton.setOnAction(e -> {
			if(pauseContinueButton.getText().equals("Pause") && startStopButton.getText().equals("Stop")) {
				pauseContinueButton.setText("Continue");
				sw.pause();
				pauseCars();
				pauseStoplights();
			} else if(startStopButton.getText().equals("Start")){
				popUpWarningWindow("Simulation must be running first");
			} else if(pauseContinueButton.getText().equals("Continue")){
				pauseContinueButton.setText("Pause");
				sw.play();
				playCars();
				playStoplights();
			}
		});
		
		window.setOnCloseRequest(e -> {
			if(startStopButton.getText().equals("Stop")) {
				terminateSimulation();
			}
		});
		
		return layoutPane;
	}
	
//	Displays a pop-up window for setting the number of cars and stoplights in the simulation.
//	Allows the user to select the quantity of cars and stoplights for the simulation.
	public static void getNumbersWindow() {
		Stage popUpStage = new Stage();
		popUpStage.setTitle("Set the Simulation");
		
		VBox popUpBox = new VBox();
		VBox popUpLayout = new VBox(10);	
		Label carLabel = new Label("Number of cars");
		ChoiceBox<Integer> numberOfCarsChoiceBox = new ChoiceBox<Integer>();
		numberOfCarsChoiceBox.getItems().addAll(1, 2, 3, 4, 5);
		numberOfCarsChoiceBox.setValue(1);
		numberOfCarsChoiceBox.setTooltip(new Tooltip("Select a number of cars"));
		numberOfCarsChoiceBox.setMinWidth(110);
		Label stoplightLabel = new Label("Number of stoplights");
		ChoiceBox<Integer> numberOfStoplightsChoiceBox = new ChoiceBox<Integer>();
		numberOfStoplightsChoiceBox.getItems().addAll(1, 2, 3, 4, 5);
		numberOfStoplightsChoiceBox.setValue(1);
		numberOfStoplightsChoiceBox.setTooltip(new Tooltip("Select a number of stoplights"));
		numberOfStoplightsChoiceBox.setMinWidth(110);
		Button okButton = new Button("Ok");
		okButton.setMinWidth(110);
		popUpLayout.getChildren().addAll(carLabel, numberOfCarsChoiceBox, stoplightLabel, numberOfStoplightsChoiceBox, okButton);
		popUpLayout.setPadding(new Insets(0, 0, 0, 20));
		popUpBox.getChildren().add(popUpLayout);
		
		okButton.setOnAction(e -> {
			int i;
			for(i = 1; i <= numberOfCarsChoiceBox.getValue(); i++) 
				 cars.add(new Car());
			
			int numberOfStoplights = numberOfStoplightsChoiceBox.getValue();
			for(i = 1; i <= numberOfStoplights; i++) 
				stoplights.add(new Stoplight(numberOfStoplights));
			popUpStage.close();
		});
		
		// Opens pop up windows with an error if user tries to close the pop up window
		popUpStage.setOnCloseRequest(e -> {
			e.consume();
			popUpWarningWindow("Select Your Choices and Click Ok");
		});
		
		popUpBox.setAlignment(Pos.CENTER);
		Scene popUpScene = new Scene(popUpBox, 150, 200);
		popUpStage.initStyle(StageStyle.UTILITY);
		popUpStage.setResizable(false); 
		popUpStage.setScene(popUpScene);
		
		// Pauses the primary stage thread
	    popUpStage.initModality(Modality.APPLICATION_MODAL); 

	    // Show the pop up window and wait for the user to interact with it.
	    // Primary stage thread continues once pop up window closes
	    popUpStage.showAndWait();  
				
	}
	
	public static void popUpWarningWindow(String message) {
		Stage popUpStage = new Stage();
		popUpStage.setTitle("Error");
		
		VBox buttonBox =  new VBox(10);	
		Text warningMessage = new Text(message);
		Button okButton = new Button("Ok");
		buttonBox.getChildren().addAll(warningMessage, okButton);
		
		okButton.setOnAction(e -> 
			popUpStage.close()
		);
		
		buttonBox.setAlignment(Pos.CENTER);
		Scene popUpScene = new Scene(buttonBox, 250, 75);
		popUpStage.initStyle(StageStyle.UTILITY);
		popUpStage.setResizable(true); // Change to false
		popUpStage.setScene(popUpScene);
		
		// Pauses the primary stage thread
	    popUpStage.initModality(Modality.APPLICATION_MODAL); 

	    // Show the pop up window and wait for the user to interact with it.
	    // Primary stage thread continues once pop up window closes
	    popUpStage.showAndWait();
		
	}
	
//	Initializes the simulation's viewport, placing stoplights, cars, and distance label
//	within the pane and starting their respective threads.
	public static void setUpViewportSimulation(Pane viewport) {
		Distance totalViewportDistance = new Distance(Stoplight.getTotalOfStoplights());
		viewport.getChildren().add(totalViewportDistance.createDistanceLabel());
		
		for(Stoplight stoplight: stoplights) {
			stoplightLocations.add(stoplight.getStoplightShape().getLayoutX());
			viewport.getChildren().add(stoplight.getStoplightShape());
			stoplight.start();
		}
		Car.setStoplights(stoplights);
		
		for(Car car: cars) {
			viewport.getChildren().add(car.getCarShape());
			car.start();
		}		
		Main.sw.start();
	}
	
	public static void pauseCars() {
		for(Car car: cars) {
			car.pause();
		}
	}
	
	public static void playCars() {
		for(Car car: cars) {
			car.play();
		}
	}
	
	public static void pauseStoplights() {
		for(Stoplight stoplight: stoplights) {
			stoplight.pause();
		}
	}
	
	public static void playStoplights() {
		for(Stoplight stoplight: stoplights) {
			stoplight.play();
		}
	}
	
	public static void terminateCars() {
		for(Car car: cars) {
			car.interrupt();
		}
	}
	
	public static void terminateStoplights() {
		for(Stoplight stoplight: stoplights) {
			stoplight.interrupt();
		}
	}
	
	public static void terminateSimulation() {
		sw.interrupt();
		layoutPane.getChildren().remove(0);
		viewport.getChildren().clear();
		terminateStoplights();
		terminateCars();
		stoplights.clear();
		cars.clear();
		Car.resetNumberOfCars();
		Stoplight.resetStoplightNumber();
	}
}
