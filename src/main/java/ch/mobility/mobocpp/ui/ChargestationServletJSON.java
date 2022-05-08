package ch.mobility.mobocpp.ui;

import ch.mobility.ocpp2mob.CPStatusHistoryEntry;
import ch.mobility.ocpp2mob.CSStatusForIdResponse;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/csJ")
public class ChargestationServletJSON extends HttpServlet {

//    private AvroProducer avroProducer = null;
//    private AvroConsumer avroConsumer = null;

    private AvroProducer getAvroProducer() {
        return AvroProducer.get();
    }

    private AvroConsumer getAvroConsumer() {
        return AvroConsumer.get();
    }

    private Gson gson = new Gson();

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

    public static class Error {

        private String error;

        public Error(String error) {
            this.error = error;
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");

        final String[] csArray = request.getParameterMap().get("cs");
        if (csArray != null && csArray.length > 0) {
            String csId = csArray[0];

            getAvroProducer().requestStatusForId(csId, null, null);
            List<CSStatusForIdResponse> receiveDetail = getAvroConsumer().receive(CSStatusForIdResponse.class, 3000, 1);
            if (receiveDetail.size() != 1) {
                Error error = new Error("Error fetching data for Chargingstation " + csId);
                String json = this.gson.toJson(error);
                System.out.println("Sende Error: " + json);
                out.print(json);
            } else {
                final CSStatusForIdResponse csStatusForIdResponse = receiveDetail.get(0);
//                final CSStatusDetail statusDetail = csStatusForIdResponse.getStatus();
                String json = this.gson.toJson(csStatusForIdResponse);
                System.out.println("Sende CSStatusForIdResponse: " + json);
                out.print(json);
            }
        } else {
            Error error = new Error("Parameter 'cs' not found");
            String json = this.gson.toJson(error);
            out.print(json);
        }
        out.flush();
    }

    private String getErrorString(CPStatusHistoryEntry historyEntry) {
        if (!str(historyEntry.getErrorCode()).equalsIgnoreCase("NoError")) {
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

    private static String str(CharSequence c) {
        if (c != null) {
            return String.valueOf(c);
        }
        return null;
    }
}
