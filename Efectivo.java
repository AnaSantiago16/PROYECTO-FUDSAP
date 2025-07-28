/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package productos;

import Empleado.loginEmpleado;
import calculadora.CalculadoraImpuestos;
import java.awt.HeadlessException;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.swing.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.File;
import java.io.IOException;
import loginform.loginUsuario;

public class Efectivo {

    public static void procesarPagoEfectivo(double subtotal, double ivaCalculado, double totalConIVA, Map<String, ProductoSeleccionado> carrito) {
        String correoUsuario = loginUsuario.correoUsuario;
        String correoEmpleado = loginEmpleado.correoEmpleado;
        String correoFinal = null;
        String nombreCliente = null;

        boolean esEmpleado = false;

        if (correoUsuario != null && !correoUsuario.isEmpty()) {
            correoFinal = correoUsuario;
            nombreCliente = obtenerNombreDesdeBD(correoFinal, false);
        } else if (correoEmpleado != null && !correoEmpleado.isEmpty()) {
            correoFinal = correoEmpleado;
            esEmpleado = true;
            nombreCliente = obtenerNombreDesdeBD(correoFinal, true);
        }

        if (correoFinal == null || nombreCliente == null) {
            JOptionPane.showMessageDialog(null, "No se pudo recuperar el correo o nombre del usuario.");
            return;
        }

        try {
            Document document = new Document();
            File pdfFile;

            if (correoFinal.equalsIgnoreCase("santy18belen@gmail.com")) {
                File carpeta = new File("Venta");
                if (!carpeta.exists()) carpeta.mkdirs(); // Crear carpeta si no existe
                pdfFile = new File(carpeta, "ticket_compra_" + System.currentTimeMillis() + ".pdf");
            } else {
                pdfFile = new File("ticket_compra.pdf");
            }

            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

            Paragraph titulo = new Paragraph("RECIBO DE COMPRA", titleFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            if (correoFinal.equalsIgnoreCase("santy18belen@gmail.com")) {
                document.add(new Paragraph("Cliente: " + nombreCliente, normalFont));
            }

            document.add(new Paragraph("Correo: " + correoFinal, normalFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1, 2});
            table.addCell("Producto");
            table.addCell("Cantidad");
            table.addCell("Subtotal");

            for (ProductoSeleccionado p : carrito.values()) {
                table.addCell(p.getNombre());
                table.addCell(String.valueOf(p.getCantidad()));
                table.addCell(String.format("$%.2f", p.getCantidad() * p.getPrecioUnitario()));
            }

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(String.format("Subtotal: $%.2f", subtotal), normalFont));
            document.add(new Paragraph(String.format("IVA (16%%): $%.2f", ivaCalculado), normalFont));
            document.add(new Paragraph(String.format("Total Final: $%.2f", totalConIVA), normalFont));
            document.add(new Paragraph("Gracias por su compra.", normalFont));
            document.close();

            if (correoFinal.equalsIgnoreCase("santy18belen@gmail.com")) {
                JOptionPane.showMessageDialog(null, "Ticket guardado correctamente en carpeta Venta/");
            } else {
                enviarCorreoConPDF(correoFinal, "Tu recibo de compra", "Adjunto encontrarás tu ticket de compra.", pdfFile);
                JOptionPane.showMessageDialog(null, "Ticket enviado al correo: " + correoFinal);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al generar o enviar el ticket: " + e.getMessage());
        }
    }

    private static String obtenerNombreDesdeBD(String correo, boolean esEmpleado) {
        String tabla = esEmpleado ? "empleado" : "registro";
        String consulta = "SELECT nombre FROM " + tabla + " WHERE correo = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Datos", "postgres", "AnaSanty16");
             PreparedStatement stmt = conn.prepareStatement(consulta)) {
            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nombre");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al obtener nombre desde BD: " + ex.getMessage());
        }
        return null;
    }

    private static void enviarCorreoConPDF(String destinatario, String asunto, String mensajeTexto, File pdfAdjunto) {
        final String remitente = "fudsap.25@gmail.com";
        final String password = "cgvh dirx jirv rbzm";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session sesion = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, password);
            }
        });

        try {
            Message message = new MimeMessage(sesion);
            message.setFrom(new InternetAddress(remitente));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto);

            MimeBodyPart mensajeTextoPart = new MimeBodyPart();
            mensajeTextoPart.setText(mensajeTexto);

            MimeBodyPart adjuntoPart = new MimeBodyPart();
            adjuntoPart.attachFile(pdfAdjunto);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mensajeTextoPart);
            multipart.addBodyPart(adjuntoPart);

            message.setContent(multipart);

            Transport.send(message);

        } catch (MessagingException | IOException e) {
            JOptionPane.showMessageDialog(null, "Error al enviar correo: " + e.getMessage());
        }
    }
}
