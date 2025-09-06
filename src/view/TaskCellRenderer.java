package view;

import model.task.ITask;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * ListCellRenderer for rendering {@link ITask} items in a list component.
 * <p>
 * Displays task attributes such as ID, title, description, state, creation date, and priority
 * using a panel with labeled fields. Selection and focus states are reflected in the background
 * and foreground as per the host look-and-feel.
 */
public class TaskCellRenderer extends JPanel implements ListCellRenderer<ITask> {

    private final JLabel idLabel;
    private final JLabel titleLabel;
    private final JLabel descriptionLabel;
    private final JLabel stateLabel;
    private final JLabel creationDateLabel;
    private final JLabel priorityLabel;

    public TaskCellRenderer() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Create a main panel for all the content
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Create a panel for the top-right section (state and priority)
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));

        // Create a panel for the center section (title and description)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Initialize labels
        idLabel = new JLabel();
        titleLabel = new JLabel();
        descriptionLabel = new JLabel();
        stateLabel = new JLabel();
        creationDateLabel = new JLabel();
        priorityLabel = new JLabel();

        // Style the title to be bold
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        descriptionLabel.setForeground(Color.GRAY);
        creationDateLabel.setForeground(Color.GRAY);
        priorityLabel.setFont(priorityLabel.getFont().deriveFont(Font.ITALIC, 11f));

        // Add labels to the text panel
        centerPanel.add(titleLabel);
        centerPanel.add(descriptionLabel);
        centerPanel.add(creationDateLabel);

        eastPanel.add(stateLabel);
        eastPanel.add(priorityLabel);

        // Add the main parts to the renderer panel
        add(idLabel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(eastPanel, BorderLayout.EAST);
    }

    /**
     * Configures and returns the component used to render the specified list cell.
     *
     * @param list the JList being painted
     * @param task the task value to render for the current cell
     * @param index the cell index
     * @param isSelected whether the cell is selected
     * @param cellHasFocus whether the cell currently has focus
     * @return a component that renders the current cell's value
     */
    @Override
    public Component getListCellRendererComponent(JList<? extends ITask> list, ITask task, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        // Set the content for each label based on the task object
        idLabel.setText("#" + task.getId());
        titleLabel.setText(task.getTitle());
        descriptionLabel.setText(task.getDescription());
        stateLabel.setText(task.getState().getDisplayName());
        priorityLabel.setText(task.getPriority().getDisplayName());
        creationDateLabel.setText(dateFormat.format(task.getCreationDate()));

        // Handle selection and focusing
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}