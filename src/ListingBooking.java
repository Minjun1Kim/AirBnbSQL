import java.text.SimpleDateFormat;
import java.util.Date;

public class ListingBooking {
    private int bookingId;
    private int listingId;
    private String type;
    private double latitude;
    private double longitude;
    private String address;
    private String postalCode;
    private String city;
    private String country;
    private double price;
    private Date bookingStart;
    private Date bookingEnd;

    public ListingBooking(int bookingId, int listingId, String type, double latitude, double longitude, String address,
                          String postalCode, String city, String country, double price, Date bookingStart, Date bookingEnd) {
        this.bookingId = bookingId;
        this.listingId = listingId;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.price = price;
        this.bookingStart = bookingStart;
        this.bookingEnd = bookingEnd;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getListingId() {
        return listingId;
    }

    public void setListingId(int listingId) {
        this.listingId = listingId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getBookingStart() {
        return bookingStart;
    }

    public void setBookingStart(Date bookingStart) {
        this.bookingStart = bookingStart;
    }

    public Date getBookingEnd() {
        return bookingEnd;
    }

    public void setBookingEnd(Date bookingEnd) {
        this.bookingEnd = bookingEnd;
    }
    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String start = dateFormat.format(bookingStart);
        String end = dateFormat.format(bookingEnd);

        return String.format("| %-10s | %-9s  | %-13.6f | %-14.6f | %-28s | %-10s| %-14s | %-20s    | $%5.2f    | %s     to  %-10s   |",
                listingId, type, latitude, longitude, address, postalCode, city, country, price, start, end);
    }
}
