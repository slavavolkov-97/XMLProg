import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Catalog {
   private final String uuid;
   private Timestamp deliveryDate;
   private String company;
   private final ArrayList<Plant> plants;
   public Catalog(String uuid, String deliveryDate, String company){
       this.uuid = uuid;
       try {
           Date date = new SimpleDateFormat("dd.MM.yyyy").parse(deliveryDate);
           this.deliveryDate = new Timestamp(date.getTime());
           this.company = company;
       } catch(ParseException e) {
           e.printStackTrace();
       }
       this.company = company;
       this.plants = new ArrayList<>();
   }

    public void addPlant(String common, String botanical, String zone, String light, String price, String availability) {
        this.plants.add(new Plant(common, botanical, zone, light, price, availability));
    }

    public ArrayList<Plant> getPlants() {
        return plants;
    }

    public String getUuid(){ return uuid; }
    public Timestamp getDeliveryDate(){
       return deliveryDate;
   }
    public String getCompany(){
       return company;
   }
}
