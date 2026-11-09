import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServidorChatGUI extends JFrame {
    private JTextArea areaLog;
    private DefaultListModel<String> modeloListaUsuarios;
    private JList<String> listaUsuarios;
    private JButton btnIniciar;
    private ServerSocket serverSocket;
    private boolean enEjecucion = false;

    // Mapa de clientes conectados: Nombre -> PrintWriter
    private final Map<String, PrintWriter> clientesConectados = new HashMap<>();

    public ServidorChatGUI() {
        setTitle("Servidor Central de Chat - Sockets");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnIniciar = new JButton("Iniciar Servidor (Puerto 5000)");
        panelSuperior.add(btnIniciar);
        add(panelSuperior, BorderLayout.NORTH);

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Log de Actividad del Servidor"));
        add(scrollLog, BorderLayout.CENTER);

        modeloListaUsuarios = new DefaultListModel<>();
        listaUsuarios = new JList<>(modeloListaUsuarios);
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        scrollUsuarios.setBorder(BorderFactory.createTitledBorder("Usuarios Conectados"));
        scrollUsuarios.setPreferredSize(new Dimension(180, 0));
        add(scrollUsuarios, BorderLayout.EAST);

        btnIniciar.addActionListener(e -> alternarServidor());
    }

    private void alternarServidor() {
        if (!enEjecucion) {
            new Thread(this::iniciarServidor).start();
        } else {
            detenerServidor();
        }
    }

    private void iniciarServidor() {
        try {
            int puerto = 5000;
            serverSocket = new ServerSocket(puerto);
            enEjecucion = true;
            btnIniciar.setText("Detener Servidor");
            areaLog.append(">>> Servidor iniciado y escuchando en el puerto " + puerto + "\n");

            while (enEjecucion) {
                Socket socketCliente = serverSocket.accept();
                new Thread(new ManejadorCliente(socketCliente)).start();
            }
        } catch (IOException e) {
            if (enEjecucion) {
                areaLog.append("Error en el servidor: " + e.getMessage() + "\n");
            }
        }
    }

    private void detenerServidor() {
        enEjecucion = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        btnIniciar.setText("Iniciar Servidor (Puerto 5000)");
        areaLog.append(">>> Servidor detenido.\n");
        modeloListaUsuarios.clear();
        clientesConectados.clear();
    }

    // Método para reenviar mensajes a todos los clientes conectados (Broadcast)
    private synchronized void difundirMensaje(String emisor, String mensaje) {
        String formatoMensaje = emisor + "--> " + mensaje;
        for (Map.Entry<String, PrintWriter> entry : clientesConectados.entrySet()) {
            entry.getValue().println(formatoMensaje);
        }
    }

    private class ManejadorCliente implements Runnable {
        private Socket socket;
        private PrintWriter salida;
        private BufferedReader entrada;
        private String nombreUsuario;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                salida = new PrintWriter(socket.getOutputStream(), true);

                // Leer el nombre enviado por el cliente al conectarse
                nombreUsuario = entrada.readLine();
                if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
                    nombreUsuario = "Anónimo";
                }

                // Asegurar nombres únicos en el chat
                synchronized (clientesConectados) {
                    String nombreBase = nombreUsuario;
                    int contador = 1;
                    while (clientesConectados.containsKey(nombreUsuario)) {
                        nombreUsuario = nombreBase + "_" + contador;
                        contador++;
                    }
                    clientesConectados.put(nombreUsuario, salida);
                }

                final String nombreFinal = nombreUsuario;
                SwingUtilities.invokeLater(() -> {
                    modeloListaUsuarios.addElement(nombreFinal);
                    areaLog.append("[CONEXIÓN] Se unió: " + nombreFinal + "\n");
                });

                // Anunciar a la sala que alguien entró
                difundirMensaje("Servidor", "El usuario " + nombreFinal + " se ha conectado al chat.");

                String mensaje;
                while ((mensaje = entrada.readLine()) != null) {
                    if (mensaje.equalsIgnoreCase("chao") || mensaje.equalsIgnoreCase("exit")) {
                        difundirMensaje("Servidor", "El usuario " + nombreFinal + " ha salido del chat.");
                        break;
                    }
                    // Reenviar el mensaje de este usuario a todos los demás en el chat
                    difundirMensaje(nombreFinal, mensaje);
                }

            } catch (IOException e) {
                // Error de conexión o cierre abrupto
            } finally {
                if (nombreUsuario != null) {
                    synchronized (clientesConectados) {
                        clientesConectados.remove(nombreUsuario);
                    }
                    final String nombreFinal = nombreUsuario;
                    SwingUtilities.invokeLater(() -> {
                        modeloListaUsuarios.removeElement(nombreFinal);
                        areaLog.append("[DESCONEXIÓN] Salió: " + nombreFinal + "\n");
                    });
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServidorChatGUI().setVisible(true));
    }
}