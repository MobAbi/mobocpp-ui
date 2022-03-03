package ch.mobility.mobocpp.ui;

import ch.mobility.ocpp2mob.CSStatusConnected;
import ch.mobility.ocpp2mob.CSStatusConnectedResponse;
import ch.mobility.ocpp2mob.ChargingStateEnum;
import ch.mobility.ocpp2mob.ConnectorStatusEnum;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/map")
public class MapServlet extends HttpServlet {

    private static int LAST_CONTACT_OK_IN_SECONDS = 5 * 60;
    final static String RED = "#f03";          // F1
    final static String YELLOW_LIGHT = "#ff9"; // W1
    final static String YELLOW_DARK = "#ff0";  // W2
    final static String BLUE = "#00f";         // O1
    final static String GREEN_DARK = "#0f0";   // O2
    final static String GREEN_LIGHT = "#0f9";  // O3

    private static String KeyLadestationBekanntVerbindungBestehtA = "JCME321202921192";
    private static String KeyLadestationBekanntVerbindungBestehtB = "JC310001";
    private static String KeyLadestationBekanntVerbindungBestehtC = "1311222009";

    // Simuliert die Stammdaten zu den Ladestationen
    private static List<CSStammdaten> stammdatenMap = new ArrayList<>();
    private static Map<String, Instant> lastContact = new HashMap<>();
    static {
        //stammdatenMap.add(CSStammdaten.of(KeyLadestationOhneStammdaten, "47.5475926,7.5874733", "Basel Hauptbahnhof"));
        stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntKeineVerbindungLetzerKontaktKleinerN, "47.4458578,9.1400634", "Wil Raiffeisenbank"));
        stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntKeineVerbindungLetzerKontaktGroesserN, "47.1442198,8.4349035", "Rotkreuz Hauptsitz"));
        stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtLadestationMeldetFehler, "46.5160055,6.6277126", "Lausanne Bahnhof"));
        stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtKeinFahrzeugAngeschlossen, "46.1726474,8.7994749", "Locarno SBB"));
        stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLaden, "46.8482,9.5311401", "Chur Museumsstrasse"));
        stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenAmLaden, "47.3776673,8.5323237", "Zurich Europaallee"));

        stammdatenMap.add(CSStammdaten.of(KeyLadestationBekanntVerbindungBestehtA, "46.9585416,8.3640993", "Stans Bahnhof"));
        stammdatenMap.add(CSStammdaten.of(KeyLadestationBekanntVerbindungBestehtB, "46.8974001,8.2461243", "Sarnen Bahnhof"));
        stammdatenMap.add(CSStammdaten.of(KeyLadestationBekanntVerbindungBestehtC, "46.2937841,7.8794028", "Visp Bahnhof"));
    }

//    private AvroProducer avroProducer = null;
//    private AvroConsumer avroConsumer = null;

    private AvroProducer getAvroProducer() {
        return AvroProducer.get();
    }

    private AvroConsumer getAvroConsumer() {
        return AvroConsumer.get();
    }

    @Override
    public void init() {
//        avroProducer = new AvroProducer();
//        avroConsumer = new AvroConsumer();
    }

    @Override
    public void destroy() {
//        if (avroProducer != null) {
//            avroProducer.close();
//        }
//        if (avroConsumer != null) {
//            avroConsumer.close();
//        }
        AvroProducer.get().close();
        AvroConsumer.get().close();
    }

    private CSStammdaten getStammdaten(String key) {
        for (CSStammdaten csStammdaten : stammdatenMap) {
            if (csStammdaten.getId().equalsIgnoreCase(key)) {
                return csStammdaten;
            }
        }
        return null;
    }

    private CSStatusConnected getStatusConnected(List<CSStatusConnectedResponse> receiveConnected, String id) {
        for (CSStatusConnectedResponse csStatusConnectedResponse : receiveConnected) {
            for (CSStatusConnected csStatusConnected : csStatusConnectedResponse.getCSStatusList()) {
                if (str(csStatusConnected.getId()).equalsIgnoreCase(id)) {
                    return csStatusConnected;
                }
            }
        }
        return null;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        getAvroProducer().requestStatusConnected();
        List<CSStatusConnectedResponse> receiveConnected = getAvroConsumer().receive(CSStatusConnectedResponse.class, 3000, 1);
        for (CSStatusConnectedResponse csStatusConnectedResponse : receiveConnected) {
            for (CSStatusConnected csStatusConnected : csStatusConnectedResponse.getCSStatusList()) {
                lastContact.put(str(csStatusConnected.getId()), DateTimeHelper.parse(str(csStatusConnected.getLastContact())));
//                getAvroProducer().requestStatusForId(str(csStatusConnected.getId()), null, 1);
//                List<CSStatusForIdResponse> receiveDetail = getAvroConsumer().receive(CSStatusForIdResponse.class, 10000, 1);
            }
        }

        receiveConnected.add(FakeCSStatusConnected.addFakeCS(lastContact));

        final List<CSStatusConnected> keineStammdaten = new ArrayList<>(); // A1
        for (CSStatusConnectedResponse csStatusConnectedResponse : receiveConnected) {
            for (CSStatusConnected csStatusConnected : csStatusConnectedResponse.getCSStatusList()) {
                final CSStammdaten stammdaten = getStammdaten(str(csStatusConnected.getId()));
                if (stammdaten == null) {
                    keineStammdaten.add(csStatusConnected);
                }
            }
        }

        response.setContentType("text/html;");
        response.getWriter().println("<!DOCTYPE html>");
        response.getWriter().println("<html>");
        response.getWriter().println(getHead());
        response.getWriter().println("<body>");

        response.getWriter().println("<div id=\"map\"></div>");
        if (!keineStammdaten.isEmpty()) {
            response.getWriter().println("<br>Zu folgenden Ladestationen fehlen Stammdaten:");
            for (CSStatusConnected statusConnected : keineStammdaten) {
                response.getWriter().println("<br>" + statusConnected.getId());
            }
            response.getWriter().println("<br>");
        }
        response.getWriter().println("<script>");

        // !!!! Hier gehts weiter: Ausprobieren ob das funktioniert !!!!
        response.getWriter().println("function clickOnMarker(e){\n" +
                "console.log(e)\n" +
                "}");

        response.getWriter().println(getJScriptMap());

        int counter = 0;
        for (CSStammdaten stammdaten : stammdatenMap) {
            final CSStatusConnected statusConnected = getStatusConnected(receiveConnected, stammdaten.getId());
            final String color = calcColor(stammdaten, statusConnected);
            Instant lastContactValue = lastContact.get(stammdaten.getId());
            String bezeichnung = stammdaten.getName() + " [" + stammdaten.getId() + "]";
            bezeichnung += getStatusString(statusConnected);
            bezeichnung += " Letzter Kontakt: " + DateTimeHelper.humanReadable(lastContactValue);
            bezeichnung += " <a href=cs?cs=" + stammdaten.getId() + ">Details</a>";
            response.getWriter().println(getJScriptCircle(counter, stammdaten.getCoordinates(), color, bezeichnung));
            counter++;
        }

        response.getWriter().println("</script>");
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }

    private String getStatusString(CSStatusConnected statusConnected) {
        if (statusConnected != null) {
            return " Status: " + statusConnected.getCPConnectorStatus().toString() +
                    " / " + statusConnected.getCPChargingState().toString();
        }
        return " Status: Nicht Verbunden"; // A1
    }

    private String calcColor(CSStammdaten stammdaten, CSStatusConnected statusConnected) {

        if (stammdaten == null) throw new IllegalArgumentException("Stammdaten darf nicht leer sein");

        final String result;
        final Instant lastContactValue = lastContact.get(stammdaten.getId());
        if (lastContactValue == null) {
            result = RED;
        } else {
            boolean lastContactOverLimit = LastContactHelper.isLastContactOverLimit(LAST_CONTACT_OK_IN_SECONDS, lastContactValue);
            if (lastContactOverLimit) {
                result = RED;
            } else {
                if (statusConnected == null) {
                    result = YELLOW_LIGHT;
                } else {
                    if (ConnectorStatusEnum.Faulted.name().equalsIgnoreCase(str(statusConnected.getCPConnectorStatus()))) {
                        result = YELLOW_DARK;
                    } else if (matchConnectorStatus(ConnectorStatusEnum.Available, str(statusConnected.getCPConnectorStatus())) &&
                            matchChargingState(ChargingStateEnum.Idle, str(statusConnected.getCPChargingState()))) {
                        result = BLUE;
                    } else if (matchConnectorStatus(ConnectorStatusEnum.Occupied, str(statusConnected.getCPConnectorStatus())) &&
                            (!matchChargingState(ChargingStateEnum.Charging, str(statusConnected.getCPChargingState())))) {
                        result = GREEN_DARK;
                    } else if (matchConnectorStatus(ConnectorStatusEnum.Occupied, str(statusConnected.getCPConnectorStatus())) &&
                            (matchChargingState(ChargingStateEnum.Charging, str(statusConnected.getCPChargingState())))) {
                        result = GREEN_LIGHT;
                    } else {
                        throw new IllegalStateException("stammdaten=" + stammdaten + ", statusConnected=" + statusConnected);
                    }
                }
            }
        }
        return result;
    }

    private boolean matchConnectorStatus(ConnectorStatusEnum wanted, String value) {
        return wanted.name().equalsIgnoreCase(value);
    }

    private boolean matchChargingState(ChargingStateEnum wanted, String value) {
        return wanted.name().equalsIgnoreCase(value);
    }

    private String getJScriptMap() {
        return "    var map = L.map('map').setView([46.80,8.40], 8);\n" +
                "    L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}', {\n" +
                "        attribution: 'Map data &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors, Imagery © <a href=\"https://www.mapbox.com/\">Mapbox</a>',\n" +
                "        maxZoom: 18,\n" +
                "        id: 'mapbox/streets-v11',\n" +
                "        tileSize: 512,\n" +
                "        zoomOffset: -1,\n" +
                "        accessToken: 'pk.eyJ1Ijoibm9pcHNlciIsImEiOiJjbDAzdDI1am4wOWF5M2JyMWpwb3V6bmFwIn0.W81R3AQfaw4RANyOlij9BQ'\n" +
                "    }).addTo(map);\n" +
                "\n";
    }

    private String getJScriptCircle(int counter, String coordinates, String color, String bezeichnung) {
        String result =
                "    var circle" + counter + " = L.circleMarker([" + coordinates + "], {\n" +
                "        color: 'black',\n" +
                //"        color: '" + color + "',\n" +
                "        fillColor: '" + color + "',\n" +
                "        fillOpacity: 0.5,\n" +
                "        radius: 7\n" +
                "    }).addTo(map);\n" +
                "    circle" + counter + ".bindPopup(\"" + bezeichnung + "\");";
        System.out.println("getJScriptCircle: " + result);
        return result;
    }

    private String getJScriptCircle2(int counter, String coordinates, String color, String bezeichnung) {
        String result =
                "    var circle" + counter + " = L.circleMarker([" + coordinates + "], {\n" +
                        "        color: 'black',\n" +
                        //"        color: '" + color + "',\n" +
                        "        fillColor: '" + color + "',\n" +
                        "        fillOpacity: 0.5,\n" +
                        "        radius: 7\n" +
                        "    }).addTo(map);\n" +
                        "    circle" + counter + ".on('click', clickOnMarker);" +
                        "    circle" + counter + ".bindPopup(\"" + bezeichnung + "\");";
        System.out.println("getJScriptCircle: " + result);
        return result;
    }

    private String getHead() {
        return "<head>\n" +
                "    <title>MobOCPP UI</title>\n" +
                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\"\n" +
                "          integrity=\"sha512-xodZBNTC5n17Xt2atTPuE1HxjVMSvLVW9ocqUKLsCC5CXdbqCmblAshOMAS6/keqq/sMZMZ19scR4PsZChSR7A==\"\n" +
                "          crossorigin=\"\"/>\n" +
                "    <script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"\n" +
                "            integrity=\"sha512-XQoYMqMTK8LvdxXYG3nZ448hOEQiglfqkJs1NOQV44cWnUrBc8PkAOcXy20w0vlaXaVUearIOBhiXZ5V3ynxwA==\"\n" +
                "            crossorigin=\"\">\n" +
                "    </script>\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">\n" +
                "</head>";
    }

    private static String str(CharSequence c) {
        if (c != null) {
            return String.valueOf(c);
        }
        return null;
    }

    private static class CSStammdaten {
        private final String id;
        private final String coordinates;
        private final String name;

        public static CSStammdaten of(String id, String coordinates, String name) {
            return new CSStammdaten(id, coordinates, name);
        }

        private CSStammdaten(String id, String coordinates, String name) {
            this.id = id;
            this.coordinates = coordinates;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getCoordinates() {
            return coordinates;
        }

        public String getName() {
            return name;
        }
    }
}
