import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonParsingTest {

    private static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static  ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private static final String path = "'/home/rafal/Dokumenty/Parser_ANS.1/files/cdr_files'";

    @BeforeEach
    void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Test
    void printErrorMsgWhenDateIsNotParsable() {
        String[] args = new String[]{path, "'2017-13-11", "00:00:00'", "'cancelLocation'", "'48\\d{9}'", "'.*'"};
        Main.main(args);
        assertEquals("2017-13-11 00:00:00 is not parsable!\n", errContent.toString());
    }

    @Test
    void printErrorMsgWhenEmptyFile() {
        String[] args = new String[]{path, "'2017-10-11", "00:00:00'", "'cancelLocation'", "'48\\d{9}'", "'.*'"};
        Main.main(args);
        assertEquals("Empty file!\n", errContent.toString());
    }

    @Test
    void printErrorMsgWhenTooMuchArgs() {
        String[] args = new String[]{path, "'2017-10-11", "00:00:00'", "'cancelLocation'", "'48\\d{9}'", "'.*'", "abc"};
        Main.main(args);
        assertEquals("Number of args are not valid\n", errContent.toString());
    }

    @Test
    void printIOExceptionWhenWrongPath(){
        String[] args = new String[]{path+"abc", "'2017-10-11", "00:00:00'", "'cancelLocation'", "'48\\d{9}'", "'.*'"};
        Main.main(args);
        assertEquals("IOException\n", errContent.toString());
    }

    @Test
    void printNothingWhenCdrTypeIsWrong() {
        String[] args = new String[]{path, "'2017-10-11", "00:00:00'", "'carcelLocation'", "'48\\d{9}'", "'.*'"};
        Main.main(args);
        assertEquals("", outContent.toString());
    }

    @Test
    void printNothingWhenTooLateDate(){
        String[] args = new String[]{path, "'2017-12-15", "00:00:00'", "'updateLocation'", "'48\\d{9}'", "'.*'"};
        Main.main(args);
        assertEquals("", outContent.toString());
    }

    @Test
    void printNothingWhenMsisdnRegexNotMeetAnyRequirements(){
        String[] args = new String[]{path, "'2017-12-10", "00:00:00'", "'updateLocation'", "'48\\d{8}'", "'.*'"};
        Main.main(args);
        assertEquals("", outContent.toString());
    }

    @Test
    void printNothingWhenImsiRegexNotMeetAnyRequirements(){
        String[] args = new String[]{path, "'2017-10-10", "00:00:00'", "'updateLocation'", "'48\\d{9}'", "'ab.*'"};
        Main.main(args);
        assertEquals("", outContent.toString());
    }

    @Test
    void printAppropriateJsonsWhenArgsAreFromPdf(){
        String[] args = new String[]{path, "'2017-12-11", "00:00:00'", "'updateLocation'", "'48\\d{9}'", "'.*'"};
        Main.main(args);
        String expectedJsons = "{\"imsi\":\"260060011564227\",\"type\":\"updateLocation\",\"msisdn\":\"48790054321\",\"vlr\":\"4\",\"timestamp\":\"2017-12-12 23:16:58\"}\n" +
                "{\"imsi\":\"260060018942143\",\"type\":\"updateLocation\",\"msisdn\":\"48652180181\",\"vlr\":\"1\",\"timestamp\":\"2017-12-11 00:56:18\"}\n";
        assertEquals(expectedJsons, outContent.toString());
    }


    @Test
    void printAppropriateJsonsWithDifferentImsiRegex(){
        String[] args = new String[]{path, "'2017-12-10", "00:00:00'", "'updateLocation'", "'48\\d{9}'", "'.*7'"};
        Main.main(args);
        String expectedJsons = "{\"imsi\":\"260060011564227\",\"type\":\"updateLocation\",\"msisdn\":\"48790054321\",\"vlr\":\"4\",\"timestamp\":\"2017-12-12 23:16:58\"}\n" +
                "{\"imsi\":\"260060018293827\",\"type\":\"updateLocation\",\"msisdn\":\"48790005968\",\"vlr\":\"1\",\"timestamp\":\"2017-12-10 12:57:18\"}\n";
        assertEquals(expectedJsons, outContent.toString());
        assertEquals("Empty file!\n", errContent.toString());
    }

    @Test
    void printAllUpdateLocationJsons(){
        String[] args = new String[]{path, "'2010-12-10", "00:00:00'", "'updateLocation'", "'48\\d{9}'", "'.*'"};
        Main.main(args);
        String expectedJsons = "{\"imsi\":\"260060011564227\",\"type\":\"updateLocation\",\"msisdn\":\"48790054321\",\"vlr\":\"4\",\"timestamp\":\"2017-12-12 23:16:58\"}\n" +
                "{\"imsi\":\"260060018942143\",\"type\":\"updateLocation\",\"msisdn\":\"48652180181\",\"vlr\":\"1\",\"timestamp\":\"2017-12-11 00:56:18\"}\n{\"imsi\":\"260060018293827\",\"type\":\"updateLocation\",\"msisdn\":\"48790005968\",\"vlr\":\"1\",\"timestamp\":\"2017-12-10 12:57:18\"}\n";
        assertEquals(expectedJsons, outContent.toString());
        assertEquals("Empty file!\n", errContent.toString());
    }

    @Test
    void printAllCancelLocationJsons(){
        String[] args = new String[]{path, "'2010-12-10", "00:00:00'", "'cancelLocation'", "'48\\d{9}'", "'.*'"};
        Main.main(args);
        String expectedJsons = "{\"imsi\":\"260060018293823\",\"type\":\"cancelLocation\",\"msisdn\":\"48790008845\",\"vlr\":\"3\",\"timestamp\":\"2017-12-10 12:57:18\"}\n" +
                "{\"imsi\":\"260060016824537\",\"type\":\"cancelLocation\",\"msisdn\":\"48790004123\",\"vlr\":\"5\",\"timestamp\":\"2017-12-10 15:27:11\"}\n";
        assertEquals(expectedJsons, outContent.toString());
        assertEquals("Empty file!\n", errContent.toString());
    }


}

