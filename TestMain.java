import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


class TestMain {
  
  public static void main(String[] args){
      IPOld.load("/Users/york/htdocs/kankan-mp4-extractor/libs/ipip/17monipdb.dat");
      IP.load("/Users/york/htdocs/kankan-mp4-extractor/libs/ipip/17monipdb.dat");
      
      final int testSize = 100 * 10000;
      String[] ips = new String[testSize];
      for(int i=0; i<testSize; i++){
        ips[i] = IP.randomIp();
      }
      ips[0] = "119.147.158.186";
      ips[1] = "223.136.200.228";
      ips[2] = "68.104.105.223";
      ips[3] = "1.64.29.228";
      System.out.println("ip random completed");
      
      
      // check that the return value of new implement and old implement are the same
      for(int i=0; i<testSize; i++){
        String[] newReturn = IP.find(ips[i]);
        String[] oldReturn = IPOld.find(ips[i]);
        if (!Arrays.deepEquals(newReturn, oldReturn)) {
            System.out.println("ip info not equal for: " + ips[i]);
            System.out.println(Arrays.toString(newReturn));
            System.out.println(Arrays.toString(oldReturn));
            System.exit(1);
        }
      }
      
      // compare the performance
      Long startNew = System.nanoTime();
      for(int i=0; i<testSize; i++){
          IP.find(ips[i]);
      }
      Long endNew = System.nanoTime();
      System.out.println("execute time of new implement is: " + (endNew - startNew) / 1000 / 1000);

      Long startOld = System.nanoTime();
      for(int i=0; i<testSize; i++){
          IPOld.find(ips[i]);
      }
      Long endOld = System.nanoTime();
      System.out.println("execute time of old implement is: " + (endOld - startOld) / 1000 / 1000);
  }
  
  
}