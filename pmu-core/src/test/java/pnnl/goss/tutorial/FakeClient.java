package pnnl.goss.tutorial;

import java.util.HashMap;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import pnnl.goss.core.Data;
import pnnl.goss.core.Request;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.client.Client;
import pnnl.goss.core.client.GossResponseEvent;

public class FakeClient implements Client {

	// Handlings passing data from/to topic
	HashMap<String, String> pubSubResponeCache = new HashMap<String,String>();
	// Handles the subscribing of an event.
	HashMap<String, GossResponseEvent> topicEvent = new HashMap<String, GossResponseEvent>();

	public boolean isSubscribed(String topic){
		return topicEvent.containsKey(topic);
	}
	
	@Override
	public Object getResponse(Request request) throws IllegalStateException,
			JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getResponse(Request request, RESPONSE_FORMAT responseFormat) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendRequest(Request request, GossResponseEvent event,
			RESPONSE_FORMAT responseFormat) throws NullPointerException {
		// TODO Auto-generated method stub
		

	}

	@Override
	public void subscribeTo(String topicName, GossResponseEvent event)
			throws NullPointerException {
		// TODO Auto-generated method stub
		topicEvent.put(topicName, event);

	}

	@Override
	public void publish(String topicName, Data data,
			RESPONSE_FORMAT responseFormat) throws NullPointerException {
		
				

	}

	@Override
	public void publish(String topicName, String data)
			throws NullPointerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}