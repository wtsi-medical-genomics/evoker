import javax.swing.*;
import java.awt.*;

public class LoggingDialog extends JDialog {
    private JTextArea ta;

    LoggingDialog(JFrame parent){
        super(parent, "Evoker Log");

        ta = new JTextArea();
        ta.setEditable(false);
        JScrollPane scrollzor = new JScrollPane(ta,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollzor.setPreferredSize(new Dimension(400,400));
        setContentPane(scrollzor);

        ta.setFont(new Font("Monospaced",Font.PLAIN,12));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);

        ta.append("*********\n");
        ta.append("Evoker...\n");
        ta.append("*********\n\n");        
    }

    public void log(String text){
        ta.append(text+"\n");
    }

}
