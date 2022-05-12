package ch.mobility.mobocpp.ui;

import ch.mobility.mobocpp.kafka.AvroProsumer;
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
import java.util.*;

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
        stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntKeineVerbindungLetzerKontaktKleinerN, "47.4458578,9.1400634", "Raiffeisenbank", "9500", "Wil"));
        stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntKeineVerbindungLetzerKontaktGroesserN, "47.1442198,8.4349035", "Hauptsitz", "6343", "Rotkreuz"));
        stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtLadestationMeldetFehler, "46.5160055,6.6277126", "Bahnhof", "1003", "Lausanne"));
        stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtKeinFahrzeugAngeschlossen, "46.1726474,8.7994749", "SBB Locarno", "6600", "Locarno"));
        stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenNichtAmLaden, "46.8482,9.5311401", "Jochstrasse", "7000", "Chur"));
        stammdatenMap.add(CSStammdaten.of(FakeCSStatusConnected.KeyLadestationBekanntVerbindungBestehtFahrzeugAngeschlossenAmLaden, "47.3776673,8.5323237", "Europaallee", "8004", "Zurich"));

        stammdatenMap.add(CSStammdaten.of(KeyLadestationBekanntVerbindungBestehtA, "46.9585416,8.3640993", "Bahnhof", "6370", "Stans"));
        stammdatenMap.add(CSStammdaten.of(KeyLadestationBekanntVerbindungBestehtB, "46.8974001,8.2461243", "Bahnhof", "6060", "Sarnen"));
        stammdatenMap.add(CSStammdaten.of(KeyLadestationBekanntVerbindungBestehtC, "46.2937841,7.8794028", "Bahnhof", "3930", "Visp"));
    }

    private AvroProsumer getAvroProsumer() {
        return AvroProsumer.get();
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
        AvroProsumer.get().close();
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
                if (csStatusConnected.getId().equalsIgnoreCase(id)) {
                    return csStatusConnected;
                }
            }
        }
        return null;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        final List<CSStatusConnectedResponse> receiveConnected = getAvroProsumer().getStatusConnected();
        for (CSStatusConnectedResponse csStatusConnectedResponse : receiveConnected) {
            for (CSStatusConnected csStatusConnected : csStatusConnectedResponse.getCSStatusList()) {
                lastContact.put(csStatusConnected.getId(), DateTimeHelper.parse(csStatusConnected.getLastContact()));
//                getAvroProducer().requestStatusForId(str(csStatusConnected.getId()), null, 1);
//                List<CSStatusForIdResponse> receiveDetail = getAvroConsumer().receive(CSStatusForIdResponse.class, 10000, 1);
            }
        }

        receiveConnected.add(FakeCSStatusConnected.addFakeCS(lastContact));

        final List<CSStatusConnected> keineStammdaten = new ArrayList<>(); // A1
        for (CSStatusConnectedResponse csStatusConnectedResponse : receiveConnected) {
            for (CSStatusConnected csStatusConnected : csStatusConnectedResponse.getCSStatusList()) {
                final CSStammdaten stammdaten = getStammdaten(csStatusConnected.getId());
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

//        response.getWriter().println("<div id=\"csModal\" class=\"modal\">");
//        response.getWriter().println(" <div id=\"csModalContent\" class=\"modal-content\">");
//        response.getWriter().println("  <span class=\"close\">&times;</span>");
//        response.getWriter().println("  <p>Some text in the Modal..0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789</p>");
//        response.getWriter().println(" </div>");
//        response.getWriter().println("</div>");

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


//        response.getWriter().println("var modal = document.getElementById(\"csModal\");");
//        response.getWriter().println("var btn = document.getElementById(\"myBtn\");\n");
//        response.getWriter().println("var span = document.getElementsByClassName(\"close\")[0];");

//        response.getWriter().println("async function clickOnMarker(e){\n" +
//                "const response = await fetch('csJ?cs=JC310001');\n" +
//                "console.log('Response !!! ', response);\n" +
//                "console.log('Data !!! ', response.data);\n" +
//                "console.log('JSON !!! ', await response.json());\n" +
//                "  var modalContent = document.getElementById(\"csModalContent\");\n" +
//                "  modalContent.innerHTML = '0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789 changed1';\n" +
//                "  modal.style.display = \"block\";\n" +
//                "  map.display = \"none\";\n" +
//                "}");

//        response.getWriter().println("async function clickOnMarker2(value){\n" +
//                "const response = await fetch('csJ?cs=value');\n" +
//                "console.log('Response !!! ', response);\n" +
//                "console.log('Data !!! ', response.data);\n" +
//                "console.log('JSON !!! ', await response.json());\n" +
//                "  var modalContent = document.getElementById(\"csModalContent\");\n" +
//                "  modalContent.innerHTML = '0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789 changed1';\n" +
//                "  modal.style.display = \"block\";\n" +
//                "  map.display = \"none\";\n" +
//                "}");

        response.getWriter().println("function removeDetail(content) {\n");
        response.getWriter().println("  console.log('content: ', content);\n");
//        response.getWriter().println("  var result = content;\n");
        response.getWriter().println("  let hrPos = content.search(\"<hr>\");\n");
        response.getWriter().println("  console.log('hrPos', hrPos);\n");
        response.getWriter().println("  if (hrPos !== -1) {\n");
        response.getWriter().println("    console.log('hrPos ist nicht minus eins: ', content.substring(0, hrPos));\n");
        response.getWriter().println("    return content.substring(0, hrPos);\n");
        response.getWriter().println("  }\n");
        response.getWriter().println("  console.log('hrPos ist minus ein: ', content);\n");
        response.getWriter().println("  return content;\n");
        response.getWriter().println("}\n");

        response.getWriter().println("async function showCS(circle, value) {\n" +
//                "   console.log('showCS: ', circle, value);\n" +
                "   var popup = circle.getPopup();\n" +
                "   popup.setContent(removeDetail(popup.getContent()));\n" +

                "   const url = 'csJ?cs=' + value;\n" +
                "   const response = await fetch(url);\n" +
                "   const jsonResult = await response.json();\n" +
                "   console.log('JSONResult: ', jsonResult);\n" +

                "   if (jsonResult.Status !== undefined) {\n" +
                "    console.log('jsonResult.Status: ', jsonResult.Status);\n" +
                "    const oldContent = removeDetail(popup.getContent());\n" +
                "    let newContent = oldContent + '<hr>';\n" +
                "    newContent +=  'BackendStatus: ' + jsonResult.Status.BackendStatus + '<br>';\n" +
                "    newContent +=  'Vendor: ' + jsonResult.Status.Vendor + '<br>';\n" +
                "    newContent +=  'Model: ' + jsonResult.Status.Model + '<br>';\n" +
                "    newContent +=  'FW-Version: ' + jsonResult.Status.Firmwareversion + '<br>';\n" +
                "    newContent +=  'OCPPVersion: ' + jsonResult.Status.OCPPVersion + '<br>';\n" +
                "    newContent +=  'FirstContact: ' + jsonResult.Status.FirstContact + '<br>';\n" +
                "    newContent +=  'LastContact: ' + jsonResult.Status.LastContact + '<br>';\n" +
                "    newContent +=  'IPAddress: ' + jsonResult.Status.IPAddress + '<br>';\n" +
                "    newContent +=  '<br>';\n" +

                "    for (index = 1; index < jsonResult.Status.CPStatusList.length; index++) {" +
                "      newContent +=  'ConnectorId: ' + jsonResult.Status.CPStatusList[index].ConnectorId + '<br>';\n" +
                "      newContent +=  'ConnectorStatus: ' + jsonResult.Status.CPStatusList[index].ConnectorStatus + '<br>';\n" +
                "      newContent +=  'ChargingState: ' + jsonResult.Status.CPStatusList[index].ChargingState + '<br>';\n" +
                "      newContent +=  'CurrentChargedEnergy: ' + jsonResult.Status.CPStatusList[index].CurrentChargedEnergy + '<br>';\n" +
                "      newContent +=  'CurrentChargingAmpere L1: ' + jsonResult.Status.CPStatusList[index].CurrentChargingAmpereL1 + '<br>';\n" +
                "      newContent +=  'CurrentChargingAmpere L2: ' + jsonResult.Status.CPStatusList[index].CurrentChargingAmpereL2 + '<br>';\n" +
                "      newContent +=  'CurrentChargingAmpere L3: ' + jsonResult.Status.CPStatusList[index].CurrentChargingAmpereL3 + '<br>';\n" +
                "      newContent +=  'ErrorCode: ' + jsonResult.Status.CPStatusList[index].ErrorCode + '<br>';\n" +
                "      newContent +=  'ErrorInfo: ' + jsonResult.Status.CPStatusList[index].ErrorInfo + '<br><br>';\n" +
                "    }" +

                "    popup.setContent(newContent);" +
                "   }" +
                "   popup.update();" +

//                "  var modalContent = document.getElementById(\"csModalContent\");\n" +
//                "  modalContent.innerHTML = '0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789 changed1';\n" +
//                "  modal.style.display = \"block\";\n" +
//                "  map.display = \"none\";\n" +
                "}");

//        response.getWriter().println("span.onclick = function() {");
//        response.getWriter().println("  modal.style.display = \"none\";");
//        response.getWriter().println("  map.display = \"inline\";");
//        response.getWriter().println("}");

//        response.getWriter().println("window.onclick = function(event) {");
//        response.getWriter().println("  if (event.target == modal) {");
//        response.getWriter().println("    modal.style.display = \"none\";");
//        response.getWriter().println("    map.display = \"inline\";");
//        response.getWriter().println("  }");
//        response.getWriter().println("}");

        response.getWriter().println(getJScriptMap());

        int counter = 0;
        for (CSStammdaten stammdaten : stammdatenMap) {
            final CSStatusConnected statusConnected = getStatusConnected(receiveConnected, stammdaten.getId());
            final String color = calcColor(stammdaten, statusConnected);
            Instant lastContactValue = lastContact.get(stammdaten.getId());

            final String circleName = "circle" + counter;
            String popuptext = stammdaten.getName() + " [" + stammdaten.getId() + "]<br>";
            popuptext += getStatusString(statusConnected) + "<br>";
            popuptext += "Letzter Kontakt: " + DateTimeHelper.humanReadable(lastContactValue) + "<br>";
            popuptext += "<button onClick=\\\"showCS(" + circleName + ",'" + stammdaten.getId() + "')\\\">Details</button>";

            //            popuptext += " <a href=cs?cs=" + stammdaten.getId() + ">Details</a>";

//            response.getWriter().println(getJScriptCircleMitPopupname(counter, stammdaten.getCoordinates(), color, popupName));
            response.getWriter().println(getJScriptCircle(circleName, stammdaten.getCoordinates(), color, popuptext));
//            response.getWriter().println(getJScriptCircle(counter, stammdaten.getCoordinates(), color, stammdaten.getId()));
            counter++;
        }

        response.getWriter().println("</script>");
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }

    private String getStatusString(CSStatusConnected statusConnected) {
        if (statusConnected != null) {
            return "Status: " + statusConnected.getCPConnectorStatus().toString() +
                    " / " + statusConnected.getCPChargingState().toString();
        }
        return "Status: Nicht Verbunden"; // A1
    }

//    final static String RED = "#f03";          // F1
//    final static String YELLOW_LIGHT = "#ff9"; // W1
//    final static String YELLOW_DARK = "#ff0";  // W2
//    final static String BLUE = "#00f";         // O1
//    final static String GREEN_DARK = "#0f0";   // O2
//    final static String GREEN_LIGHT = "#0f9";  // O3

//    Blau: Online / Verfügbar, keine Auto angeschlossen
//    Dunkel-Grün: Online / Verfügbar, Auto angeschlossen, nicht am Laden
//    Dunkel-Grün blinkend: Online / Verfügbar, Auto angeschlossen und am Laden
//    Gelb: Problem, Hinweis zum Problem anzeigbar sofern vorhanden
//    Rot: Fehler, Hinweis zum Fehler anzeigbar sofern vorhanden

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
                    if (ConnectorStatusEnum.Faulted.name().equalsIgnoreCase(statusConnected.getCPConnectorStatus())) {
                        result = YELLOW_DARK;
                    } else if (ConnectorStatusEnum.Reserved.name().equalsIgnoreCase(statusConnected.getCPConnectorStatus())) {
                        result = YELLOW_DARK;
                    } else if (ConnectorStatusEnum.Unavailable.name().equalsIgnoreCase(statusConnected.getCPConnectorStatus())) {
                        result = YELLOW_DARK;
                    } else if (matchConnectorStatus(ConnectorStatusEnum.Available, statusConnected.getCPConnectorStatus())) {
                        result = BLUE;
                    } else if (ConnectorStatusEnum.Occupied.name().equalsIgnoreCase(statusConnected.getCPConnectorStatus())) {
                        if (matchChargingState(ChargingStateEnum.Charging, statusConnected.getCPChargingState())) {
                            result = GREEN_DARK;
                        } else {
                            result = GREEN_LIGHT;
                        }
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

    private String getJScriptCircle(String circleName, String coordinates, String color, String popuptext) {
        final String popupName = circleName + "popup";
        String result =
                "    var " + circleName + " = L.circleMarker([" + coordinates + "], {\n" +
                "        color: 'black',\n" +
                //"        color: '" + color + "',\n" +
                "        fillColor: '" + color + "',\n" +
                "        fillOpacity: 0.5,\n" +
                "        radius: 7\n" +
                "    }).addTo(map);\n" +
                "    const " + popupName + " = new L.Popup({ closeOnClick: true, maxWidth: 600, maxHeight: 800 })\n" +
                "     .setContent(\"" + popuptext + "\");\n" +
                //"    " + circleName + ".bindPopup(\"" + popuptext + "\");\n" +
                "    var layer = " + circleName + ".bindPopup(" + popupName + ");\n" +

                  "    layer.on('popupclose', (e) => {\n" +
                  "      console.log('layer.popupclose 1: ', e);\n" +
                  "      console.log('layer.popupclose 2: ', e.popup);\n" +
                  "      var content = removeDetail(e.popup.getContent());" +
                  "      e.popup.setContent(content);" +
                  "      e.popup.update();" +
                  "    });\n";

        //System.out.println("getJScriptCircle: " + result);
        return result;
    }

    private String getJScriptCircleMitPopupname(int counter, String coordinates, String color, String popupname) {
        String result =
                "    var circle" + counter + " = L.circleMarker([" + coordinates + "], {\n" +
                        "        color: 'black',\n" +
                        //"        color: '" + color + "',\n" +
                        "        fillColor: '" + color + "',\n" +
                        "        fillOpacity: 0.5,\n" +
                        "        radius: 7\n" +
                        "    }).addTo(map);\n" +
                        "    circle" + counter + ".bindPopup(\"" + popupname + "\");";
        //System.out.println("getJScriptCircle: " + result);
        return result;
    }

//    private String getJScriptCircle(int counter, String coordinates, String color, String id) {
//        String result =
//                "    var circle" + counter + " = L.circleMarker([" + coordinates + "], {\n" +
//                        "        color: 'black',\n" +
//                        //"        color: '" + color + "',\n" +
//                        "        fillColor: '" + color + "',\n" +
//                        "        fillOpacity: 0.5,\n" +
//                        "        radius: 7\n" +
//                        "    }).addTo(map);\n" +
//                        "    circle" + counter + ".on('click', clickOnMarker2('" + id + "'));";
//                        //+ "    circle" + counter + ".bindPopup(\"" + bezeichnung + "\");";
//        System.out.println("getJScriptCircle: " + result);
//        return result;
//    }

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
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"mapstyle.css\">\n" +
                "</head>";
    }
}
