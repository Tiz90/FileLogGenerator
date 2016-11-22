import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.math.BigInteger;


public class LogWriter implements Runnable{
	
	// configurazioni
	private static int lineLen = 50; //lunghezza byte singola riga
	private static double minDelayTime = 0.1;

	// bulk di partenza
	private static int lineBulk = 5; //bulk di righe scritte in una volta
	private static int bytesBulk = calcBytesBulk(lineLen, lineBulk);
	
	private static int calcBytesBulk(int lineLen, int lineBulk){
		return lineLen * lineBulk;
	}
	
	// genero stringa casuale
	private static SecureRandom random = new SecureRandom();
	private static String nextSessionId() {
		// stringa lunga circa 50 caratteri (il 240 stabilisce la lenght)
		return new BigInteger(240, random).toString(32);
	}
	
	
	private boolean simulator;
	private String filepath;
	private String log_filepath;
	private int rateBytes;
	private double maxSizeBytes;
	private boolean runForever;
	private int secToPause; // minToPause * 60;
	private int pauseMillis;
	private boolean enablePause;
	
	private static float calcDelayTime(int rate){
		float delayTime = 0;
		bytesBulk = calcBytesBulk(lineLen, lineBulk);
		// System.out.print("Setting up things...");
		while (delayTime < minDelayTime){
			// System.out.println("---------------");
			lineBulk = lineBulk + 1;
			bytesBulk = calcBytesBulk(lineLen, lineBulk);
			// System.out.println("Rate: " + rate + "Bytes/Sec");
			// System.out.println("Bytes bulk: " + bytesBulk);
			delayTime = (float)bytesBulk / (float)rate;
			// System.out.println("delay time: " + delayTime);
//			if (delayTime < minDelayTime){
//				System.out.print(".");
//			}
			// System.out.println("---------------");
		}
		// System.out.println(".");
		return delayTime;
//		int i = 0;
//		String mylog = i + "-" + nextSessionId();
//		System.out.println("String random: " + mylog + " lunga " + mylog.length());
		
	}
	
	private static float calcCycles (int rateBytes, double maxSizeBytes){
		return (float) ((double)maxSizeBytes / (double)rateBytes);
	}
	
	private static float calcIntCycles (float delayTime){
		return (float)1/delayTime;
	}
	
	public LogWriter(String filepath, int rateBytes, int maxSizeBytes, boolean simulator, double minDelayTimeMillis, boolean enablePause, int secToPause, int pauseMillis, String log_filepath, boolean runForever){
		this.rateBytes = rateBytes;
		this.filepath = filepath;
		this.maxSizeBytes = maxSizeBytes;
		this.simulator = simulator;
		minDelayTime = (double)minDelayTimeMillis/(double)1000;
		this.pauseMillis = pauseMillis;
		this.secToPause = secToPause;
		this.enablePause = enablePause;
		this.log_filepath = log_filepath;
		this.runForever = runForever;
	}
	
	private float cycles;
	private int cyclesRounded;
	
	public float getCycles(){
		return cycles;
	}


	public void run(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		
		lineBulk = 5;
		bytesBulk = calcBytesBulk(lineLen, lineBulk);
		
		// System.out.println("Thread started...");
		float delayTime = calcDelayTime(rateBytes);
		int delayTimeMillis = Math.round(delayTime*1000);
		int intCycles = Math.round(calcIntCycles(delayTime));
		cycles = calcCycles(rateBytes, maxSizeBytes);
		if (runForever){
			cycles = 999;
		}
		cyclesRounded = Math.round(cycles);
		try{
			FileWriter fw_log = new FileWriter (log_filepath, true);
			BufferedWriter bw_log = new BufferedWriter(fw_log);
			PrintWriter out_log = new PrintWriter(bw_log);
			
			
			
			if (simulator){
				cal = Calendar.getInstance();
				System.out.println("::Stats for Single files::");
				System.out.println(" -- Rate: " + rateBytes + " Bytes/s");
				System.out.println(" -- LineBulk: " + lineBulk);
				System.out.println(" -- Bytes bulk: " + bytesBulk);
				System.out.println(" -- delay time: " + delayTime + "s");
				System.out.println(" -- delay time millis: " + delayTimeMillis + "ms");
				System.out.println(" -- intCycles: " + intCycles);
				System.out.println(" -- cycles: " + cycles);
				System.out.println(" -- cyclesRounded: " + cyclesRounded);
				System.out.println("::Globals::");
				System.out.println(" -- Start time: " + dateFormat.format(cal.getTime()));
				System.out.println(" -- Estimated time: " + cyclesRounded + "s");
				
				out_log.println("::Stats for Single files::");
				out_log.println(" -- Rate: " + rateBytes + " Bytes/s");
				out_log.println(" -- LineBulk: " + lineBulk);
				out_log.println(" -- Bytes bulk: " + bytesBulk);
				out_log.println(" -- delay time: " + delayTime + "s");
				out_log.println(" -- delay time millis: " + delayTimeMillis + "ms");
				out_log.println(" -- intCycles: " + intCycles);
				out_log.println(" -- cycles: " + cycles);
				out_log.println(" -- cyclesRounded: " + cyclesRounded);
				out_log.println("::Globals::");
				out_log.println(" -- Start time: " + dateFormat.format(cal.getTime()));
				out_log.println(" -- Estimated time: " + cyclesRounded + "s");
				
				out_log.close();
				return; // interrompo il thread ho finito di simulare quello che volevo
			}
			out_log.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println("Numero cicli: " + cycles + " - Arrotondo a: " +  cyclesRounded);
//		try{
//			System.out.println("a");
//			Thread.sleep(1000);
//			System.out.println("b");
//		}
//		catch (Exception e){
//			
//		}
		String mylog;
		mylog = nextSessionId();
		long millis_init = System.currentTimeMillis();
		
		for (int i = 0; i < cyclesRounded; i++){
			if (runForever){
				cyclesRounded++; // il ciclo for durerà all'infinito
			}
			// System.out.println("Secondi rimanenti: " + (cyclesRounded - i));
			
			try{
				
				FileWriter fw_log = new FileWriter (log_filepath, true);
				BufferedWriter bw_log = new BufferedWriter(fw_log);
				PrintWriter out_log = new PrintWriter(bw_log);
				
				if (enablePause){
					if ( (i % secToPause) == 0 && (i != 0) ){
						cal = Calendar.getInstance();
						
						System.out.println(dateFormat.format(cal.getTime()) + "\t" + Thread.currentThread().getId() + "\t" + "entered in pause state.");
						out_log.println(dateFormat.format(cal.getTime()) + "\t" + Thread.currentThread().getId() + "\t" + "entered in pause state.");
						out_log.flush();
						
						Thread.sleep(pauseMillis);
						
						cal = Calendar.getInstance();
						System.out.println(dateFormat.format(cal.getTime()) + "\t" + Thread.currentThread().getId() + "\t" + "exited from pause state.");
						out_log.println(dateFormat.format(cal.getTime()) + "\t" + Thread.currentThread().getId() + "\t" + "exited from pause state.");
						out_log.flush();
					}
				}
				
				FileWriter fw = new FileWriter (filepath, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw);
				for (int k = 0; k < intCycles; k ++){
					for (int j = 0; j < lineBulk; j++){
											
						out.println(mylog);
					}
					Thread.sleep(delayTimeMillis);
				}
				out.close();
				out_log.close();
			}
			catch (Exception e){
				System.out.println("Error writing in file...");
				e.printStackTrace();
			}
		}
		long millis_end = System.currentTimeMillis();
		long tot_execution = millis_end -  millis_init;
		
		try{
			FileWriter fw_log = new FileWriter (log_filepath, true);
			BufferedWriter bw_log = new BufferedWriter(fw_log);
			PrintWriter out_log = new PrintWriter(bw_log);
			System.out.println(" -- Job " +filepath+ " finished in: " + ((float)tot_execution/(float)1000) + "s");
			out_log.println(" -- Job " +filepath+ " finished in: " + ((float)tot_execution/(float)1000) + "s");
			out_log.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		// System.out.println("Fine Thread");
	}
}
