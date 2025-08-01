/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package productos;

/**
 *
 * @author Ana
 */

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ButtonRenderer extends JButton implements TableCellRenderer {

    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        
        if (value instanceof JButton) {
            JButton boton = (JButton) value;
            setIcon(boton.getIcon()); // esto es lo importante
            setToolTipText(boton.getToolTipText());
            setText(""); // <-- sin texto, solo el ícono
        } else {
            setText((value == null) ? "" : value.toString());
        }
        return this;
    }
}
