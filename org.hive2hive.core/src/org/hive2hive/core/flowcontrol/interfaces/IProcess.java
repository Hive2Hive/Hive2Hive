package org.hive2hive.core.flowcontrol.interfaces;

/** This interface provides the default methods of a process which represents a use case.
 * 
 * @author Christian
 *
 */
public interface IProcess {

	public void initialize();
	
	public void start();
	
	public void pause();
	
	public void stop();
	
	public void process();
	
	public int getProgress();
	
	public IProcess getNextProcess();
	
	public IProcess getPreviousProcess();
	
}
