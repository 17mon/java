import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class IP
{

	public static void main(String[] args)
	{
		IP.load("/Users/Neo/Desktop/17monipdb.dat");
		System.out.println("find:"+Arrays.toString(IP.find("213.255.193.25")));
		System.out.println("find2:"+Arrays.toString(IP.find2("213.255.193.25")));
	}

	public static boolean enableFileWatch = false;

	
	private static List<IpBean> iplist = new ArrayList<IpBean>();

	
	private static int offset;
	private static int[] index = new int[256];
	private static ByteBuffer dataBuffer;
	private static ByteBuffer indexBuffer;
	private static Long lastModifyTime = 0L;
	private static File ipFile;
	private static ReentrantLock lock = new ReentrantLock();

	public static void load(String filename)
	{
		ipFile = new File(filename);
		load();
		if (enableFileWatch)
		{
			watch();
		}
	}

	public static String[] find(String ip)
	{
		int ip_prefix_value = new Integer(ip.substring(0, ip.indexOf(".")));
		long ip2long_value = ip2long(ip);
		int start = index[ip_prefix_value];
		int max_comp_len = offset - 1028;
		long index_offset = -1;
		int index_length = -1;
		byte b = 0;
		for (start = start * 8 + 1024; start < max_comp_len; start += 8)
		{
			if (int2long(indexBuffer.getInt(start)) >= ip2long_value)
			{
				index_offset = bytesToLong(b, indexBuffer.get(start + 6),
								indexBuffer.get(start + 5),
								indexBuffer.get(start + 4));
				index_length = 0xFF & indexBuffer.get(start + 7);
				break;
			}
		}

		byte[] areaBytes;
		lock.lock();
		try
		{
			dataBuffer.position(offset + (int) index_offset - 1024);
			areaBytes = new byte[index_length];
			dataBuffer.get(areaBytes, 0, index_length);
		}
		finally
		{
			lock.unlock();
		}

		return new String(areaBytes).split("\t");
	}
	
	public  static String[] find2(String ip)
	{
		String[]  area = null;
		long ip2long_value = ip2long(ip);
		
		IpBean o = new IpBean();
		o.startip = ip2long_value;
		
		int len = iplist.size()-1;
		
		int idx = len / 2;
		int high = len;
		int low = 0;

		while (low <= high)
		{
			//System.out.print(idx+ " ");
			IpBean t = iplist.get(idx);
			//Log.OutLog("%s %s %s",Format.long2ip(t.startip),Format.long2ip(t.endip),Format.long2ip(ip2long_value));
			int re = t.compareTo(o);
			if (re < 0)
			{
				low = idx+1;
			}
			else if (re > 0)
			{
				high = idx-1;
			}
			else 
			{
				area = t.area;
				break;
			}
			idx =(low+high)/2;
		}

		return area;
	}

	private static void watch()
	{
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				long time = ipFile.lastModified();
				if (time > lastModifyTime)
				{
					lastModifyTime = time;
					load();
				}
			}
		}, 1000L, 5000L, TimeUnit.MILLISECONDS);
	}
	
	private static void load2()
	{
		iplist.clear();
		int start = index[0];
		int max_comp_len = offset - 1028;
		long index_offset = -1;
		int index_length = -1;
		byte b = 0;
		
		IpBean lastip = null;
		for (start = start * 8 + 1024; start < max_comp_len; start += 8)
		{
			long startipl = int2long(indexBuffer.getInt(start));
			index_offset = bytesToLong(b, indexBuffer.get(start + 6),
							indexBuffer.get(start + 5),
							indexBuffer.get(start + 4));
			index_length = 0xFF & indexBuffer.get(start + 7);
			
			byte[] areaBytes;
			lock.lock();
			try
			{
				dataBuffer.position(offset + (int) index_offset - 1024);
				areaBytes = new byte[index_length];
				dataBuffer.get(areaBytes, 0, index_length);
				
				IpBean ipb = new IpBean();
				ipb.endip = startipl;
				ipb.area = new String(areaBytes).split("\t");
				
				if (lastip != null)
				{					
					if (Arrays.equals(ipb.area, lastip.area))
					{
						lastip.endip = ipb.endip;
					}
					else
					{
						ipb.startip = lastip.endip+1;
						iplist.add(ipb);
						lastip = ipb;
					}
				}
				else
				{
					ipb.startip = 0;
					lastip = ipb;
				}
			}
			finally
			{
				lock.unlock();
			}
		}
	}

	private static void load()
	{
		lastModifyTime = ipFile.lastModified();
		FileInputStream fin = null;
		lock.lock();
		try
		{
			dataBuffer = ByteBuffer.allocate(Long.valueOf(ipFile.length())
							.intValue());
			fin = new FileInputStream(ipFile);
			int readBytesLength;
			byte[] chunk = new byte[4096];
			while (fin.available() > 0)
			{
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
			while (loop++ < 256)
			{
				index[loop - 1] = indexBuffer.getInt();
			}
			indexBuffer.order(ByteOrder.BIG_ENDIAN);
		}
		catch (IOException ioe)
		{

		}
		finally
		{
			try
			{
				if (fin != null)
				{
					fin.close();
				}
			}
			catch (IOException e)
			{
			}
			lock.unlock();
		}
		load2();
	}

	private static long bytesToLong(byte a, byte b, byte c, byte d)
	{
		return int2long((((a & 0xff) << 24) | ((b & 0xff) << 16)
						| ((c & 0xff) << 8) | (d & 0xff)));
	}

	private static int str2Ip(String ip)
	{
		String[] ss = ip.split("\\.");
		int a, b, c, d;
		a = Integer.parseInt(ss[0]);
		b = Integer.parseInt(ss[1]);
		c = Integer.parseInt(ss[2]);
		d = Integer.parseInt(ss[3]);
		return (a << 24) | (b << 16) | (c << 8) | d;
	}

	private static long ip2long(String ip)
	{
		return int2long(str2Ip(ip));
	}

	private static long int2long(int i)
	{
		long l = i & 0x7fffffffL;
		if (i < 0)
		{
			l |= 0x080000000L;
		}
		return l;
	}
	
	static class IpBean implements Comparable<IpBean>
	{
		long startip;
		long endip;
		String[] area;
		
		@Override
		public int compareTo(IpBean o)
		{
			if (o.startip>=startip && o.startip<=endip)
			{
				return 0;
			}
			else
			{
				if (startip > o.startip)
				{
					return 1;
				}
				else if(startip < o.startip)
				{
					return -1;
				}
				return 0;
			}
		}
	}
}