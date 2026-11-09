import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteChatGUI extends JFrame {
    private JTextField txtIp, txtPuerto, txtNombre, txtMensaje;
    private JTextArea areaChat;
    private JButton btnConectar, btnEnviar;
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private boolean conectado = false;

    public ClienteChatGUI() {
        setTitle("Cliente de Chat");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel de Configuración
        JPanel panelConfig = new JPanel(new GridLayout(2, 4, 5, 5));
        panelConfig.setBorder(BorderFactory.createTitledBorder("Configuración"));

        panelConfig.add(new JLabel("IP:"));
        txtIp = new JTextField("127.0.0.1");
        panelConfig.add(txtIp);

        panelConfig.add(new JLabel("Puerto:"));
        txtPuerto = new JTextField("5000");
        panelConfig.add(txtPuerto);

        panelConfig.add(new JLabel("Nombre:"));
        txtNombre = new JTextField("Poli01"); // Puedes cambiarlo a Poli03 en otra ventana
        panelConfig.add(txtNombre);

        btnConectar = new JButton("Conectar");
        panelConfig.add(btnConectar);

        add(panelConfig, BorderLayout.NORTH);

        // Área de Conversación (Chat principal)
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        add(new JScrollPane(areaChat), BorderLayout.CENTER);

        // Panel Inferior para escribir mensajes
        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));
        txtMensaje = new JTextField();
        txtMensaje.setEnabled(false);
        panelInferior.add(txtMensaje, BorderLayout.CENTER);

        btnEnviar = new JButton("Enviar");
        btnEnviar.setEnabled(false);
        panelInferior.add(btnEnviar, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);

        // Eventos
        btnConectar.addActionListener(e -> conectarServidor());
        btnEnviar.addActionListener(e -> enviarMensaje());
        txtMensaje.addActionListener(e -> enviarMensaje());
    }

    private void conectarServidor() {
        if (!conectado) {
            try {
                String ip = txtIp.getText().trim();
                int puerto = Integer.parseInt(txtPuerto.getText().trim());
                String nombre = txtNombre.getText().trim();

                socket = new Socket(ip, puerto);
                salida = new PrintWriter(socket.getOutputStream(), true);
                entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Enviar el nombre de usuario al servidor de inmediato
                salida.println(nombre);

                conectado = true;
                btnConectar.setText("Desconectar");
                txtIp.setEnabled(false);
                txtPuerto.setEnabled(false);
                txtNombre.setEnabled(false);
                txtMensaje.setEnabled(true);
                btnEnviar.setEnabled(true);
                areaChat.append("Conectado al chat. Escribe 'chao' para salir.\n");

                // Hilo para escuchar mensajes de otros usuarios enviados por el servidor
                new Thread(this::escucharServidor).start();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            enviarChaoYDesconectar();
        }
    }

    private void escucharServidor() {
        try {
            String mensajeServidor;
            while ((mensajeServidor = entrada.readLine()) != null) {
                areaChat.append(mensajeServidor + "\n");
            }
        } catch (IOException e) {
            areaChat.append("La conexión con el servidor se ha cerrado.\n");
        }
    }

    private void enviarMensaje() {
        String texto = txtMensaje.getText().trim();
        if (!texto.isEmpty() && salida != null) {
            salida.println(texto);
            txtMensaje.setText("");
            if (texto.equalsIgnoreCase("chao")) {
                desconectarUI();
            }
        }
    }

    private void enviarChaoYDesconectar() {
        if (salida != null) {
            salida.println("chao");
        }
        desconectarUI();
    }

    private void desconectarUI() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        conectado = false;
        btnConectar.setText("Conectar");
        txtIp.setEnabled(true);
        txtPuerto.setEnabled(true);
        txtNombre.setEnabled(true);
        txtMensaje.setEnabled(false);
        btnEnviar.setEnabled(false);
        areaChat.append("Has salido de la sesión de chat.\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClienteChatGUI().setVisible(true));
    }
}