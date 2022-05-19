package ch.mobility.mobocpp.ui;

import ch.mobility.mobocpp.kafka.AvroProsumer;
import ch.mobility.ocpp2mob.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@WebServlet("/detail")
public class DetailServlet extends HttpServlet {

    private static String PARAM_ID = "id";
    private static String NO_ERROR = "NoError";
    private static String NO_INFO = "Status Update";
//    private String PARAM_CONNECTOR_ID = "connectorId";

    private static String UNBEKANNT = "Unbekannt";

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

    private void checkParams(HttpServletRequest request) {
//        if (!request.getParameterMap().containsKey(PARAM_ID) || !request.getParameterMap().containsKey(PARAM_CONNECTOR_ID)) {
//            throw new IllegalArgumentException("Parameter " + PARAM_ID + " and " + PARAM_CONNECTOR_ID + " expected." +
//                    " Received: " + request.getParameterMap().keySet());
//        }
        if (!request.getParameterMap().containsKey(PARAM_ID)) {
            throw new IllegalArgumentException("Parameter " + PARAM_ID + " expected." +
                    " Received: " + request.getParameterMap().keySet());
        }
    }

    private String getStandardInfolabelText() {
        return "Letzte Aktualisierung: " + DateTimeHelper.humanReadable(Instant.now());
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        checkParams(request);
        final String id = request.getParameter(PARAM_ID);
//        final Integer connectorId = Integer.valueOf(request.getParameter(PARAM_CONNECTOR_ID));
        final List<CSStatusForIdResponse> receivedStatusForIdResponse = getAvroProsumer().getStatusForId(id, null, Integer.valueOf(5));
        if (receivedStatusForIdResponse.size() > 1) {
            throw new IllegalStateException("Expected only one Response, received: " + receivedStatusForIdResponse.size());
        }
        final CSStatusForIdResponse csStatusForIdResponse = receivedStatusForIdResponse.isEmpty() ? null : receivedStatusForIdResponse.get(0);

        response.setContentType("text/html;");
        response.getWriter().println("<!DOCTYPE html>");
        response.getWriter().println("<html>");
        response.getWriter().println(getHead());
        response.getWriter().println("<body>");
        response.getWriter().println("<script>");
        response.getWriter().println(getJS());
        response.getWriter().println("</script>");
        response.getWriter().println(" <div class=\"top\">");
        response.getWriter().println("   <button class=\"button\" onClick=\"window.location.href = '/list';\"> < Zur&uuml;ck zur &Uuml;bersicht</button>");
        response.getWriter().println(" </div>");
        response.getWriter().println(" <div class=\"maincontrol\">");
        response.getWriter().println("   <button class=\"button\" onClick=\"doAction('reset','" + id + "')\">Neustart</button>");
        response.getWriter().println("   <button class=\"button\" onClick=\"doAction('unlock','" + id + "')\">Kabel freigeben</button>");
        response.getWriter().println("   <button class=\"button\" onClick=\"doAction('triggerstatus','" + id + "')\">Statusupdate anfordern</button>");
        response.getWriter().println("   <div class=\"dropdown\">");
        response.getWriter().println("     <button class=\"dropdownbutton\">Ladestrom setzen</button>");
        response.getWriter().println("     <div class=\"dropdown-content\">");
        response.getWriter().println("       <label class=\"dropdown-label\" onClick=\"doCurrent('0','" + id + "')\">Suspend (0A)</label>");
        response.getWriter().println("       <label class=\"dropdown-label\" onClick=\"doCurrent('6','" + id + "')\">6 Ampere</label>");
        response.getWriter().println("       <label class=\"dropdown-label\" onClick=\"doCurrent('8','" + id + "')\">8 Ampere</label>");
        response.getWriter().println("       <label class=\"dropdown-label\" onClick=\"doCurrent('10','" + id + "')\">10 Ampere</label>");
        response.getWriter().println("       <label class=\"dropdown-label\" onClick=\"doCurrent('12','" + id + "')\">12 Ampere</label>");
        response.getWriter().println("       <label class=\"dropdown-label\" onClick=\"doCurrent('14','" + id + "')\">14 Ampere</label>");
        response.getWriter().println("       <label class=\"dropdown-label\" onClick=\"doCurrent('16','" + id + "')\">16 Ampere</label>");
        response.getWriter().println("     </div>");
        response.getWriter().println("   </div>");
        //        response.getWriter().println("    <button class=\"button\">Ladevorgang...</button>");

        response.getWriter().println("   <div class=\"dropdown\">");
        response.getWriter().println("     <button class=\"dropdownbutton\">Ladevorgang...</button>");
        response.getWriter().println("     <div class=\"dropdown-content\">");
        response.getWriter().println("       <label class=\"dropdown-label\" onClick=\"doAction('start','" + id + "')\">Starten</label>");
        response.getWriter().println("       <label class=\"dropdown-label\" onClick=\"doAction('stop','" + id + "')\">Stoppen</label>");
        response.getWriter().println("     </div>");
        response.getWriter().println("   </div>");

        response.getWriter().println("   <button class=\"button\" onClick=\"reload()\">Ansicht Aktualisieren</button>");
        response.getWriter().println("   <label class=\"infolabel\" id=\"infolabel\">" + getStandardInfolabelText() + "</label>");
        response.getWriter().println(" </div>");
        response.getWriter().println(" <div class=\"main\">");
        response.getWriter().println("   <div class=\"mainleft\">");
        response.getWriter().println(getMainLeft(id, csStatusForIdResponse));
        response.getWriter().println("   </div>");
        response.getWriter().println("   <div class=\"mainright\">");
        response.getWriter().println(getMainRight(csStatusForIdResponse));
        response.getWriter().println("   </div>");
        response.getWriter().println(" </div>");
        response.getWriter().println(" <div class=\"bottom\">");
        response.getWriter().println(getBottom(csStatusForIdResponse));
        response.getWriter().println(" </div>");
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }

    private String getHead() {
        return "<head>\n" +
                "    <title>MobOCPP UI</title>\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"detailstyle.css\">\n" +
                "</head>";
    }

    private String getJS() {
        return "async function doAction(actiontyp, cs) {\n" +
               "   console.log('doAction: ', actiontyp, cs);\n" +
                "  const url = 'action?actiontyp=' + actiontyp + '&cs=' + cs;\n" +
                "  const response = await fetch(url);\n" +
                "  const jsonResult = await response.json();\n" +
                "  console.log('JSONResult: ', jsonResult);\n" +
                "  if (jsonResult.status !== undefined) {\n" +
                "    var infolabel = document.getElementById(\"infolabel\");\n" +
                "    infolabel.innerText = jsonResult.status;\n" +
                "  }" +
               "}\n" +
                "async function doCurrent(limit, cs) {\n" +
                "  console.log('doCurrent: ', limit, cs);\n" +
                "  const url = 'action?actiontyp=current&limit=' + limit + '&cs=' + cs;\n" +
                "  const response = await fetch(url);\n" +
                "  const jsonResult = await response.json();\n" +
                "  console.log('JSONResult: ', jsonResult);\n" +
                "  if (jsonResult.status !== undefined) {\n" +
                "    var infolabel = document.getElementById(\"infolabel\");\n" +
                "    infolabel.innerText = jsonResult.status;\n" +
                "  }" +
                "}\n" +
                "function collapsibleStatusHistory() {\n" +
                "  var history = document.getElementById(\"statushistorycontent\");\n" +
                "  if (history.style.display === \"block\") {\n" +
                "    history.style.display = \"none\";\n" +
                "  } else {\n" +
                "    history.style.display = \"block\";\n" +
                "  }\n" +
                "}\n" +
                "function collapsibleTransHistory() {\n" +
                "  var history = document.getElementById(\"transhistorycontent\");\n" +
                "  if (history.style.display === \"block\") {\n" +
                "    history.style.display = \"none\";\n" +
                "  } else {\n" +
                "    history.style.display = \"block\";\n" +
                "  }\n" +
                "}\n" +
               "function reload() {\n" +
               "  window.location.reload();\n" +
               "}\n";
    }

    private String addLine(String key, int value) {
        return addLine(key, String.valueOf(value));
    }

    private String addLine(String key, String value) {
        if (value == null) {
            return key + ": " + UNBEKANNT + "<br>";
        }
        return key + ": " + value + "<br>";
    }

    private String getMainLeft(String id, CSStatusForIdResponse csStatusForIdResponse) {
        String result = "Keine Daten empfangen f&uuml;r " + id;
        if (csStatusForIdResponse != null) {
            if (csStatusForIdResponse.getResponseInfo().getError() != null) {
                result = addLine("Error", csStatusForIdResponse.getResponseInfo().getError());
            } else {
                final CSStatusDetail status = csStatusForIdResponse.getStatus();
                result = addLine("<b>Kennung</b>", status.getId());
                result+= addLine("Backendstatus", status.getBackendStatus().toString());
                result+= addLine("Vendor", status.getVendor());
                result+= addLine("Model", status.getModel());
                result+= addLine("FW-Version", status.getFirmwareversion());
                result+= addLine("OCPP-Version", status.getOCPPVersion());
                result+= addLine("First contact", DateTimeHelper.humanReadable(DateTimeHelper.parse(status.getFirstContact())));
                result+= addLine("Last contact", DateTimeHelper.humanReadable(DateTimeHelper.parse(status.getLastContact())));
                result+= addLine("CS IP Address", status.getIPAddress());
                result+= addLine("Backend IP Adress", csStatusForIdResponse.getResponseInfo().getOCPPBackendId());
            }
        }
        return result;
    }

    private String getMainRight(CSStatusForIdResponse csStatusForIdResponse) {
        String result = "";
        if (csStatusForIdResponse != null) {
            final CSStatusDetail status = csStatusForIdResponse.getStatus();
            for (CPStatus cpStatus : status.getCPStatusList()) {
                if (cpStatus.getConnectorId() != 0) {
                    result += addLine("<b>Connector</b>", cpStatus.getConnectorId());
                    result += addLine("Connector Status", cpStatus.getConnectorStatus().name());
                    result += addLine("Charging State", (cpStatus.getChargingState() == null ? "" : cpStatus.getChargingState().name()));
                    result += addLine("Current charged energy", cpStatus.getCurrentChargedEnergy());
                    result += addLine("Current charging Ampere L1", cpStatus.getCurrentChargingAmpereL1());
                    result += addLine("Current charging Ampere L2", cpStatus.getCurrentChargingAmpereL2());
                    result += addLine("Current charging Ampere L3", cpStatus.getCurrentChargingAmpereL3());
                    if (NO_ERROR.equals(cpStatus.getErrorCode())) {
                        if (!NO_INFO.equals(cpStatus.getErrorInfo())) {
                            result += addLine("Information", cpStatus.getErrorInfo());
                        }
                    } else {
                        result += addLine("Error code", cpStatus.getErrorCode());
                        result += addLine("Error Information", cpStatus.getErrorInfo());
                    }
                }
            }
        }
        return result;
    }

    private String getTD(Integer value) {
        if (value != null) {
            return getTD(String.valueOf(value));
        }
        return getTD(UNBEKANNT);
    }

    private String getTD(int value) {
        return getTD(String.valueOf(value));
    }

    private String getTD(String value) {
        return "    <td>" + value + "</td>";
    }

    private String getBottom(CSStatusForIdResponse csStatusForIdResponse) {
        String result = "";
        if (csStatusForIdResponse != null) {
            final CSStatusDetail status = csStatusForIdResponse.getStatus();

            // Statushistorie
            result += "<div class=\"statushistory\">";
            result += " <div class=\"statushistoryhead\">";
            result += " <button class=\"button\" onClick=\"collapsibleStatusHistory()\">Zeige Statushistorie</button></div>";
            result += " <div class=\"statushistorycontent\" id=\"statushistorycontent\">";
            for (CPStatus cpStatus : status.getCPStatusList()) {
                if (cpStatus.getConnectorId() != 0) {
                    if (!cpStatus.getCPStatusHistoryList().isEmpty()) {
                        result += " <table id=\"statushistorytable\">\n" +
                                " <thead>\n" +
                                "   <tr>\n" +
                                "     <th>Zeitstempel</th>\n" +
                                "     <th>Connector Status</th>\n" +
                                "     <th>Charging State</th>\n" +
                                "     <th>Error code</th>\n" +
                                "     <th>Error info</th>\n" +
                                "  </tr>\n" +
                                "  </thead>\n" +
                                "  <tbody>\n";
                        for (CPStatusHistoryEntry hentry : cpStatus.getCPStatusHistoryList()) {
                            result += "   <tr>\n";
                            result += getTD(DateTimeHelper.humanReadable(DateTimeHelper.parse(hentry.getTimestamp())));
                            result += getTD(hentry.getConnectorStatus().name());
                            result += getTD((hentry.getChargingState() == null ? "": hentry.getChargingState().name()));
                            result += getTD((NO_ERROR.equals(hentry.getErrorCode()) ? "" : hentry.getErrorCode()));
                            result += getTD((NO_INFO.equals(hentry.getErrorInfo()) ? "" : hentry.getErrorInfo()));
                            result += "   </tr>\n";
                        }
                        result += " </tbody>\n" +
                                " </table>";
                    }
                }
            }
            result += " </div>";
            result += "</div>";

            // Transaktionshistorie
            result += "<div class=\"transhistory\">";
            result += " <div class=\"transhistoryhead\">";
            result += " <button class=\"button\" onClick=\"collapsibleTransHistory()\">Zeige Ladehistorie</button></div>";
            result += " <div class=\"transhistorycontent\" id=\"transhistorycontent\">";
            for (CPStatus cpStatus : status.getCPStatusList()) {
                if (cpStatus.getConnectorId() != 0) {
                    if (!cpStatus.getCPTransactionHistoryList().isEmpty()) {
                        result += "<table id=\"transhistorytable\">\n" +
                                " <thead>\n" +
                                "   <tr>\n" +
                                "     <th>Start</th>\n" +
                                "     <th>Stop</th>\n" +
                                "     <th>Startwert</th>\n" +
                                "     <th>Stopwert</th>\n" +
                                "     <th>Total</th>\n" +
                                "  </tr>\n" +
                                " </thead>\n" +
                                " <tbody>\n";
                        for (CPTransactionHistoryEntry tentry : cpStatus.getCPTransactionHistoryList()) {
                            String total = UNBEKANNT;
                            if (tentry.getStopValue() != null) {
                                total = String.valueOf(tentry.getStopValue().intValue() - tentry.getStartValue());
                            }
                            result += "   <tr>\n";
                            result += getTD(DateTimeHelper.humanReadable(DateTimeHelper.parse(tentry.getStartTimestamp())));
                            result += getTD(DateTimeHelper.humanReadable(DateTimeHelper.parse(tentry.getStopTimestamp())));
                            result += getTD(tentry.getStartValue());
                            result += getTD(tentry.getStopValue());
                            result += getTD(total);
                            result += "   </tr>\n";
                        }
                        result += " </tbody>\n" +
                                "</table>";
                    }
                }
            }
            result += " </div>";
            result += "</div>";
        }
        return result;
    }
}
