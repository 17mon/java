import org.junit.jupiter.api.Test;

import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class IpBeanTest {
    IP ipTesT=new IP();

/*  IP Addresses to test returned Geo location
    UK - 193.62.157.66
    USA - 162.254.206.227
    India - 1.23.255.255
 */

    @Test
    public void testIPLoad_UK_IP() {
        ipTesT.load("./src/main/resources/17monipdb.dat");

        //Output translation - [United Kingdom, United Kingdom]
        assertEquals("[英国, 英国]", Arrays.toString(IP.find("193.62.157.66"))); // Default value used by author
        assertEquals("[英国, 英国]", Arrays.toString(IP.find("213.255.193.25")));
    }
    @Test
     public void testIPLoad_US_IP() {
        //Output translation - [America America]
        assertEquals("[美国, 美国]", Arrays.toString(IP.find("162.254.206.227")));

    }
    @Test
      public void testIPLoad_India_IP() {
        //Outpur translation - [India, India]
        assertEquals("[印度, 印度]", Arrays.toString(IP.find("1.23.255.255")));

    }
    }

/*   Transalations are done via Google Translator */


