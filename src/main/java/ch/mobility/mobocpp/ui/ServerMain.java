package ch.mobility.mobocpp.ui;

import ch.mobility.mobocpp.kafka.AvroProsumer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import sun.misc.Signal;

import java.net.URL;

public class ServerMain {

    public static void main(String[] args) throws Exception {

        String hostIP = "192.168.1.48";
        if (args.length == 1 && (!"".equals(args[0]))) {
            hostIP = args[0];
        }
        AvroProsumer.init(hostIP);

        Server server = new Server(8088);
        WebAppContext webAppContext = new WebAppContext();
        server.setHandler(webAppContext);

        // Load static content from inside the jar file.
        URL webAppDir =
                ServerMain.class.getClassLoader().getResource("META-INF/resources");
        webAppContext.setResourceBase(webAppDir.toURI().toString());

        // Look for annotations in the classes directory (dev server) and in the
        // jar file (live server)
        webAppContext.setAttribute(
                "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/target/classes/|.*\\.jar");

        // Start the server! 🚀
        server.start();
        System.out.println("Server started, accessing Kafka on host " + hostIP);

        registerShutdownFunctions(server);

        // Keep the main thread alive while the server is running.
        server.join();
        logInfo("Bye");
    }

    private static void registerShutdownFunctions(Server server) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                stop(server, "ShutdownHook");
            } catch (Exception e) {
                logError("ShutdownHook: " + e.getMessage());
            }
        }));
//        // https://stackoverflow.com/questions/1611931/catching-ctrlc-in-java
        handleSignal(server, "INT");
        handleSignal(server, "TERM");
    }

    private static void handleSignal(Server server, String signalName) {
        Signal.handle(new Signal(signalName),  // SIGTERM
            signal -> {
                try {
                    stop(server, "SIG" + signalName);
                } catch (Exception e) {
                    logError("SIG" + signalName + ": " + e.getMessage());
                }
            });
    }

    private static void stop(Server server, String msg) throws Exception {
        logInfo("Stopping Server: " + msg);
        AvroProsumer.get().close();
        server.stop();
        server.destroy();
    }

    private static void logInfo(String msg) {
        System.out.println(msg);
    }

    private static void logError(String msg) {
        System.err.println(msg);
    }
}
