package parser;

import dataTypes.DATATYPES;
import org.bouncycastle.asn1.*;

import javax.json.Json;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class ASN1parser {

    private final Map<String, String> asn1Values;
    private final DateTimeFormatter formatter;
    private boolean areRequirementsMeet;
    private Pattern argMsisdnRegex;
    private Pattern argImsiRegex;

    public ASN1parser() {
        asn1Values = new HashMap<>();
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    public  String parseToJson(ASN1Primitive asn1Primitive, LocalDateTime argDate, String argCdrType,
                               Pattern argMsisdnRegex, Pattern argImsiRegex) {

        ASN1Sequence seq = (ASN1Sequence) asn1Primitive;
        areRequirementsMeet = true;
        this.argImsiRegex = argImsiRegex;
        this.argMsisdnRegex = argMsisdnRegex;

        for (ASN1Encodable aSeq : seq) {

            if (aSeq instanceof ASN1OctetString) {

                LocalDateTime berDate = createDateFromOctet(aSeq);
                if(isDateMeetReuirements(berDate, argDate)) {
                    insertAppropriateDate(berDate);
                }

            } else if (aSeq instanceof ASN1Enumerated) {

                BigInteger cdrTypeValue = ((ASN1Enumerated) aSeq).getValue();

                if(isCdrTypeMeetRequirements(argCdrType,cdrTypeValue)) {
                    insertAppropriateCdrType(cdrTypeValue);
                }


            } else if (aSeq instanceof ASN1TaggedObject) {

                if(isTagValueMeetRequirements(aSeq, calculateAscTagString((ASN1TaggedObject) aSeq))) {
                    insertAppropriateTagType(aSeq);
                }
            }

            if(!areRequirementsMeet)
                break;
        }

        return areRequirementsMeet ? Json
                .createObjectBuilder()
                .add(DATATYPES.IMSI.getName(), asn1Values.getOrDefault(DATATYPES.IMSI.getName(), "empty"))
                .add(DATATYPES.TYPE.getName(), asn1Values.getOrDefault(DATATYPES.TYPE.getName(), "empty"))
                .add(DATATYPES.MSISDN.getName(), asn1Values.getOrDefault(DATATYPES.MSISDN.getName(), "empty"))
                .add(DATATYPES.VLR.getName(), asn1Values.getOrDefault(DATATYPES.VLR.getName(), "empty"))
                .add(DATATYPES.TIMESTAMP.getName(), asn1Values.getOrDefault(DATATYPES.TIMESTAMP.getName(), "empty"))
                .build()
                .toString()
                : "not meet";

    }



    private boolean isTagValueMeetRequirements(ASN1Encodable aSeq, String value) {

        int tagNr = ((ASN1TaggedObject) aSeq).getTagNo();

        if (tagNr == DATATYPES.MSISDN.getNumberIdentifier()) {

            areRequirementsMeet = value.matches(argMsisdnRegex.toString());

        } else if (tagNr == DATATYPES.IMSI.getNumberIdentifier()) {

            areRequirementsMeet = value.matches(argImsiRegex.toString());

        }

        return areRequirementsMeet;
    }


    private void insertAppropriateTagType(ASN1Encodable aSeq) {
        int tagNr = ((ASN1TaggedObject) aSeq).getTagNo();

        if (tagNr == DATATYPES.MSISDN.getNumberIdentifier()) {

            asn1Values.put(DATATYPES.MSISDN.getName(),calculateAscTagString((ASN1TaggedObject) aSeq));

        } else if (tagNr == DATATYPES.IMSI.getNumberIdentifier()) {

            asn1Values.put(DATATYPES.IMSI.getName(), calculateAscTagString((ASN1TaggedObject) aSeq));

        } else if (tagNr == DATATYPES.VLR.getNumberIdentifier()) {

            asn1Values.put(DATATYPES.VLR.getName(), calculateAscTagString((ASN1TaggedObject) aSeq));

        }
    }

    private LocalDateTime createDateFromOctet(ASN1Encodable aSeq) {
        byte[] octets = ((ASN1OctetString) aSeq).getOctets();

        return Instant.ofEpochMilli(Long
                .parseLong(calculateAscString(octets, octets.length)))
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private void insertAppropriateDate(LocalDateTime berDate) {
        asn1Values.put(DATATYPES.TIMESTAMP.getName(), String.valueOf(formatter.format(berDate)));
    }

    private boolean isDateMeetReuirements(LocalDateTime berDate, LocalDateTime argDate) {
        areRequirementsMeet = argDate.isBefore(berDate) || argDate.isEqual(berDate);
        return areRequirementsMeet;
    }

    private void insertAppropriateCdrType(BigInteger asn1EnumValue) {

        if(compare(asn1EnumValue, DATATYPES.UPDATELOCATION)){

            asn1Values.put(DATATYPES.TYPE.getName(), String.valueOf(DATATYPES.UPDATELOCATION.getName()));
        }
        else if(compare(asn1EnumValue, DATATYPES.CANCELLOCATION)){

            asn1Values.put(DATATYPES.TYPE.getName(), String.valueOf(DATATYPES.CANCELLOCATION.getName()));
        }

    }

    private boolean compare(BigInteger asn1EnumValue, DATATYPES location) {
        return BigInteger.valueOf(location.getNumberIdentifier()).compareTo(asn1EnumValue) == 0;
    }

    private boolean isCdrTypeMeetRequirements(String argCdrType, BigInteger cdrTypeValue) {
        if (Objects.equals(argCdrType, DATATYPES.CANCELLOCATION.getName())) {
            areRequirementsMeet = BigInteger.valueOf(DATATYPES.CANCELLOCATION.getNumberIdentifier())
                    .compareTo(cdrTypeValue) == 0;
        } else
            areRequirementsMeet = Objects.equals(argCdrType, DATATYPES.UPDATELOCATION.getName())
                    && BigInteger.valueOf(DATATYPES.UPDATELOCATION.getNumberIdentifier()).compareTo(cdrTypeValue) == 0;
        return areRequirementsMeet;
    }

    private String calculateAscTagString(ASN1TaggedObject asn1Tag) {
        byte[] octets = ASN1OctetString.getInstance(asn1Tag, false).getOctets();
        return calculateAscString(octets, octets.length);
    }

    private String calculateAscString(byte[] bytes, int length) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i != length; ++i) {
            if (bytes[i] >= 32 && bytes[i] <= 126) {
                stringBuilder.append((char) bytes[i]);
            }
        }

        return stringBuilder.toString();
    }
}

