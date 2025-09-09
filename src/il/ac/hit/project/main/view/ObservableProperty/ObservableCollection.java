package il.ac.hit.project.main.view.ObservableProperty;

import java.util.ArrayList;
import java.util.List;


public class ObservableCollection<T> implements IObservableCollection<T> {

    private List<T> list =  new ArrayList<>();
    private final List<IPropertyObserver<List<T>>> listeners = new ArrayList<>();


    @Override
    public List<T> get() {
        return getList();
    }

    @Override
    public void setValue(List<T> value) {
        setList(value);
        notifyListeners();
    }

    @Override
    public void appendValue(T value) {
        getList().add(value);
        notifyListeners();
    }

    @Override
    public void removeValue(T value) {
        getList().remove(value);
        notifyListeners();
    }


    @Override
    public void addListener(IPropertyObserver<List<T>> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(IPropertyObserver<List<T>> listener) {
        listeners.remove(listener);
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }

    @Override
    public void notifyListeners() {
        for(IPropertyObserver<List<T>> listener : listeners){
            listener.update(getList());
        }
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}