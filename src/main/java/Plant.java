import java.math.BigDecimal;

public class Plant {
    private final String common;
    private final String botanical;
    private final String zone;
    private final String light;
    private final Integer availability;
    private final BigDecimal price;
    public Plant(String common, String botanical, String zone, String light, String price, String availability) {
        this.common = common;
        this.botanical = botanical;
        this.zone = zone;
        this.light = light;
        this.price = new BigDecimal(price.replace("$",""));
        this.availability = Integer.valueOf(availability);
    }

    public String getCommon() {
        return common;
    }
    public String getBotanical() {
        return botanical;
    }
    public String getZone() {
        return zone;
    }
    public String getLight() {
        return light;
    }
    public BigDecimal getPrice(){
        return price;
    }
    public Integer getAvailability() {
        return availability;
    }
}
