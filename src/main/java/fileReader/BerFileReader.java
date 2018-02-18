package fileReader;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

public class BerFileReader {

    public BerFileReader(){}

    public Optional<ASN1Primitive> readFile(String fullPath) throws RuntimeException, IOException {

        Optional<ASN1Primitive> asn1Primitive = Optional.empty();
        ASN1InputStream ais;
        ais = new ASN1InputStream(new FileInputStream(new File(fullPath)));

        while (ais.available() > 0) {
            asn1Primitive = Optional.ofNullable(ais.readObject());
        }

        ais.close();
        return asn1Primitive;
    }
}
