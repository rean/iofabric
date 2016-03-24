package com.iotracks.iofabric.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.iotracks.iofabric.utils.configuration.Configuration;

public class Orchestrator {
	private String controllerUrl;
	private String instanceId;
	private String accessToken;
	private String controllerCert;
	private String eth;
	private CloseableHttpClient client;
	
	public Orchestrator() {
		this.update();
	}

	public boolean ping() throws Exception {
		try {
			JsonObject result = getJSON(controllerUrl + "status");
			return result.getString("status").equals("ok");
		} catch (Exception e) {
			throw e;
		} 
	}

	public JsonObject provision(String key) throws Exception {
		JsonObject result = null;
		try {
			result = getJSON(controllerUrl + "instance/provision/key/" + key);
		} catch (Exception e) {
			throw e;
		} 
		return result;
	}
	
	private RequestConfig getRequestConfig() throws Exception {
		InetAddress address = null;
		boolean found = false;
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		    while (networkInterfaces.hasMoreElements()) {
		        NetworkInterface networkInterface = networkInterfaces.nextElement();
		        if (networkInterface.getName().equals(eth)) {
		        	Enumeration<InetAddress> ipAddresses = networkInterface.getInetAddresses();
		        	while (ipAddresses.hasMoreElements()) {
		        		address = ipAddresses.nextElement();
		        		if (address instanceof Inet4Address) {
		        			found = true;
		        			break;
		        		}
		        	}
		        	if (found)
		        		break;
		        }
		    }
		} catch (Exception e) {}
		
		if (!found)
			throw new Exception(String.format("unable to bind network interface \"%s\"", eth));
		return RequestConfig.copy(RequestConfig.DEFAULT).setLocalAddress(address).build();
	}
	
	private void initialize() throws Exception {
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					private X509Certificate[] certs;
					
					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return certs;
					}
					
					@Override
					public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
						certs = arg0;
						boolean verified = false;
						for (X509Certificate c : arg0) {
							String cert = Base64.getEncoder().encodeToString(c.getSignature());
							if (cert.equals(controllerCert)) {
								verified = true;
								break;
							}
						}
						if (!verified)
							throw new CertificateException("HOOOOOOOOOOOOOOOOOY");
					}
					
					@Override
					public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
						System.out.println(arg1);
					}
				}
		}; 
        SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, trustAllCerts, new SecureRandom());
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
	    client = HttpClients.custom().setSSLSocketFactory(sslsf).build();
	}
	
	public static void main(String[] args) throws Exception {
		Orchestrator o = new Orchestrator();
		o.eth = "eth0";
//		JsonObject result = o.getJSON("https://iotracks.com/api/v1/status");
		o.controllerUrl = "https://iotracks.com/api/v1/";
		o.instanceId = "qk7PnPVpDTGmx3zWNR8zNP34";
		o.accessToken = "0b51a84b066a049228ea3e0b14f424720842c132153b2fa7091c21a1129534d4";
		o.controllerCert = "o1gexkMyrKwvk3i36q5UQEctfniNUPb4ZqzWT3PWRO+vC8xbwfRPmo9JfmCvwifHFvH7k4GQqXzvb35uRZQWhL3sSfHEDvSvBFmDhw8sO5fDWhKbewQ1e6OVMwh7k3EiQrOp2W9PgZL8B7Z5vIRKnXcJ8cWJ8vC0nFSqEnsNuk/vkxns731OYaOOdpxZz4yUsYSX9xq5B7iyxk8Tedu/T1Ebf2kNUSrB1hX/N1E0ZVH0Hr44auwOq789ezkFe/Tz+xqh0Mh+TmSNzYxhVZD+OspdJQ/4HaNKdFZPGlVAcHUlpjMuukulXVOaDTDhjV9hLK/M77CZoYD/C/JiTHAmmA==";
		JsonObject result = o.doCommand("containerlist", null, null);
		System.out.println(result.toString());
		System.out.println();
	}
	
	public JsonObject getJSON(String surl) throws Exception {
		initialize();
		RequestConfig config = getRequestConfig();
		HttpPost post = new HttpPost(surl);
		post.setConfig(config);
		
		CloseableHttpResponse response = client.execute(post);
		
		if (response.getStatusLine().getStatusCode() != 200)
			throw new Exception("ERROR");
		
        Reader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

        JsonObject result = Json.createReader(in).readObject();
        
        response.close();
		return result;
	}

	public JsonObject doCommand(String command, Map<String, Object> queryParams, Map<String, Object> postParams) throws Exception {
		JsonObject result = null;
		
		StringBuilder uri = new StringBuilder(controllerUrl);
		
		uri.append("instance/")
			.append(command)
			.append("/id/").append(instanceId)
			.append("/token/").append(accessToken);
		
		if (queryParams != null)
			queryParams.entrySet().forEach(entry -> {
				uri.append("/").append(entry.getKey())
					.append("/").append(entry.getValue());
			});

		List<NameValuePair> postData = new ArrayList<NameValuePair>();		
		if (postParams != null)
			postParams.entrySet().forEach(entry -> {
				postData.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
			});

		try {
			initialize();
			RequestConfig config = getRequestConfig();
			HttpPost post = new HttpPost(uri.toString());
			post.setConfig(config);
			post.setEntity(new UrlEncodedFormEntity(postData));
			
			CloseableHttpResponse response = client.execute(post);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			result = Json.createReader(in).readObject();
		} catch (Exception e) {
			throw e;
		}
		
		return result;
	}

	public void update() {
		instanceId = Configuration.getInstanceId();
		accessToken = Configuration.getAccessToken();
		controllerUrl = Configuration.getControllerUrl();
		controllerCert = Configuration.getControllerCert();
		eth = Configuration.getNetworkInterface();
	}
}
