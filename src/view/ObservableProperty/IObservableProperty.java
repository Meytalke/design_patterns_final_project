package view.ObservableProperty;

public interface IObservableProperty<T> {


    T get();
    void setValue(T value);
    void addListener(IPropertyObserver<T> listener);
    void removeListener(IPropertyObserver<T> listener);
    void clearListeners();
    void notifyListeners();
}
