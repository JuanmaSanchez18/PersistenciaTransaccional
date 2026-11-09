# Persistencia y Datos Transaccionales – Entrega 1 (Escenario 3)

Chat bidireccional multiusuario con sockets TCP en Java 11 (interfaz Swing).

**Subgrupo B4** – Politécnico Grancolombiano · Docente: William Matallana Porras

| Archivo | Descripción |
|---|---|
| `src/main/java/ServidorChatGUI.java` | Servidor: escucha en el puerto 5000, un hilo por cliente, difunde mensajes |
| `src/main/java/ClienteChatGUI.java` | Cliente: IP, puerto y nombre configurables; `chao` para salir |
| `src/main/java/org/example/Main.java` | Plantilla de IntelliJ (no participa en la solución) |

## Ejecución

```bash
mvn compile
java -cp target/classes ServidorChatGUI   # pulsar "Iniciar Servidor"
java -cp target/classes ClienteChatGUI    # abrir varias veces (Poli01, Poli02, Poli03)
```

Por defecto los clientes se conectan a `127.0.0.1:5000`.
