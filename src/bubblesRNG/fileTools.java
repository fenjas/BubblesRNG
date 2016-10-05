package bubblesRNG;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.opencsv.CSVReader;

public class fileTools {

	static Date d = new Date();
	static SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMYY_hhmmss");

	//Write data as binary and text files 
	static String writeToBinFile(String workspacepath, List<Integer> RandomNumbers, int bytesToCapture, boolean shuffle, String options) throws IOException {

		String rndBinFN = workspacepath + "bubblesHRNG_" + dateFormat.format(d) + "_" + options + ".bin";
		DataOutputStream outBin = new DataOutputStream(new FileOutputStream(rndBinFN));

		//Avoid index error
		if (bytesToCapture>RandomNumbers.size()) {bytesToCapture=RandomNumbers.size();};

		for (int i = 0; i < bytesToCapture; i++) {
			outBin.writeByte(RandomNumbers.get(i));
		}

		outBin.flush();
		outBin.close();

		return rndBinFN;
	}

	//Create a random number sample file using the standard java prng class
	static void genStdRnd(String workspacepath, int samples) throws Exception{
		Random r = new Random();
		String rndBinFN = workspacepath + "java_std_prng_" + dateFormat.format(d) + ".bin";
		DataOutputStream outBin = new DataOutputStream(new FileOutputStream(rndBinFN));	

		try {
			for (int i=0; i<samples; i++) outBin.writeByte(r.nextInt(256));

			outBin.flush();
			outBin.close();

		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	//Create a random number sample file using the cryptograhic strong java prng class
	static void genSecRnd(String workspacepath, int samples) throws NoSuchAlgorithmException, Exception{
		SecureRandom r = SecureRandom.getInstance("SHA1PRNG");
		String rndBinFN = workspacepath + "java_sec_prng_" + dateFormat.format(d) + ".bin";
		DataOutputStream outBin = new DataOutputStream(new FileOutputStream(rndBinFN));	

		try {
			for (int i=0; i<samples; i++) outBin.writeByte(r.nextInt(256));

			outBin.flush();
			outBin.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Parse csv file generated by the Ent tool (Uses the open source library - OpenCSV)
	static String[] parseCSV(String rndDataFn) throws IOException {

		CSVReader reader = new CSVReader(new FileReader(rndDataFn));
		String[] nextLine;
		String[] entValues = { "0", "0", "0", "0", "0", "0" }; // File-bytes,Entropy,Chi-square,Mean,Monte-Carlo-Pi,Serial-Correlation

		int line = 0;
		while ((nextLine = reader.readNext()) != null) {

			if (line == 1) {
				for (int i = 1; i < nextLine.length; i++) {
					entValues[i - 1] = nextLine[i];
				}
			}
			line++;
		}

		reader.close();
		return entValues;
	}

	//ENT analysis
	static String writeEntReport(String ws, String rndDataFn, String entCSV, String entFull) throws Exception{

		Runtime rt = Runtime.getRuntime();  
		String fSuffix = "_" + dateFormat.format(d) + ".txt";

		//Call Ent tool with -t (tabbed) parameter
		String cmdToExec =  "cmd.exe /c " + ws + "ent.exe -t " + rndDataFn + " > " + ws + entCSV + fSuffix;
		rt.exec(cmdToExec);

		//Call Ent tool with default settings
		cmdToExec =  "cmd.exe /c " + ws + "ent.exe " + rndDataFn + " > " + ws + entFull + fSuffix;
		rt.exec(cmdToExec);

		return fSuffix;
	}

	//Copy the ENT.exe util to the current workspace folder
	void copyENTtoDisk(String destPath) throws IOException{

		InputStream ddlStream = getClass().getClassLoader().getResourceAsStream("ent.exe");
		OutputStream fos = null;

		try {
			fos = new FileOutputStream(destPath + "/ent.exe");
			byte[] buf = new byte[2048];
			int r = ddlStream.read(buf);
			while(r != -1) {
				fos.write(buf, 0, r);
				r = ddlStream.read(buf);
			}
		} finally {
			if(fos != null) {
				fos.close();
			}
		}
	}
}

