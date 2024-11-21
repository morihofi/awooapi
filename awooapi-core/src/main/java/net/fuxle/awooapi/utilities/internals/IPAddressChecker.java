package net.fuxle.awooapi.utilities.internals;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPAddressChecker {

	public static boolean isPrivateIP(String ipAddress) {
		try {
			InetAddress inetAddress = InetAddress.getByName(ipAddress);
			byte[] address = inetAddress.getAddress();

			if (address.length == 4) { // IPv4
				int firstByte = address[0] & 0xFF;
				int secondByte = address[1] & 0xFF;

				// 10.0.0.0 - 10.255.255.255
				if (firstByte == 10) {
					return true;
				}

				// 172.16.0.0 - 172.31.255.255
				if (firstByte == 172 && (secondByte >= 16 && secondByte <= 31)) {
					return true;
				}

				// 192.168.0.0 - 192.168.255.255
				if (firstByte == 192 && secondByte == 168) {
					return true;
				}

				// Multicast range: 224.0.0.0 - 239.255.255.255
				if (firstByte >= 224 && firstByte <= 239) {
					return false; // Explizit nicht privat
				}
			} else if (address.length == 16) { // IPv6
				int firstByte = address[0] & 0xFF;
				int secondByte = address[1] & 0xFF;

				// Unique Local Address (ULA): fc00::/7
				if ((firstByte & 0xFE) == 0xFC) {
					return true;
				}

				// Link-Local Address: fe80::/10
				if (firstByte == 0xFE && (secondByte & 0xC0) == 0x80) {
					return true;
				}

				// Multicast Address: ff00::/8
				if (firstByte == 0xFF) {
					return false; // Explizit nicht privat
				}
			}
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Unsupported IP-Address format: " + ipAddress);
		}

		return false;
	}
}
