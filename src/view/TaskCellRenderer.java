package view;

import model.task.ITask;
import javax.swing.*;
import java.awt.*;

/*
* This class is here to control the UI look of each cell in the list rendered to the screen.
* */
public class TaskCellRenderer extends JPanel implements ListCellRenderer<ITask> {

    private final JLabel idLabel;
    private final JLabel titleLabel;
    private final JLabel descriptionLabel;
    private final JLabel stateLabel;

    public TaskCellRenderer() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Create panel for text content
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        // Initialize labels
        idLabel = new JLabel();
        titleLabel = new JLabel();
        descriptionLabel = new JLabel();
        stateLabel = new JLabel();

        // Style the title to be bold
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        descriptionLabel.setForeground(Color.GRAY);

        // Add labels to the text panel
        textPanel.add(titleLabel);
        textPanel.add(descriptionLabel);

        // Add the main components to the renderer panel
        add(idLabel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
        add(stateLabel, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ITask> list, ITask task, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        // Set the content for each label based on the task object
        idLabel.setText("#" + task.getId());
        titleLabel.setText(task.getTitle());
        descriptionLabel.setText(task.getDescription());
        stateLabel.setText(task.getState().getDisplayName());

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