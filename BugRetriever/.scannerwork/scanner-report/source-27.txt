package controller;

import org.eclipse.jgit.lib.ProgressMonitor;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleProgressMonitor implements ProgressMonitor {

    private static final Logger LOGGER = Logger.getLogger( SimpleProgressMonitor.class.getName() );

    @Override
    public void start(int totalTasks) {LOGGER.log(Level.INFO, () -> "Starting work on " + totalTasks + " tasks");}

    @Override
    public void beginTask(String title, int totalWork) {LOGGER.log(Level.INFO, () -> "Start " + title + ": " + totalWork);}

    @Override
    public void update(int completed) {LOGGER.log(Level.INFO, () -> completed + "-");}

    @Override
    public void endTask() {LOGGER.log(Level.INFO, ("Done"));}

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void showDuration(boolean b) {
        // Do nothing
    }
}
