package ch.mobility.mobocpp.rs;

import ch.mobility.mobocpp.CarSoCSingleton;
import ch.mobility.mobocpp.rs.model.CarSoC;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;

@Path("carsoc/{vin}/{soc}/{chargestatus}")
public class CarSoCRS {

//    private Gson gson = new Gson();
    // carsoc/<vin>/<soc>/<chargestatus>
    // carsoc/<vin>?soc=XX&chargestatus=YY

//    @Context private ResourceInfo resourceInfo;

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
//    @Consumes(MediaType.APPLICATION_JSON)
    public void insert(
            @PathParam("vin") String vin,
            @PathParam("soc") String soc,
            @PathParam("chargestatus") String chargestatus
//            @Context HttpServletResponse servletResponse
    ) {
//        System.out.println("resourceInfo: " + resourceInfo);
//        System.out.println("servletResponse: " + servletResponse);
        try {
            CarSoC carSoC = new CarSoC(Instant.now(), vin, soc, chargestatus);
            CarSoCSingleton.getInstance().add(carSoC);
//            System.out.println("carSoC: " + carSoC);
        } catch (Exception e) {
            System.err.println(e.getMessage());
//            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public Hello getAll() {
//        Hello hello = new Hello("Hallo Andi .... " + Instant.now());
////        String json = this.gson.toJson(hello);
//        return hello;
//    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getAll() {
        return "Hallo Andi";
    }

    public static class Hello {
        private String hello;
        public Hello(String hello) {
            this.hello = hello;
        }
    }
}
