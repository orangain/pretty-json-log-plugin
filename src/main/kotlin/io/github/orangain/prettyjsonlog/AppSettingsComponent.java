package io.github.orangain.prettyjsonlog;


import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Supports creating and managing a {@link JPanel} for the Settings Dialog.
 */
public class AppSettingsComponent {

    private final JPanel myMainPanel;
    private final JBTextField messageFieldText = new JBTextField();

    private final JBTextField errorFieldText = new JBTextField();
//    private final JBCheckBox myIdeaUserStatus = new JBCheckBox("IntelliJ IDEA user");

    public AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Message fields:"), messageFieldText, 1, false)
                .addLabeledComponent(new JBLabel("Error fields:"), errorFieldText, 1, false)
                //.addComponent(myIdeaUserStatus, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return messageFieldText;
    }

    @NotNull
    public String getMessageFieldText() {
        return messageFieldText.getText();
    }

    public void setMessageFieldText(@NotNull String newText) {
        messageFieldText.setText(newText);
    }

    public String getErrorFieldText() {
        return errorFieldText.getText();
    }
    public void setErrorFieldText(@NotNull String newText) {
        errorFieldText.setText(newText);
    }

//    public boolean getIdeaUserStatus() {
//        return myIdeaUserStatus.isSelected();
//    }
//
//    public void setIdeaUserStatus(boolean newStatus) {
//        myIdeaUserStatus.setSelected(newStatus);
//    }

}
