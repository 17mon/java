import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class IP {

    public static String randomIp() {
        Random r = new Random();
        StringBuffer str = new StringBuffer();
        str.append(r.nextInt(1000000) % 255);
        str.append(".");
        str.append(r.nextInt(1000000) % 255);
        str.append(".");
        str.append(r.nextInt(1000000) % 255);
        str.append(".");
        str.append(0);

        return str.toString();
    }

    public static void main(String[] args){
        IP.load("H:\\loveapp\\codebase\\17mon\\17monipdb.dat");

        Long st = System.nanoTime();
        for (int i = 0; i < 1000000; i++)
        {
            IP.find(randomIp());
        }
        Long et = System.nanoTime();
        System.out.println((et - st) / 1000 / 1000);

        System.out.println(Arrays.toString(IP.find("118.28.8.8")));
    }

    public static boolean enableFileWatch = false;

    private static int offset;
    private static int[] index = new int[256];
    private static ByteBuffer dataBuffer;
    private static ByteBuffer indexBuffer;
    private static Long lastModifyTime = 0L;
    private static File ipFile ;
    private static ReentrantLock lock = new ReentrantLock();

    public static void load(String filename) {
        ipFile = new File(filename);
        load();
        if (enableFileWatch) {
            watch();
        }
    }

    public static void load(String filename, boolean strict) throws Exception {
        ipFile = new File(filename);
        if (strict) {
            int contentLength = Long.valueOf(ipFile.length()).intValue();
            if (contentLength < 512 * 1024) {
                throw new Exception("ip data file error.");
            }
        }
        load();
        if (enableFileWatch) {
            watch();
        }
    }

    private static int quickFindInIndexBuffer(long ip2long_value, int start, int end) {
        while(true){
            int middle = (start + end) / 2;
            middle = middle & 0xFFFFFFF8;
            if (middle <= start) return end;
            long middleValue = int2long(indexBuffer.getInt(middle));
            if (middleValue == ip2long_value) {
                return middle;
            } else if (middleValue > ip2long_value) {
                if (middle + 8 == end) {
                    return middle;
                }
                end = middle;
            } else {
                if (middle + 8 == end) {
                    return end;
                }
                start = middle;
            }
        }
    }

    public static String[] find(String ip) {
        long ip2long_value  = ip2long(ip);
        int ip_prefix_value = ((int)ip2long_value >> 24) & 0xFF;
        int start = index[ip_prefix_value];
        int max_comp_len = offset - 1028;
        if(ip_prefix_value < index.length-1) {
          max_comp_len = index[ip_prefix_value+1] * 8 + 1024;
        }
        long index_offset = -1;
        int index_length = -1;
        byte b = 0;

        int index = start * 8 + 1024;
        if (int2long(indexBuffer.getInt(index)) < ip2long_value) {
            index = quickFindInIndexBuffer(ip2long_value, index, max_comp_len);
        }
        index_offset = bytesToLong(b, indexBuffer.get(index + 6), indexBuffer.get(index + 5), indexBuffer.get(index + 4));
        index_length = 0xFF & indexBuffer.get(index + 7);
        if (index_length <= 0)
            return new String[0];

        byte[] areaBytes;

        lock.lock();
        try {
            dataBuffer.position(offset + (int) index_offset - 1024);
            areaBytes = new byte[index_length];
            dataBuffer.get(areaBytes, 0, index_length);
        } finally {
            lock.unlock();
        }

        return new String(areaBytes, Charset.forName("UTF-8")).split("\t", -1);
    }

    private static void watch() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long time = ipFile.lastModified();
                if (time > lastModifyTime) {
                    lastModifyTime = time;
                    load();
                }
            }
        }, 1000L, 5000L, TimeUnit.MILLISECONDS);
    }

    private static void load() {
        lastModifyTime = ipFile.lastModified();
        FileInputStream fin = null;
        lock.lock();
        try {
            dataBuffer = ByteBuffer.allocate(Long.valueOf(ipFile.length()).intValue());
            fin = new FileInputStream(ipFile);
            int readBytesLength;
            byte[] chunk = new byte[4096];
            while (fin.available() > 0) {
                readBytesLength = fin.read(chunk);
                dataBuffer.put(chunk, 0, readBytesLength);
            }
            dataBuffer.position(0);
            int indexLength = dataBuffer.getInt();
            byte[] indexBytes = new byte[indexLength];
            dataBuffer.get(indexBytes, 0, indexLength - 4);
            indexBuffer = ByteBuffer.wrap(indexBytes);
            indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
            offset = indexLength;

            int loop = 0;
            while (loop++ < 256) {
                index[loop - 1] = indexBuffer.getInt();
            }
            indexBuffer.order(ByteOrder.BIG_ENDIAN);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
            lock.unlock();
        }
    }

    private static long bytesToLong(byte a, byte b, byte c, byte d) {
        return int2long((((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff)));
    }

    private static int str2Ip(String ip)  {
        try {
            byte[] bytes = java.net.InetAddress.getByName(ip).getAddress();

            return ((bytes[0] & 0xFF) << 24) |
                    ((bytes[1] & 0xFF) << 16) |
                    ((bytes[2] & 0xFF) << 8) |
                    bytes[3];
        } catch (UnknownHostException e) {}
        return 0;
    }

    private static long ip2long(String ip)  {
        return int2long(str2Ip(ip));
    }

    private static long int2long(int i) {
        long l = i & 0x7fffffffL;
        if (i < 0) {
            l |= 0x080000000L;
        }
        return l;
    }
}
