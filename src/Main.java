import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.*;

public class Main {
	
	public static int toBytes(String filesize) {
	    int returnValue = -1;
	    Pattern patt = Pattern.compile("([\\d.]+)([GMK]B)", Pattern.CASE_INSENSITIVE);
	    Matcher matcher = patt.matcher(filesize);
	    Map<String, Integer> powerMap = new HashMap<String, Integer>();
	    powerMap.put("GB", 3);
	    powerMap.put("MB", 2);
	    powerMap.put("KB", 1);
	    if (matcher.find()) {
	      String number = matcher.group(1);
	      int pow = powerMap.get(matcher.group(2).toUpperCase());
	      BigDecimal bytes = new BigDecimal(number);
	      bytes = bytes.multiply(BigDecimal.valueOf(1024).pow(pow));
	      returnValue = bytes.intValue();
	      
	    }
	    return returnValue;
	}
	
	public static void main(String[] args) {
		
		Options options = new Options();
		
		Option dir = new Option("b", "basedir", true, "input dir path");
		dir.setRequired(true);
        options.addOption(dir);
        
		Option fil = new Option("f", "basefile", true, "input file base");
		fil.setRequired(true);
        options.addOption(fil);

		Option rate = new Option("r", "rate", true, "input desired throughput in Bytes/s");
		rate.setRequired(true);
        options.addOption(rate);
        
		Option size = new Option("s", "maxsize", true, "input max total size of logs");
		size.setRequired(true);
        options.addOption(size);

		Option nfiles = new Option("n", "nfiles", true, "input number of logs to be written");
		nfiles.setRequired(true);
        options.addOption(nfiles);
        
		Option mdelay = new Option("d", "mindelay", true, "input min delay from writes in files");
		mdelay.setRequired(true);
        options.addOption(mdelay);
     
		Option pauseEnableOpt = new Option("p", "pauseEnable", true, "0 for disable, 1 for enable");
		pauseEnableOpt.setRequired(true);
        options.addOption(pauseEnableOpt);

        
		Option minToPauseOpt = new Option("m", "minToPause", true, "input min to pause interval");
		// minToPauseOpt.setRequired(true);
        options.addOption(minToPauseOpt);
        
		Option pauseMillisOpt = new Option("t", "pauseMillis", true, "input min to pause interval");
		// minToPauseOpt.setRequired(true);
        options.addOption(pauseMillisOpt);
        
		Option logFilePathOtp = new Option("l", "logFilePath", true, "input log file path");
		logFilePathOtp.setRequired(true);
        options.addOption(logFilePathOtp);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar FileLogGenerator.jar", options);

            System.exit(1);
            return;
        }

        String basepath = cmd.getOptionValue("basedir");
        String filebase = cmd.getOptionValue("basefile");
        String logFilePath = cmd.getOptionValue("logFilePath");
		int rateTot = toBytes(cmd.getOptionValue("rate"));
		int dimTot = toBytes(cmd.getOptionValue("maxsize"));
		boolean runForever = false;
		if (dimTot == 0){
			runForever = true;
		}
		int num_files = Integer.parseInt(cmd.getOptionValue("nfiles"));
		double mindelay = (double)Integer.parseInt(cmd.getOptionValue("mindelay"));
		boolean pauseEnable = false;
		int minToPause = 0;
		int pauseSecs = 0;
		
		
		if (Integer.parseInt(cmd.getOptionValue("pauseEnable")) == 1){
			pauseEnable = true;
		}

		try{
			if (pauseEnable){
				if (cmd.hasOption("minToPause") && cmd.hasOption("pauseMillis")){
					minToPause = Integer.parseInt(cmd.getOptionValue("minToPause"));
					pauseSecs = Integer.parseInt(cmd.getOptionValue("pauseMillis"));
				}
				else {
					System.out.println("You have not setted minToPause or pauseMillis, the pause feature will be disabled...");
					FileWriter fw_log = new FileWriter (logFilePath, true);
					BufferedWriter bw_log = new BufferedWriter(fw_log);
					PrintWriter out_log = new PrintWriter(bw_log);			
					out_log.println("You have not setted minToPause or pauseMillis, the pause feature will be disabled...");
					out_log.close();
					pauseEnable = false;
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		int secToPause = minToPause * 60;
		int pauseMillis = pauseSecs * 1000;
		
//		int rateTot = Integer.parseInt(cmd.getOptionValue("rate"));
//		int dimTot = Integer.parseInt(cmd.getOptionValue("maxsize"));
//		int num_files = Integer.parseInt(cmd.getOptionValue("nfiles"));
//        System.out.println(basepath);
//        System.out.println(filebase);
        
        
//		String basepath = "/Users/cos/Desktop/coala-logs/";
//		
//		String filebase = "logs-";
        
		// unita misura base

		// --nfiles
		
		

		
		
		Runnable[] thrds = new Runnable[num_files];
		Thread[] threads = new Thread[num_files];
		
//		int kb10 = 10*kb1;
//		int kb100 = 100*kb1;
//		int mb5 = 5*mb1;
//		int mb10 = 10*mb1;
//		int mb50 = 50*mb1;
//		int mb25 = 25*mb1;
//		int mb100 = 100*mb1;
		
//		int rateTot = mb5;
//		int dimTot = mb100;
		
		int rateSingle = Math.round((float)rateTot / (float)num_files);
		int dimSingle = Math.round((float)dimTot / (float)num_files);
		
		Runnable simulator = new LogWriter(basepath + "/" + filebase + "simulator", rateSingle, dimSingle, true, mindelay, pauseEnable, secToPause, pauseMillis, logFilePath, runForever);
		Thread simulatore = new Thread(simulator);
		simulatore.start();
		try{
			simulatore.join();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		for (int i = 0; i < num_files; i++){
			thrds[i] = new LogWriter(basepath + "/" + filebase + i, rateSingle, dimSingle, false, mindelay, pauseEnable, secToPause, pauseMillis, logFilePath, runForever);
			// new Thread(thrds[i]).start();
			threads[i] = new Thread(thrds[i]);
			threads[i].start();
		}
		

		

		try{
			
			System.out.println(" -- Global rate: " + rateTot + " Bytes/s");
			System.out.println(" -- Max dim: " + dimTot + " Bytes");
			System.out.println(" -- Numero file: " + num_files);
			System.out.println(" -- Pause feature is: " + pauseEnable);
			System.out.println(" -- runForever is: " + runForever);
			System.out.println("::Now generating logs::");
			FileWriter fw_log = new FileWriter (logFilePath, true);
			BufferedWriter bw_log = new BufferedWriter(fw_log);
			PrintWriter out_log = new PrintWriter(bw_log);
			out_log.println(" -- Global rate: " + rateTot + " Bytes/s");
			out_log.println(" -- Max dim: " + dimTot + " Bytes");
			out_log.println(" -- Numero file: " + num_files);
			out_log.println(" -- Pause feature is: " + pauseEnable);
			out_log.println(" -- runForever is: " + runForever);
			out_log.println("::Now generating logs::");
			out_log.flush();
			for (int i = 0; i < num_files; i++){
				threads[i].join();
			}

			System.out.println("::End::");
			out_log.println("::End::");
			out_log.close();

		}
		catch (Exception e){
			e.printStackTrace();
		}
		

		
	}

}
