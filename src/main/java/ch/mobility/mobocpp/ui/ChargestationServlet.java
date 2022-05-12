package ch.mobility.mobocpp.ui;

import ch.mobility.mobocpp.kafka.AvroProsumer;
import ch.mobility.ocpp2mob.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/cs")
public class ChargestationServlet extends HttpServlet {

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

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html;");
        response.getWriter().println("<html>");

//        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
//            String value = "";
//            for (String s : entry.getValue()) {
//                value += s + ",";
//            }
//            response.getWriter().println("Entry '" + entry.getKey() + "': '" + value + "'");
//        }
        final String[] csArray = request.getParameterMap().get("cs");
        if (csArray != null && csArray.length > 0) {
            String csId = csArray[0];

            response.getWriter().println(getHead(csId));
            response.getWriter().println("<body>");

            final List<CSStatusForIdResponse> receiveDetail = getAvroProsumer().getStatusForId(csId, null, null);
            if (receiveDetail.size() != 1) {
                response.getWriter().println("Error fetching data for Chargingstation " + csId);
            } else {
                final CSStatusForIdResponse csStatusForIdResponse = receiveDetail.get(0);
                final CSStatusDetail statusDetail = csStatusForIdResponse.getStatus();
                response.getWriter().println("<br>Id: " + statusDetail.getId());
                response.getWriter().println("<br>BackendStatus: " + statusDetail.getBackendStatus());
                response.getWriter().println("<br>Vendor: " + statusDetail.getVendor());
                response.getWriter().println("<br>Model: " + statusDetail.getModel());
                response.getWriter().println("<br>Firmwareversion: " + statusDetail.getFirmwareversion());
                response.getWriter().println("<br>OCPPVersion: " + statusDetail.getOCPPVersion());
                response.getWriter().println("<br>FirstContact: " + statusDetail.getFirstContact());
                response.getWriter().println("<br>LastContact: " + statusDetail.getLastContact());
                response.getWriter().println("<br>IPAddress: " + statusDetail.getIPAddress());
//                response.getWriter().println("<br>Chargepoints: " + statusDetail.getCPStatusList().size());
                response.getWriter().println("<hr>");
                for (CPStatus cpStatus : statusDetail.getCPStatusList()) {
                    if (cpStatus.getConnectorId() != 0) {
                        response.getWriter().println("<br>Chargepoint");
                        response.getWriter().println("<br>ConnectorId: " + cpStatus.getConnectorId());
                        response.getWriter().println("<br>ConnectorStatus: " + cpStatus.getConnectorStatus());
                        response.getWriter().println("<br>ChargingState: " + cpStatus.getChargingState());
                        response.getWriter().println("<br>CurrentChargingAmpere L1: " + cpStatus.getCurrentChargingAmpereL1());
                        response.getWriter().println("<br>CurrentChargingAmpere L2: " + cpStatus.getCurrentChargingAmpereL2());
                        response.getWriter().println("<br>CurrentChargingAmpere L3: " + cpStatus.getCurrentChargingAmpereL3());
                        response.getWriter().println("<br>CurrentChargedEnergy: " + cpStatus.getCurrentChargedEnergy());
                        response.getWriter().println("<br>ErrorCode: " + cpStatus.getErrorCode());
                        response.getWriter().println("<br>ErrorInfo: " + cpStatus.getErrorInfo());

                        if (!cpStatus.getCPStatusHistoryList().isEmpty()) {
                            response.getWriter().println("<br><br>Status History:");
                            for (CPStatusHistoryEntry historyEntry : cpStatus.getCPStatusHistoryList()) {
                                String error = getErrorString(historyEntry);
                                String line =
                                        historyEntry.getTimestamp() +
                                                ": " + historyEntry.getConnectorStatus() +
                                                " / " + historyEntry.getChargingState() +
                                                (error == null ? "" : ", " + error);
                                response.getWriter().println("<br>" + line);
                            }
                        }
                        if (!cpStatus.getCPTransactionHistoryList().isEmpty()) {
                            response.getWriter().println("<br><br>Transaction History:");
                            for (CPTransactionHistoryEntry historyEntry : cpStatus.getCPTransactionHistoryList()) {
                                String line =
                                        " Start: " + historyEntry.getStartTimestamp() +
                                                ", Stop: " + historyEntry.getStopTimestamp() +
                                                ", StartValue: " + historyEntry.getStartValue() +
                                                ", StopValue: " + historyEntry.getStopValue();
                                response.getWriter().println("<br>" + line);
                            }
                        }
                        response.getWriter().println("<hr>");
                    }
                }
            }
        } else {
            response.getWriter().println(getHead("Unknown"));
            response.getWriter().println("<body>");
        }

        response.getWriter().println("<br><a href=map>Back to map</a>");
        response.getWriter().println("<script>");
        response.getWriter().println("</script>");
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }

    private String getErrorString(CPStatusHistoryEntry historyEntry) {
        if (!historyEntry.getErrorCode().equalsIgnoreCase("NoError")) {
            return "Error: " + historyEntry.getErrorCode() + "=" + historyEntry.getErrorInfo();
        }
        return null;
    }

    private String getHead(String csId) {
        return "<head>\n" +
                "    <title>MobOCPP UI - Chargingstation " + csId + "</title>\n" +
//                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\"\n" +
//                "          integrity=\"sha512-xodZBNTC5n17Xt2atTPuE1HxjVMSvLVW9ocqUKLsCC5CXdbqCmblAshOMAS6/keqq/sMZMZ19scR4PsZChSR7A==\"\n" +
//                "          crossorigin=\"\"/>\n" +
//                "    <script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"\n" +
//                "            integrity=\"sha512-XQoYMqMTK8LvdxXYG3nZ448hOEQiglfqkJs1NOQV44cWnUrBc8PkAOcXy20w0vlaXaVUearIOBhiXZ5V3ynxwA==\"\n" +
//                "            crossorigin=\"\">\n" +
//                "    </script>\n" +
                "    <link rel=\"stylesheet\" href=\"mapstyle.css\">\n" +
                "</head>";
    }
}
