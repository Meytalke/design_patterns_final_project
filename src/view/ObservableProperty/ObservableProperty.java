package view.ObservableProperty;



public class ObservableProperty<T,U> implements IObservableProperty<T> {

    private T value;
    private U component;

    public ObservableProperty(T value, U component) {
        /*
        * Initializing observableProperty object with the value to store, and the UI component to */
        setValue(value);
        setComponent(component);
    }

    @Override
    public void updateUI() {
    //Update the UI component with the new value T

    }

    @Override
    public void changeValue(T value) {
        setValue(value);
    }


    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public U getComponent() {
        return component;
    }

    public void setComponent(U component) {
        this.component = component;
    }
}
