

import javax.swing.*;
import java.awt.*;

/**
 * Java Minesweeper Game
 *
 * Original Author: Jan Bodnar
 * Website: http://zetcode.com
 * Adapted by Chris Organ
 */

public class Test extends JFrame {


    private JLabel statusbar;

    public Test() {

        initUI();
    }

    private void initUI() {

        statusbar = new JLabel("");
        add(statusbar, BorderLayout.SOUTH);

        add(new GameWindow(statusbar,GameWindow.N_ROWS,GameWindow.N_COLS));

        setResizable(true);
        pack();

        setTitle("Minesweeper");
        setLocationRelativeTo(null);


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {

            var ex = new Test();
            ex.setVisible(true);
        });
    }
}
