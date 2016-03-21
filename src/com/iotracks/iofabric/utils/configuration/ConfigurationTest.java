

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigurationTest 
{
 	// TODO: how to test loadConfig and setConfig
	@Test
	public void testGetAccessToken()
	{
		String testValue = "access token";
		Configuration.setAccessToken(testValue);
		assertEquals(Configuration.getAccessToken(), testValue);
	}
	
	@Test
	public void testGetControllerUrl()
	{
		String testValue = "controller url";
		Configuration.setControllerUrl(testValue);
		assertEquals(Configuration.getControllerUrl(), testValue);
	}
	
	@Test
	public void testGetControllerCert()
	{
		String testValue = "controller cert";
		Configuration.setControllerCert(testValue);
		assertEquals(Configuration.getControllerCert(), testValue);
	}
	
	@Test
	public void testGetNetworkInterface()
	{
		String testValue = "network inteface";
		Configuration.setNetworkInterface(testValue);
		assertEquals(Configuration.getNetworkInterface(), testValue);
	}
	
	@Test
	public void testGetDockerUrl()
	{
		String testValue = "docker url";
		Configuration.setDockerUrl(testValue);
		assertEquals(Configuration.getDockerUrl(), testValue);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testGetDiskLimit()
	{
		float testValue = (float) 3.14;
		Configuration.setDiskLimit(testValue);
		assertEquals(Configuration.getDiskLimit(), testValue);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testGetMemoryLimit()
	{
		float testValue = (float) 3.14;
		Configuration.setMemoryLimit(testValue);
		assertEquals(Configuration.getMemoryLimit(), testValue);
	}
	
	@Test
	public void testGetDiskDirectory()
	{
		String testValue = "disk directory";
		Configuration.setDiskDirectory(testValue);
		assertEquals(Configuration.getDiskDirectory(), testValue);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testGetCpuLimit()
	{
		float testValue = (float) 3.14;
		Configuration.setCpuLimit(testValue);
		assertEquals(Configuration.getCpuLimit(), testValue);
	}
	
	@Test
	public void testGetInstanceId()
	{
		String testValue = "instance id";
		Configuration.setInstanceId(testValue);
		assertEquals(Configuration.getInstanceId(), testValue);
	}
	
	@Test
	public void testGetLogFileCount()
	{
		int testValue = 1;
		Configuration.setLogFileCount(testValue);
		assertEquals(Configuration.getLogFileCount(), testValue);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testGetLogDiskLimit()
	{
		float testValue = (float) 3.14;
		Configuration.setLogDiskLimit(testValue);
		assertEquals(Configuration.getLogDiskLimit(), testValue);
	}
	
	@Test
	public void getLogDiskDirectory()
	{
		String testValue = "log disk directory";
		Configuration.setLogDiskDirectory(testValue);
		assertEquals(Configuration.getLogDiskDirectory(), testValue);
	}
}
