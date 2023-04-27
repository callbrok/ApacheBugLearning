package controller;

import org.eclipse.jgit.lib.ProgressMonitor;

public class SimpleProgressMonitor implements ProgressMonitor {
    @Override
    public void start(int totalTasks) {
        System.err.println("Starting work on " + totalTasks + " tasks");
    }

    @Override
    public void beginTask(String title, int totalWork) {
        System.err.println("Start " + title + ": " + totalWork);
    }

    @Override
    public void update(int completed) {
        System.err.print(completed + "-");
    }

    @Override
    public void endTask() {
        System.err.println("Done");
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void showDuration(boolean b) {

    }
}
