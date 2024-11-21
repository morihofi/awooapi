package net.fuxle.awooapi.utilities;

import net.fuxle.awooapi.utilities.internals.IPAddressChecker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IPAddressCheckerTest {

	@Test
	public void testPrivateIPv4Addresses() {
		assertTrue(IPAddressChecker.isPrivateIP("10.0.0.1"));
		assertTrue(IPAddressChecker.isPrivateIP("172.16.5.10"));
		assertTrue(IPAddressChecker.isPrivateIP("192.168.1.1"));
	}

	@Test
	public void testPublicIPv4Addresses() {
		assertFalse(IPAddressChecker.isPrivateIP("8.8.8.8"));
		assertFalse(IPAddressChecker.isPrivateIP("1.1.1.1"));
		assertFalse(IPAddressChecker.isPrivateIP("123.45.67.89"));
	}

	@Test
	public void testMulticastIPv4Addresses() {
		assertFalse(IPAddressChecker.isPrivateIP("224.0.0.1"));
		assertFalse(IPAddressChecker.isPrivateIP("239.255.255.255"));
		assertFalse(IPAddressChecker.isPrivateIP("230.0.0.1"));
	}

	@Test
	public void testPrivateIPv6Addresses() {
		assertTrue(IPAddressChecker.isPrivateIP("fc00::1"));
		assertTrue(IPAddressChecker.isPrivateIP("fd12:3456:789a::1"));
	}

	@Test
	public void testLinkLocalIPv6Addresses() {
		assertTrue(IPAddressChecker.isPrivateIP("fe80::1"));
		assertTrue(IPAddressChecker.isPrivateIP("fe80::abcd:1234"));
	}

	@Test
	public void testPublicIPv6Addresses() {
		assertFalse(IPAddressChecker.isPrivateIP("2001:4860:4860::8888"));
		assertFalse(IPAddressChecker.isPrivateIP("2606:4700:4700::1111"));
	}

	@Test
	public void testMulticastIPv6Addresses() {
		assertFalse(IPAddressChecker.isPrivateIP("ff02::1"));
		assertFalse(IPAddressChecker.isPrivateIP("ff00::"));
	}

	@Test
	public void testInvalidIPAddresses() {
		assertThrows(IllegalArgumentException.class, () -> IPAddressChecker.isPrivateIP("999.999.999.999"));
		assertThrows(IllegalArgumentException.class, () -> IPAddressChecker.isPrivateIP("invalid_ip"));
	}

	@Test
	public void testBoundaryCases() {
		assertTrue(IPAddressChecker.isPrivateIP("10.255.255.255"));
		assertTrue(IPAddressChecker.isPrivateIP("172.31.255.255"));
		assertTrue(IPAddressChecker.isPrivateIP("192.168.255.255"));
		assertFalse(IPAddressChecker.isPrivateIP("11.0.0.0"));
		assertFalse(IPAddressChecker.isPrivateIP("172.32.0.0"));
		assertFalse(IPAddressChecker.isPrivateIP("192.169.0.0"));
		assertTrue(IPAddressChecker.isPrivateIP("fcff::"));
		assertFalse(IPAddressChecker.isPrivateIP("fe00::"));
	}
}
