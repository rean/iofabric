package com.iotracks.iofabric.local_api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

public class SocketTest {
	public static void main(String[] args) {
		doWhoIs("127.0.0.1", 60401, "hello");
	}
	/**
	   * Open a socket to the whois server, write out the query, and receive
	   * the results.
	   **/
	 private static String doWhoIs(String server, int port, String name) {
	    Socket socket = null;
	    try {
	      socket = new Socket(server, port);
	      Writer out = new OutputStreamWriter(socket.getOutputStream(), "8859_1");

	      socket.setSoTimeout(10000);
	      Reader recv = new InputStreamReader(socket.getInputStream(), "8859_1");
	      out.write("=" + name + "\r\n");
	      out.flush();

	      StringBuilder builder = new StringBuilder();
	      for (int c = 0; (c = recv.read()) != -1;) {
	        builder.append(String.valueOf((char) c));
	      }

	      return builder.toString();
	    } catch (IOException e) {
	      String message = "whois server failed: " + server + " exception:" + e.toString();
	      System.out.println(message);
	      return message;
	    } finally {
	      try {
	        if (socket != null) {
	          socket.close();
	        }
	      } catch (IOException e) {
	        // don't care.
	      }
	    }
	  }
	
}
