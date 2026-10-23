package okhttp3;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public interface Dns {
    Dns SYSTEM = hostname -> java.util.Collections.singletonList(InetAddress.getLoopbackAddress());
    List<InetAddress> lookup(String hostname) throws UnknownHostException;
}
