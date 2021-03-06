package pnnl.goss.tutorial.launchers;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.DataResponse;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.core.client.GossClient.PROTOCOL;
import pnnl.goss.core.client.GossResponseEvent;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;
import pnnl.goss.tutorial.PMUGenerator;
import pnnl.goss.tutorial.impl.PMUGeneratorImpl;

@Component
@Instantiate
public class GeneratorLauncher extends Thread {
	private PMUGenerator generator1;
	private PMUGenerator generator2;
	GossClient client = null; 
//	private static Client client = new GossClient(new UsernamePasswordCredentials("pmu_user", "password"),PROTOCOL.STOMP);
	private boolean running = false;
	
	private static Logger log = LoggerFactory
			.getLogger(GeneratorLauncher.class);
	private volatile GossRequestHandlerRegistrationService registrationService;
	
	private GeneratorLauncher launcher;
//	public static void main(String[] args){
//		
//		new GeneratorLauncher().startLauncher();
//		
//	}
	
	public GeneratorLauncher(@Requires GossRequestHandlerRegistrationService registrationService){
		try{
			log.debug("Creating Generator Launcher "+registrationService);
			if(registrationService!=null && registrationService.getCoreServerConfig()!=null){

				this.registrationService = registrationService;
				client = new GossClient(PROTOCOL.STOMP);
				log.debug("CoreServerConfig "+this.registrationService.getCoreServerConfig());
				client.setConfiguration(this.registrationService.getCoreServerConfig());
			} else {
				log.error("Generator received null core server config");
			}
		}catch(Exception e){
			log.error("Failing while creating generator launcher", e);
		}
	}
	
	@Validate
	public void startLauncher(){
		log.debug("Starting Generator Launcher");
		if(launcher==null){
			launcher = new GeneratorLauncher(this.registrationService);
			launcher.start();
		}
	}
	
	@Invalidate
	public void stopLauncher(){
		log.debug("Stopping Generator Launcher");
		launcher.stop();
		launcher=null;
	}
	
	@Override
	public void run() {
		log.debug("Creating Tutorial Generator Launcher");
		
		GossResponseEvent event = new GossResponseEvent() {
			public void onMessage(Serializable response) {
				String message = (String)((DataResponse)response).getData(); 
				log.debug("Generator received message "+message);
				if(message.contains("start pmu") && running==false){
					launch();
					running = true;
				}
				if(message.contains("stop pmu") && running==true){
					generator1.stop();
					generator2.stop();
					running = false;
				}
				
			}
		};
		client.subscribeTo("/topic/goss/tutorial/control", event);
	
	}
	
	protected void launch(){
		
		try{
			String pmu1Id = "PMU_1";
			String pmu2Id = "PMU_2";
			String pmu1Topic = "goss/tutorial/pmu/PMU_1";
			String pmu2Topic = "goss/tutorial/pmu/PMU_2";
			int itemsPerInterval = 2;
			double intervalSeconds = 1;
			Random random = new Random();
			double max = 1.0;
			double min = 0.0;
			Date datetime = null;
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			DecimalFormat decimalFormat = new DecimalFormat("#.##");
			String phase;
			String freq;
			int totalValues = 1000;
			String dataArr1[] = new String[totalValues];
			String dataArr2[] = new String[totalValues];
			
			///Create timestamp
			for(int i=0;i<totalValues;i++){
				
				if(datetime==null){
					
					datetime = formatter.parse("2014-07-10 01:00:00.000");
					
				}
				else{
					long milliseconds = 33;
					datetime.setTime(datetime.getTime()+milliseconds);
				}
				
				//Create data for PMU 1 Phasor 1 stream
				min = -15;
				max = 15;
				phase = decimalFormat.format(min + (max - min) * random.nextDouble());
				min = 58;
				max = 62;
				freq = decimalFormat.format(min + (max - min) * random.nextDouble());
				dataArr1[i] = formatter.format(datetime)+","+phase+","+freq;
				
				//Create data for PMU 2 Phasor 1 stream
				min = -15;
				max = 15;
				phase = decimalFormat.format(min + (max - min) * random.nextDouble());
				min = 58;
				max = 62;
				freq = decimalFormat.format(min + (max - min) * random.nextDouble());
				dataArr2[i] = formatter.format(datetime)+","+phase+","+freq;
				
			}
			
			generator1 = new PMUGeneratorImpl(client,  Arrays.asList(dataArr1));
			generator1.start(pmu1Id, pmu1Topic, itemsPerInterval, intervalSeconds);
			
			generator2 = new PMUGeneratorImpl(client,  Arrays.asList(dataArr2));
			generator2.start(pmu2Id, pmu2Topic, itemsPerInterval, intervalSeconds);
		
		}
		catch(ParseException pe){
			log.error("PMU stream date is not in the correct format", pe);
		}
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		
		if(generator1!=null){
			generator1.stop();
		}
		
		if(generator2!=null){
			generator2.stop();
		}
	}
}
