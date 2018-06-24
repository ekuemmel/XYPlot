package de.ewmksoft.xyplot.demo;

/**
 * A timer class having a thread which wakes up periodically. 
 * The client of an instance of this class must have 
 * @see ITimeTicker implemented and will be informed by a callback
 * each @see delay milliseconds.
 * 
 * @author Eberhard Kuemmel
 */

public class TimeTicker implements Runnable {

	/**
	 * 
	 * @param display The display class
	 * @param client  The receiver of the periodic calls
	 * @param delay   Delay between calls in [ms]
	 */
	TimeTicker(ITimeTicker client, int delay) {
		this.client = client;
		this.delay = delay;
	}
	
	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public void run() {
		while (!stop)
		{
			try {
				Thread.sleep(delay);
				if (enabled) client.onTimer();
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void shutdown() {
		stop = true;
	}
	
	private ITimeTicker client;
	private int delay;
	private boolean enabled = true;
	private boolean stop = false;

}
