package com.iotracks.iofabric.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageBus {

	    private final Map<String,List<Object>> messages;

	    public MessageBus() {
	        this.messages = new HashMap<String, List<Object>>();
	    }

	    public void sendMessage(String publisher, Object data) {
	        if(!messages.containsKey(publisher)) {
	            messages.put(publisher, new ArrayList<Object>());
	        }
	        this.messages.get(publisher).add(data);
	        
	        //Curb the use of memory in situations where the messages aren't getting pulled
	        if(this.messages.get(publisher).size() > 1000)
	        {
	            int extraMessages = this.messages.get(publisher).size() - 1000;
	            
	            for(int i = 0; i < extraMessages; i++)
	            {
	                this.messages.get(publisher).remove(i);
	            }
	        }
	    }

	    public List<Object> retrieveMessage(String pub) {
	        if(messages.containsKey(pub)) {
	            return messages.remove(pub);
	        }
	        return Collections.emptyList();
	    }

}
