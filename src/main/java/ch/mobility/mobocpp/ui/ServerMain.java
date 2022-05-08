package ch.mobility.mobocpp.ui;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.net.URL;

public class ServerMain {

    public static String HOST = "192.168.1.12";
//    public static String HOST = "192.168.1.48";
    public static String PORT_BOOTSTRAP = "9092";
    public static String PORT_SCHEMA_REGISTRY = "8081";

    public static void main(String[] args) throws Exception {

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
        System.out.println("Server started!");

        // Keep the main thread alive while the server is running.
        server.join();
    }

}
