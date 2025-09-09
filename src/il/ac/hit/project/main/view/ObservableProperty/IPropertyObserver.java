package il.ac.hit.project.main.view.ObservableProperty;

/*
 * IPropertyObserver is a functional interface.
 * Its purpose is to allow you to instantiate an anonymous class object that implements
 * this interface. Therefore, we can override update to send a lambda function to update
 * The UI component we want to target.*/
public interface IPropertyObserver <T> {
    void update(T value);
}