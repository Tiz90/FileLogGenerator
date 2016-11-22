# FileLogGenerator

Java Application that generates log files. Each line is 50byte long and it's random generated.

The application have to be runned via command line: #> java -jar jars/FileLogGenerator.jar <options>

Lista options:
 -b, --basedir <arg>: directory where the files are generated
 -f, --basefile <arg>: filename prefix for the files generated (files are named "prefix-<#progressiveNumber>.log")
 -d, --mindelay <arg>: delay beetween writes in the generated files (in ms)
 -n, --nfiles <arg>: number of files generated
 -r, --rate <arg>: total write rate for all files (eg: if we want a rate of 1MB/s we have to input 1MB).  
 -s, --maxsize <arg>: total max size of files generated (after that the application will stop) (eg: 1GB, 20MB, 50KB). Input 0 if you want the app to run forever.
 -l, --logFilePath <arg>: path where application stores the debug log
 -p --pauseEnable <0|1>: enable (1) or disable (0) the pause feature
 -m,--minToPause <arg>: time interval (in minutes) after the application will pause the generation of files (pauseEnable needs to be 1)
 -t <arg>: the amount of seconds of the pause. After that the application will restart generating logs

Example:
java -jar FileLogGenerator.jar -b /root/test-folder-agent/ -f agent- -r 200KB -s 0GB -n 200 -d 100 -p 1 -l /root/generator.log -m 20 -t 300

The application will generate logs in the folder /root/test-folder-agent/, 200 files will be generated with names from agent-0.log to agent-199.log. Total rate generation of logs will be 200KB/s (so in this case each file will be written with a rate of 1KB/s), the application will run forever (-s 0GB). Every 20 minutes the app will pause the generation of logs for 300 seconds.