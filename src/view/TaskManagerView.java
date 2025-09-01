package view;

import model.task.ITask;
import model.task.TaskState;
import viewmodel.IViewModel;
import viewmodel.TasksViewModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;

public class TaskManagerView extends JPanel implements TasksObserver, IView {

    private final JFrame window;
    private final JPanel contentPane;
    //Task title and description input field and textArea
    private final JTextField taskTitleInputF;
    private JTextArea descriptionInputTA;
    //Control buttons - addTask, updateTask, deleteTask, deleteAllTasks, report?
    private JButton addButton, updateButton, deleteButton, deleteAllButton,reportButton;

    //Task state in string value, convert to IState to control more accurate filtering
    private final JComboBox<String> stateFilterComboBox;
    private final JComboBox<String> exportFormatComboBox;

    // Task State in string value, convert to IState to control action behavior on each task
    // Or just make a visual difference depending on the state (color).
    private JComboBox<TaskState> taskStateComboBox;
    private ITask selectedTask = null;

    //Task list in memory and a listModel list to store the tasks visually
    private JList<ITask> taskList;
    private DefaultListModel<ITask> listModel;

    //Search specific regex fields
    private JTextField searchTitleInput;
    private JTextField searchDescriptionInput;
    private JTextField searchIdInput;

    //Interface over class
    private IViewModel viewModel;

    public TaskManagerView() {

        //Create UI base components
        contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //Setting up add task area
        taskTitleInputF = new JTextField(20);
        descriptionInputTA = new JTextArea(3, 20);

        //Setting up search area
        searchTitleInput = new JTextField(15);
        searchDescriptionInput = new JTextField(15);
        searchIdInput = new JTextField(5);

        //Setting up control buttons
        addButton = new JButton("Add Task");
        updateButton = new JButton("Update Task");
        deleteButton = new JButton("Delete Selected");
        deleteAllButton = new JButton("Delete All");
        reportButton = new JButton("Generate Report");

        //Setting up state options, and docExport format.
        //Change this from enum to actual state classes.
        stateFilterComboBox = new JComboBox<>(new String[]{"All", "To Do", "In Progress", "Completed"});
        taskStateComboBox = new JComboBox<>(TaskState.values());
        exportFormatComboBox = new JComboBox<>(new String[]{"Terminal","PDF", "CSV" , "JSON"});

        //Setting up the visual listModel object and the taskList JList visual object.
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskCellRenderer());

        window = new JFrame("Tasks Manager");
    }

    public void start() {
        JPanel addTaskPanel = new JPanel(new GridBagLayout());
        addTaskPanel.setBorder(BorderFactory.createTitledBorder("Add/Update Task"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Row 1: Task Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        addTaskPanel.add(new JLabel("Task Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        addTaskPanel.add(taskTitleInputF, gbc);

        // Row 2: Task Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        addTaskPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        addTaskPanel.add(new JScrollPane(descriptionInputTA), gbc);

        // Row 3: Task State
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        addTaskPanel.add(new JLabel("Task State:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        addTaskPanel.add(taskStateComboBox, gbc);

        // Row 4: Action Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.EAST;
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionButtonsPanel.add(addButton);
        actionButtonsPanel.add(updateButton);
        addTaskPanel.add(actionButtonsPanel, gbc);

        // Set initial state of buttons
        updateButton.setEnabled(false);
        taskStateComboBox.setEnabled(false);

        // Panel for search and filter controls
        JPanel searchAndFilterPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        searchAndFilterPanel.setBorder(BorderFactory.createTitledBorder("Search and Filter"));
        searchAndFilterPanel.add(new JLabel("Search Title:"));
        searchAndFilterPanel.add(searchTitleInput);
        searchAndFilterPanel.add(new JLabel("Search Description:"));
        searchAndFilterPanel.add(searchDescriptionInput);
        searchAndFilterPanel.add(new JLabel("Search ID:"));
        searchAndFilterPanel.add(searchIdInput);
        searchAndFilterPanel.add(new JLabel("Filter by State:"));
        searchAndFilterPanel.add(stateFilterComboBox);

        // Top panel containing both the add/update and search panels
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(addTaskPanel, BorderLayout.NORTH);
        topPanel.add(searchAndFilterPanel, BorderLayout.CENTER);

        // Center panel for the task list
        JPanel taskListPanel = new JPanel(new BorderLayout());
        taskListPanel.setBorder(BorderFactory.createTitledBorder("Task List"));
        taskListPanel.add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Bottom panel for action buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionsPanel.add(deleteButton);
        actionsPanel.add(deleteAllButton);
        actionsPanel.add(reportButton);
        actionsPanel.add(exportFormatComboBox);

        // Assemble the main content pane
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(taskListPanel, BorderLayout.CENTER);
        contentPane.add(actionsPanel, BorderLayout.SOUTH);

        window.setContentPane(contentPane);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        // --- Event Listeners ---
        addButton.addActionListener(e -> {
            ((TasksViewModel) viewModel).addButtonPressed();
//            String title = taskInput.getText();
//            String description = descriptionInput.getText();
//            if (!title.isEmpty()) {
//                ((TasksViewModel) viewModel).addTask(title, description);
//                resetForm();
//            }
        });

        updateButton.addActionListener(e -> {
            if (selectedTask != null) {
                String title = taskTitleInputF.getText();
                String description = descriptionInputTA.getText();
                TaskState state = (TaskState) taskStateComboBox.getSelectedItem();
                ((TasksViewModel) viewModel).updateTask(selectedTask.getId(), title, description, state);
                resetForm();
            }
        });

        deleteButton.addActionListener(e -> {
            ITask taskToDelete = taskList.getSelectedValue();
            if (taskToDelete != null) {
                ((TasksViewModel) viewModel).deleteTask(taskToDelete.getId());
                resetForm();
            } else {
                JOptionPane.showMessageDialog(window, "Please select a task to delete.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteAllButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(window, "Are you sure you want to delete all tasks?", "Confirm Delete All", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                ((TasksViewModel) viewModel).deleteAllTasks();
                resetForm();
            }
        });

        reportButton.addActionListener(e -> {
            String selectedFormat = (String) exportFormatComboBox.getSelectedItem();
            ((TasksViewModel) viewModel).generateReport(selectedFormat);
        });

        taskList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                //While value has changed but is not being actively tweaked
                if (!e.getValueIsAdjusting()) {
                    setSelectedTask(taskList.getSelectedValue());
                    if (selectedTask != null) {
                        taskTitleInputF.setText(selectedTask.getTitle());
                        descriptionInputTA.setText(selectedTask.getDescription());
                        taskStateComboBox.setSelectedItem(selectedTask.getState());
                        taskStateComboBox.setEnabled(true);
                        addButton.setEnabled(false);
                        updateButton.setEnabled(true);
                    } else {
                        resetForm();
                    }
                }
            }
        });

        stateFilterComboBox.addActionListener(e -> applyAllFilters());
        searchTitleInput.addActionListener(e -> applyAllFilters());
        searchDescriptionInput.addActionListener(e -> applyAllFilters());
        searchIdInput.addActionListener(e -> applyAllFilters());

        ((TasksViewModel) viewModel).loadTasks();
    }

    private void applyAllFilters() {
        String selectedState = (String) stateFilterComboBox.getSelectedItem();
        String titleTerm = searchTitleInput.getText();
        String descriptionTerm = searchDescriptionInput.getText();
        String idTerm = searchIdInput.getText();
        ((TasksViewModel) viewModel).filterTasks(selectedState, titleTerm, descriptionTerm, idTerm);
    }

//    private void resetForm() {
//        taskInput.setText("");
//        descriptionInput.setText("");
//        taskStateComboBox.setSelectedIndex(0);
//        taskStateComboBox.setEnabled(false);
//        addButton.setEnabled(true);
//        updateButton.setEnabled(false);
//        selectedTask = null;
//        taskList.clearSelection();
//    }

    /**
     * Called by the ViewModel when the list of tasks has been updated.
     * Schedules an update of the JList to show the new tasks.
     * @param tasks The new, updated list of tasks.
     */
    @Override
    public void onTasksChanged(List<ITask> tasks) {

        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (ITask task : tasks) {
                listModel.addElement(task);
            }
        });
    }

    @Override
    public IViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void setViewModel(IViewModel viewModel) {
        this.viewModel = viewModel;
        viewModel.addObserver(this);
    }

    public JTextField getTaskTitleInputF() {
        return taskTitleInputF;
    }

    public void setTaskTitleInputF(JTextField taskTitleInputF) {
        this.taskTitleInputF = taskTitleInputF;
    }

    public JTextArea getDescriptionInputTA() {
        return descriptionInputTA;
    }

    public void setDescriptionInputTA(JTextArea descriptionInputTA) {
        this.descriptionInputTA = descriptionInputTA;
    }

    public JButton getAddButton() {
        return addButton;
    }

    public void setAddButton(JButton addButton) {
        this.addButton = addButton;
    }

    public JButton getUpdateButton() {
        return updateButton;
    }

    public void setUpdateButton(JButton updateButton) {
        this.updateButton = updateButton;
    }

    public ITask getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(ITask selectedTask) {
        this.selectedTask = selectedTask;
    }

    public JList<ITask> getTaskList() {
        return taskList;
    }

    public void setTaskList(JList<ITask> taskList) {
        this.taskList = taskList;
    }

    public DefaultListModel<ITask> getListModel() {
        return listModel;
    }

    public void setListModel(DefaultListModel<ITask> listModel) {
        this.listModel = listModel;
    }

    public JTextField getSearchTitleInput() {
        return searchTitleInput;
    }

    public void setSearchTitleInput(JTextField searchTitleInput) {
        this.searchTitleInput = searchTitleInput;
    }

    public JTextField getSearchDescriptionInput() {
        return searchDescriptionInput;
    }

    public void setSearchDescriptionInput(JTextField searchDescriptionInput) {
        this.searchDescriptionInput = searchDescriptionInput;
    }

    public JTextField getSearchIdInput() {
        return searchIdInput;
    }

    public void setSearchIdInput(JTextField searchIdInput) {
        this.searchIdInput = searchIdInput;
    }

    public JComboBox<TaskState> getTaskStateComboBox() {
        return taskStateComboBox;
    }

    public void setTaskStateComboBox(JComboBox<TaskState> taskStateComboBox) {
        this.taskStateComboBox = taskStateComboBox;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(JButton deleteButton) {
        this.deleteButton = deleteButton;
    }

    public JButton getDeleteAllButton() {
        return deleteAllButton;
    }

    public void setDeleteAllButton(JButton deleteAllButton) {
        this.deleteAllButton = deleteAllButton;
    }
}