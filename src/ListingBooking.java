public class ListsBooking {
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
    private String bookingStart;
    private String bookingEnd;

    public ListingBooking(int bookingId, int listingId, String type, double latitude, double longitude, String address,
                          String postalCode, String city, String country, double price, String bookingStart, String bookingEnd) {
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

    public int getListingId() {
        return listingId;
    }

    public String getType() {
        return type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public double getPrice() {
        return price;
    }

    public String getBookingStart() {
        return bookingStart;
    }

    public String getBookingEnd() {
        return bookingEnd;
    }
}
