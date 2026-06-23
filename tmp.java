import java.io.*;
import java.net.*;
public class tmp {
  public static void main(String[] args) throws Exception {
    var url = new URL("https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter-parent/3.3.2/spring-boot-starter-parent-3.3.2.pom");
    try (var in = url.openStream()) {
      byte[] buf = in.readNBytes(64);
      System.out.println("read="+buf.length);
    }
  }
}
