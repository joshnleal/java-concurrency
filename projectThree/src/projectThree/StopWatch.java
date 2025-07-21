package projectThree;

import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * @author Joshua Leal
 * Project 3
 * Date: 03/04/2025
 * 
 * Represents a stopwatch in the simulation. 
 * The stopwatch keeps track of elapsed time in minutes, seconds, and milliseconds.
 * It can be paused and resumed, and it updates a GUI label to display the current time.
 */

public class StopWatch extends Thread {
	private long elapsedTime = 0;
	private long pausedTime = 0;
	private long pausedStartTime = 0;
	private Label timeLabel;
	private boolean isPaused = false;
	
	public StopWatch() {
		timeLabel = new Label(" seconds");
	}
	
	public synchronized void pause() {
		isPaused = true;
		setPausedStartTime(System.currentTimeMillis());
	}
	
	public synchronized void play() {
		isPaused = false;
		setPausedTime(System.currentTimeMillis() - getPausedStartTime());
		notify();
	}
	
	public Label getTimeLabel() {
		return timeLabel;
	}
	
	public long getElapsedTime() {
		return elapsedTime;
	}
	
	public void setElapsedTime(long time) {
		elapsedTime = time;
	}
	
	public long getPausedStartTime() {
		return pausedStartTime;
	}
	
	public void setPausedStartTime(long millis) {
		pausedStartTime = millis;
	}
	
	public long getPausedTime() {
		return pausedTime;
	}
	
	public void setPausedTime(long millis) {
		pausedTime = millis;
	}

//	Continuously updates the elapsed time and displays it in the GUI in a "MM:SS:MS" format.
//  The stopwatch can be paused and resumed during execution.
	@Override
	public void run() { 
		long startTime = System.currentTimeMillis();
		boolean isRunning = true;
		long pausedTime = 0;
		while(isRunning) {
			synchronized(this) {
				while(isPaused) {
					try {
						this.wait();
					} catch (InterruptedException e) {}
				}
			}
			
			if(getPausedTime() != 0) {
				long pauseLength = getPausedTime();
				pausedTime += pauseLength;
			}
			setElapsedTime(System.currentTimeMillis() - startTime - pausedTime);
			
			setPausedTime(0);
			
			try {
				if(getTimeLabel().getText().equals("Completed"))
					throw new InterruptedException();
				Thread.sleep(10);
			} catch (InterruptedException e) {
				return;
			}

			// Updates GUI
			Platform.runLater(() -> {			
				getTimeLabel().setText(String.format("%02d:%02d:%02d seconds", 
						getMinutes(getElapsedTime()),
						getSeconds(getElapsedTime()), 
                        getMilliseconds(getElapsedTime())));
			});
		}
	}
	
	public int getMinutes(long millis) {
        return (int) TimeUnit.MILLISECONDS.toMinutes(millis);
    }

    public int getSeconds(long millis) {
        return (int) TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
    }

    public int getMilliseconds(long millis) {
        return (int) (millis % 1000)/10; 
    }
}
