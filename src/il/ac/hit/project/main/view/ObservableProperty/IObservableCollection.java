package il.ac.hit.project.main.view.ObservableProperty;

import java.util.List;

public interface IObservableCollection <T> extends IObservableProperty<List<T>> {
    @Override
    List<T> get();
    @Override
    void setValue(List<T> value);
    void appendValue(T value);
    void removeValue(T value);

}