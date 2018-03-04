import fileReader.BerFileReader;
import org.bouncycastle.asn1.ASN1Primitive;
import parser.ASN1parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Main {

    private static boolean isProgramArgsValid =true;
    private static String inputDir;
    private static LocalDateTime date;
    private static String cdrType;
    private static Pattern msisdnRegex;
    private static Pattern imsiRegex;
    private static final int numberOfProgramArgs = 5;
    private static final String EMPTY_STRING = "";

    public static void main(String[] argsProgram) {

        List<String> args = Arrays
                .stream(argsProgram)
                .map(Main::getRidOfApostropheOnStartAndEnd)
                .collect(toList());

        getVariablesFromProgramArgs(args);

        if(isProgramArgsValid) {
            BerFileReader berFileReader = new BerFileReader();
            ASN1parser asn1parser = new ASN1parser();
            try (Stream<Path> paths = Files.walk(Paths.get(inputDir))){
                paths
                        .filter(Files::isRegularFile)
                        .forEach(fullPath -> {
                            try {
                                berFileReader
                                        .readFile(fullPath.toString())
                                        .ifPresentOrElse(asn1Primitive ->
                                                    printIfCDR_HLRmeetRequirements(asn1Primitive, asn1parser),
                                                 (() -> System.err.println("Empty file!")));
                            } catch (IOException e) {
                                System.out.println(fullPath.toString());
                                System.err.println("Wrong path");
                            }
                        });
            } catch (IOException e) {
                System.err.println("Wrong path");
            }
        }

    }

    private static void printIfCDR_HLRmeetRequirements(ASN1Primitive asn1Primitive, ASN1parser asn1parser) {

        String CDR_HLR_JSON = asn1parser.parseToJson(asn1Primitive, date, cdrType, msisdnRegex, imsiRegex);

        if(!Objects.equals(CDR_HLR_JSON, "not meet")){ //awful
            System.out.println(CDR_HLR_JSON);
        }
    }

    private static String getRidOfApostropheOnStartAndEnd(String arg) {
        return arg.replaceAll("^\\'|\\'$", EMPTY_STRING);
    }


    private static void getVariablesFromProgramArgs(List<String> args) {

        if(args.size() == numberOfProgramArgs) {
            inputDir = args.get(0);
            date = getDateFromString(args.get(1));
            cdrType = args.get(2);
            msisdnRegex = getPatternFromString(args.get(3), "msisdn");
            imsiRegex = getPatternFromString(args.get(4), "imsi");
        }else{
            System.err.println("Number of args are not valid should be "
                    + numberOfProgramArgs + " but is " + args.size());
            isProgramArgsValid = false;
        }
    }

    private static Pattern getPatternFromString(String pattern, String description) {
        try {
            return Pattern.compile(pattern);
        }catch(PatternSyntaxException e){
            System.err.println(description + "pattern for RegExp is not valid");
            isProgramArgsValid = false;
        }
        return Pattern.compile("/*");
    }

    private static LocalDateTime getDateFromString(String date) {
        DateTimeFormatter formatter;
        try {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(date, formatter);
        } catch (DateTimeParseException e) {
            System.err.println(date + " is not parsable!");
            isProgramArgsValid = false;
        }
       return LocalDateTime.now();
    }
}