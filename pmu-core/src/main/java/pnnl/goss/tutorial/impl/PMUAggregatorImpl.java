package pnnl.goss.tutorial.impl;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.felix.ipojo.annotations.Property;
import org.apache.http.auth.UsernamePasswordCredentials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.core.client.Client;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.core.client.GossResponseEvent;
import pnnl.goss.core.client.GossClient.PROTOCOL;
import pnnl.goss.tutorial.PMUAggregator;
import pnnl.goss.tutorial.datamodel.PMUPhaseAngleDiffData;

public class PMUAggregatorImpl implements PMUAggregator{

	
	final Client client;
	final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private String pmu1Topic;
	private String pmu2Topic;
	private String outputTopic;
	private static Client client1 = new GossClient(new UsernamePasswordCredentials("pmu_user", "password"),PROTOCOL.STOMP);
	private static Client client2 = new GossClient(new UsernamePasswordCredentials("pmu_user", "password"),PROTOCOL.STOMP);
	
	
	
	public PMUAggregatorImpl(@Property Client client){
		this.client = client;
	}
	
	private void publishDifference(Date date, Double value1, Double value2){
		String timestamp = fmt.format(date);
		Double value = value1-value2;
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put(pmu1Topic, value1.toString());
//		map.put(pmu2Topic, value2.toString());
//		map.put(outputTopic, value.toString());
//		map.put("timestamp", timestamp);
		
		PMUPhaseAngleDiffData data = new PMUPhaseAngleDiffData();
		data.setPhasor1(value1);
		data.setPhasor2(value2);
		data.setDifference(value);
		data.setTimestamp(date);
		Gson gson = new GsonBuilder().setDateFormat(fmt.toPattern()).create();
		
		String json = gson.toJson(data);
		System.out.println("Publishing "+json+" to "+outputTopic);
		client.publish(outputTopic, json);
//		client.publish(outputTopic, map, RESPONSE_FORMAT.JSON);
	}
	
	public void startCalculatePhaseAngleDifference(String topic1, String topic2, String outputTopic) {
		pmu1Topic = topic1;
		pmu2Topic = topic2;
		this.outputTopic = outputTopic;
		final HashMap<Date, Double> topic1Values = new HashMap<Date, Double>();
		final HashMap<Date, Double> topic2Values = new HashMap<Date, Double>();
		
		
		Thread thread1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				GossResponseEvent event1  = new GossResponseEvent() {
					
					@Override
					public void onMessage(Response response) {
						String responseStr = ((DataResponse)response).getData().toString();
						String args[] = responseStr.split(",");
						Date date = null;
						Double dblValue = null;
						try {
							date = fmt.parse(args[0]);
							dblValue = Double.parseDouble(args[2]);					
							topic1Values.put(date, dblValue);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							System.err.println("Could not parse date "+args[0]);
							e.printStackTrace();
						}
						
						if (date != null && topic2Values.containsKey(date)){
							publishDifference(date, dblValue, topic2Values.get(date));
							topic1Values.remove(date);
							topic2Values.remove(date);
						}
					}
				};
				client1.subscribeTo(pmu1Topic, event1);
				
			}
		});
		
		Thread thread2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				GossResponseEvent event2 = new GossResponseEvent() {
					
					@Override
					public void onMessage(Response response) {
						String responseStr = ((DataResponse)response).getData().toString();
						String args[] = responseStr.split(",");
						Date date = null;
						Double dblValue = null;
						try {
							date = fmt.parse(args[0]);
							dblValue = Double.parseDouble(args[1]);					
							topic2Values.put(date, dblValue);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							System.err.println("Could not parse date "+args[0]);
							e.printStackTrace();
						}
						
						if (date != null && topic1Values.containsKey(date)){
							publishDifference(date, topic1Values.get(date), dblValue);
							topic1Values.remove(date);
							topic2Values.remove(date);
						}
						
					}
				};
				
				client2.subscribeTo(pmu2Topic, event2);
				
			}
		});
		
		thread1.start();
		thread2.start();
		
	}

	public String getPmu1Topic() {
		return pmu1Topic;
	}

	public void setPmu1Topic(String pmu1Topic) {
		this.pmu1Topic = pmu1Topic;
	}

	public String getPmu2Topic() {
		return pmu2Topic;
	}

	public void setPmu2Topic(String pmu2Topic) {
		this.pmu2Topic = pmu2Topic;
	}

	public String getOutputTopic() {
		return outputTopic;
	}

	public void setOutputTopic(String outputTopic) {
		this.outputTopic = outputTopic;
	}
	
	

}
