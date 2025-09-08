package view.ObservableProperty;

/*
 * IPropertyObserver is a functional interface.
 * It's purpose is to allow you to instanciate an anonymous class object that implements
 * this interface. Therefore we can override update to send a lambda function to update
 * The UI component we want to target.*/
public interface IPropertyObserver <T> {
    void update(T value);
}