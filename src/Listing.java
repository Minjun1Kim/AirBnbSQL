public class Listing {
    private int listingId;
    private String type;
    private double latitude;
    private double longitude;
    private String address;
    private String postalCode;
    private String city;
    private String country;
    private String amenities;
    private double price;

    public Listing(int listingId, String type, double latitude, double longitude, String address, String postalCode, String city, String country, double price) {
        this.listingId = listingId;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Listing{" +
                "listingId=" + listingId +
                ", type='" + type + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", price=" + price +
                '}';
    }
}