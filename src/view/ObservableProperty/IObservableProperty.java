package view.ObservableProperty;

public interface IObservableProperty<T> {
    /*
    Where:
        T= the property type (String|boolean|int)
        U= the UI component type (Swing.something).

    * */
    public void updateUI();
    public void changeValue(T value);

}
