public class IpBean implements Comparable<IpBean>
{
    long startip;
    long endip;
    String[] area;

    @Override
    public int compareTo(IpBean otherBean)
    {
        if (otherBean.startip < startip || otherBean.startip > endip) {
            if (startip > otherBean.startip) {
                return 1;
            } else if (startip < otherBean.startip) {
                return -1;
            }
        }
        return 0;
    }
}
