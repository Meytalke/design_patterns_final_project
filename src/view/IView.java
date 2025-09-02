package view;
import viewmodel.IViewModel;

public interface IView {

     IViewModel getViewModel();
     void setViewModel(IViewModel viewModel);
     void start();
}


