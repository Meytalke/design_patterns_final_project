package view;

import model.task.*;
import viewmodel.IViewModel;
import viewmodel.TasksViewModel;
import viewmodel.strategy.SortByCreationDateStrategy;
import viewmodel.strategy.SortByPriorityStrategy;
import viewmodel.strategy.SortByTitleStrategy;
import viewmodel.strategy.SortingOption;

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
    private final JTextArea descriptionInputTA;
    //Control buttons - addTask, updateTask, deleteTask, deleteAllTasks, report?
    private final JButton addButton;
    private final JButton updateButton;
    private final JButton deleteButton;
    private final JButton deleteAllButton;
    private final JButton reportButton;
    private final JButton upButton,downButton;
    //Task state in string value, convert to IState to control more accurate filtering
    private final JComboBox<String> stateFilterComboBox;
    private final JComboBox<SortingOption> sortComboBox;
    private final JComboBox<String> exportFormatComboBox;
    private final JComboBox<TaskPriority> taskPriorityComboBox;
    private final JComboBox<TaskState> taskStateComboBox;
    private final JLabel creationDateLabel;
    private final TaskState selectedTaskState = new ToDoState();
    private ITask selectedTask = null;

    //Task list in memory and a listModel list to store the tasks visually
    private JList<ITask> taskList;
    private DefaultListModel<ITask> listModel;

    //Search specific regex fields
    private final JTextField searchTitleInput;
    private final JTextField searchDescriptionInput;
    private final JTextField searchIdInput;

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
        upButton = new JButton("↑");
        downButton = new JButton("↓");

        //Setting up state options, and docExport format.
        //Change this from enum to actual state classes.
        stateFilterComboBox = new JComboBox<>(new String[]{"All", "To Do", "In Progress", "Completed"});
        taskStateComboBox = new JComboBox<TaskState>(new TaskState[]{
                getSelectedTaskState(),//ToDoState
                getSelectedTaskState().next(), //InProgressState
                getSelectedTaskState().next().next() //CompletedState
        });
        taskStateComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TaskState state) {
                    setText(state.getDisplayName());
                }
                return this;
            }
        });
        taskPriorityComboBox = new JComboBox<>(TaskPriority.values());
        taskPriorityComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TaskPriority priority) {
                    setText(priority.getDisplayName());
                }
                return this;
            }
        });

        sortComboBox = new JComboBox<>(SortingOption.values());
        sortComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SortingOption option) {
                    setText(option.getDisplayName());
                }
                return this;
            }
        });

        creationDateLabel = new JLabel("Creation Date: N/A");
        
        exportFormatComboBox = new JComboBox<>(new String[]{"Terminal","PDF", "CSV" , "JSON"});

        //Setting up the visual listModel object and the taskList JList visual object.
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskCellRenderer());

        window = new JFrame("Tasks Manager");
    }

    private void selectTaskStateInComboBox(TaskState stateToSelect) {
        for (int i = 0; i < taskStateComboBox.getItemCount(); i++) {
            TaskState state = taskStateComboBox.getItemAt(i);
            if (state.getClass() == stateToSelect.getClass()) {
                taskStateComboBox.setSelectedIndex(i);
                return;
            }
        }
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

        // Row 4: Task Priority
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        addTaskPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        addTaskPanel.add(taskPriorityComboBox, gbc);

        // Row 5: Creation Date
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE;
        addTaskPanel.add(creationDateLabel, gbc);

        // Row 4: Action Buttons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.EAST;
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionButtonsPanel.add(addButton);
        actionButtonsPanel.add(updateButton);
        actionButtonsPanel.add(upButton);
        actionButtonsPanel.add(downButton);
        addTaskPanel.add(actionButtonsPanel, gbc);

        // Set initial state of buttons
        updateButton.setEnabled(false);
        taskStateComboBox.setEnabled(false);
        upButton.setEnabled(false);
        downButton.setEnabled(false);

        // Panel for search and filter controls
        JPanel searchAndFilterPanel = new JPanel(new GridBagLayout()); // Changed to GridBagLayout
        searchAndFilterPanel.setBorder(BorderFactory.createTitledBorder("Search and Filter"));

        GridBagConstraints searchGbc = new GridBagConstraints(); // New GBC for this panel
        searchGbc.insets = new Insets(5, 5, 5, 5); // Padding

        // --- Row 1: Search Title ---
        searchGbc.gridx = 0; searchGbc.gridy = 0; searchGbc.anchor = GridBagConstraints.WEST;
        searchAndFilterPanel.add(new JLabel("Search Title:"), searchGbc);
        searchGbc.gridx = 1; searchGbc.gridy = 0; searchGbc.fill = GridBagConstraints.HORIZONTAL; searchGbc.weightx = 1.0;
        searchAndFilterPanel.add(searchTitleInput, searchGbc);

        // --- Row 2: Search Description ---
        searchGbc.gridx = 0; searchGbc.gridy = 1; searchGbc.anchor = GridBagConstraints.WEST;
        searchGbc.fill = GridBagConstraints.NONE; searchGbc.weightx = 0; // Reset fill/weightx
        searchAndFilterPanel.add(new JLabel("Search Description:"), searchGbc);
        searchGbc.gridx = 1; searchGbc.gridy = 1; searchGbc.fill = GridBagConstraints.HORIZONTAL; searchGbc.weightx = 1.0;
        searchAndFilterPanel.add(searchDescriptionInput, searchGbc);

        // --- Row 3: Search ID ---
        searchGbc.gridx = 0; searchGbc.gridy = 2; searchGbc.anchor = GridBagConstraints.WEST;
        searchGbc.fill = GridBagConstraints.NONE; searchGbc.weightx = 0;
        searchAndFilterPanel.add(new JLabel("Search ID:"), searchGbc);
        searchGbc.gridx = 1; searchGbc.gridy = 2; searchGbc.fill = GridBagConstraints.HORIZONTAL; searchGbc.weightx = 1.0;
        searchAndFilterPanel.add(searchIdInput, searchGbc);

        // --- Row 4: Filter by State ---
        searchGbc.gridx = 0; searchGbc.gridy = 3; searchGbc.anchor = GridBagConstraints.WEST;
        searchGbc.fill = GridBagConstraints.NONE; searchGbc.weightx = 0;
        searchAndFilterPanel.add(new JLabel("Filter by State:"), searchGbc);
        searchGbc.gridx = 1; searchGbc.gridy = 3; searchGbc.fill = GridBagConstraints.HORIZONTAL; searchGbc.weightx = 1.0;
        searchAndFilterPanel.add(stateFilterComboBox, searchGbc);

        // --- Row 5: Sort by ---
        searchGbc.gridx = 0; searchGbc.gridy = 4; searchGbc.anchor = GridBagConstraints.WEST;
        searchGbc.fill = GridBagConstraints.NONE; searchGbc.weightx = 0;
        searchAndFilterPanel.add(new JLabel("Sort by:"), searchGbc);
        searchGbc.gridx = 1; searchGbc.gridy = 4; searchGbc.fill = GridBagConstraints.HORIZONTAL; searchGbc.weightx = 1.0;
        searchAndFilterPanel.add(sortComboBox, searchGbc);


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
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);

        // --- Event Listeners ---
        addButton.addActionListener(e -> {
            String title = taskTitleInputF.getText();
            String description = descriptionInputTA.getText();
            TaskPriority priority = (TaskPriority) taskPriorityComboBox.getSelectedItem();
            ((TasksViewModel) viewModel).addButtonPressed(title, description, priority);

            resetForm();
        });

        updateButton.addActionListener(e -> {
            if (selectedTask != null) {
                String title = taskTitleInputF.getText();
                String description = descriptionInputTA.getText();
                TaskState state = (TaskState) taskStateComboBox.getSelectedItem();
                TaskPriority priority = (TaskPriority) taskPriorityComboBox.getSelectedItem();
                ((TasksViewModel) viewModel).updateButtonPressed(selectedTask.getId(), title, description, state, priority);

                resetForm();
            }
        });

        upButton.addActionListener(e -> {
            if (selectedTask != null) {
                ((TasksViewModel) viewModel).upButtonPressed(selectedTask.getId());
                resetForm();
            }
        });

        downButton.addActionListener(e -> {
            if (selectedTask != null) {
                ((TasksViewModel) viewModel).downButtonPressed(selectedTask.getId());
                resetForm();
            }
        });

        sortComboBox.addActionListener(e -> {
            SortingOption selectedOption = (SortingOption) sortComboBox.getSelectedItem();

            switch (selectedOption) {
                case PRIORITY:
                    ((TasksViewModel) viewModel).setSortingStrategy(new SortByPriorityStrategy());
                    break;
                case CREATION_DATE:
                    ((TasksViewModel) viewModel).setSortingStrategy(new SortByCreationDateStrategy());
                    break;
                case TITLE:
                    ((TasksViewModel) viewModel).setSortingStrategy(new SortByTitleStrategy());
                    break;
                case null:
                    break;
                default:
                    ((TasksViewModel) viewModel).setSortingStrategy(new SortByTitleStrategy());
                    break;
            }
        });

        deleteButton.addActionListener(e -> {
            if (selectedTask != null) {
                ((TasksViewModel) viewModel).deleteButtonPressed(selectedTask.getId());

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
                        selectTaskStateInComboBox(selectedTask.getState());
                        taskPriorityComboBox.setSelectedItem(selectedTask.getPriority());
                        creationDateLabel.setText("Creation Date: " + selectedTask.getCreationDate().toString());
                        taskStateComboBox.setEnabled(true);
                        addButton.setEnabled(false);
                        updateButton.setEnabled(true);
                        upButton.setEnabled(true);
                        downButton.setEnabled(true);
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

    private void resetForm() {
        taskTitleInputF.setText("");
        descriptionInputTA.setText("");
        taskStateComboBox.setSelectedIndex(0);
        taskPriorityComboBox.setSelectedIndex(0);
        taskStateComboBox.setEnabled(false);
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        downButton.setEnabled(false);
        upButton.setEnabled(false);
        selectedTask = null;
        taskList.clearSelection();
    }

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
//        this.taskTitleInputF = taskTitleInputF;
    }

    public JTextArea getDescriptionInputTA() {
        return descriptionInputTA;
    }


    public JButton getAddButton() {
        return addButton;
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

    public JTextField getSearchDescriptionInput() {
        return searchDescriptionInput;
    }

    public JTextField getSearchIdInput() {
        return searchIdInput;
    }

    public JComboBox<TaskState> getTaskStateComboBox() {
        return taskStateComboBox;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getDeleteAllButton() {
        return deleteAllButton;
    }

    public TaskState getSelectedTaskState() {return selectedTaskState;}
}