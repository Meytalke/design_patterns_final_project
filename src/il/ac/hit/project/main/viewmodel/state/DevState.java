package il.ac.hit.project.main.viewmodel.state;

public class DevState implements IAppState {
    @Override
    public void logMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void logError(String message) {
        System.err.println(message);
    }
}
