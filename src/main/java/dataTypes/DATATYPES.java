package dataTypes;

public enum DATATYPES {
    UPDATELOCATION(2, "updateLocation"),
    CANCELLOCATION(3, "cancelLocation"),
    MSISDN(0, "msisdn"),
    IMSI(1, "imsi"),
    VLR(2, "vlr"),
    TIMESTAMP(-1, "timestamp"),
    TYPE(-1, "type");

    private final int typeNumber;
    private final String typeString;

    DATATYPES(int typeNumber, String typeString) {
        this.typeNumber = typeNumber;
        this.typeString = typeString;
    }

    public int getNumberIdentifier() {
        return typeNumber;
    }

    public String getName() {
        return typeString;
    }
}

