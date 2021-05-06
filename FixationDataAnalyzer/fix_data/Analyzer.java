package fix_data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;

/**
*Class of functions that calculate the metrics used in the experiment.
*
*Metrics measures in this class:
*Fixation Time on AOI
*Proptional Fixation Time on AOI
*Transtions between AOI1 and AOI2
*Transtitons per Second between AOI1 and AOI2
*/
public class Analyzer {

	/**
	*Parses through fixation file and puts them into a queue. strt and stop should be the start and stop timestamp that of the video that corresponds to the fixation file.
	*EX:
	*parseFixations(user1/video1.txt,strt,stop)
	*strt should be the starting timestamp of video1 when watched by user 1
	*stop should be the ending timestamp of video1 when watched by user 1
	*/
	public static Queue<Fixation> parseFixations(String fixation_file_name, int strt, int stop) {
		Queue<Fixation> out = new LinkedList<Fixation>();
		File file = new File(fixation_file_name);
		// System.out.println(fixation_file_name);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.length() > 55) {
					String[] arr = line.split(",");
					double xpos = Double.parseDouble(arr[1]);
					double ypos = Double.parseDouble(arr[2]);
					int beg = Integer.parseInt(arr[4]);
					int end = Integer.parseInt(arr[6]);
					// System.out.println("X:" + (xpos > 0) + " Y:" + (ypos > 0) + " BEG:" + (beg > strt) + "END" + (end < stop));
					if (xpos > 0 && ypos > 0 && beg > strt && end < stop) {
						out.add(new Fixation(xpos, ypos, beg, end));
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out;
	}

	/**
	*Returns a pair of string and hashmap. The string is a description of the data that was found. The hashmap uses aoi names as keys and the value is the corresponding aoi's proportional fixation time.
	*/
	public static DataPair<String,HashMap<String,Double>> getFixationTimeData(Queue<Fixation> data, int strt, int stop, AOI ... aois){
		DataPair<String,HashMap<String,Double>> out;
		HashMap<String,Double> map = new HashMap<String,Double>();
		String str = "FIXATION TIMES:\n";
		for(AOI aoi: aois){
			double fix_time = getFixationTime(aoi,data);
			double proportionalfix_time = getProportionalFixationTime(fix_time, strt, stop);
			map.put(aoi.getName(),proportionalfix_time);
			str += "AOI:	" + aoi.getName() + "	FT:	" + fix_time + "	PFT:	" + proportionalfix_time + "\n";
		}
		out = new DataPair<String,HashMap<String,Double>>(str,map);
		return out;
	}

	/**
	*Returns Fixation time of the AOI (aoi)
	*/
	public static double getFixationTime(AOI aoi, Queue<Fixation> data) {
		Queue<Fixation> temp = new LinkedList<Fixation>(data);
		double aoi_time = 0;
		while (!temp.isEmpty()) {
			Fixation f = temp.poll();
			if (aoi.isInAOI(f)) {
				// System.out.println("B" + aoi_time);
				aoi_time += f.getDuration();
				// System.out.println("A" + aoi_time);
			}
		}
		return aoi_time;
	}

	/**
	*Returns proportional fixation time given fixation time
	*/
	public static double getProportionalFixationTime(double fixation_time, int strt, int stop) {
		if( strt == 0 || stop == 0){
			return -(fixation_time);
		}
		// System.out.println("FT:	" + fixation_time + "	STRT:	" + strt + "	STOP:	" + stop);
		return (fixation_time / (stop - strt));
	}

	/**
	*Returns proportional fixation time of an AOI (aoi)
	*/
	public static double getProportionalFixationTime(AOI aoi, Queue<Fixation> data, int strt, int stop) {
		if( strt == 0 || stop == 0){
			return -(getFixationTime(aoi,data));
		}
		return (stop - strt);
	}

	/**
	*Returns a pair of string and hashmap. The string is a description of the data that was found. The hashmap uses *aoi1 name*-*aoi2 name* as keys and the value is the corresponding aoi pair's transitions per second.
	*/
	public static DataPair<String,HashMap<String,Double>> getTransitionData(Queue<Fixation> data, int strt, int stop, AOI...aois){
		DataPair<String,HashMap<String,Double>> out;
		String str = "TRANSITION Data:\n";
		HashMap<String,Double> map = new HashMap<String,Double>();
		for(int i = 0; i < aois.length;i++){
			AOI aoi1 = aois[i];
			for(int j = i + 1; j < aois.length;j++){
				AOI aoi2 = aois[j];
				int transition_num = getTransitionNum(aoi1,aoi2,data);
				double tps = getTransitionsPerSecond(transition_num,strt,stop);
				map.put(aoi1.getName() + "-" + aoi2.getName(), tps);
				str += aoi1.getName() + "-" + aoi2.getName() + ": Transitions: " + transition_num + " TPS: " + tps + "\n";
			}
		}
		out = new DataPair<String,HashMap<String,Double>>(str, map);
		return out;
	}

	/**
	*Returns number of transitions between two different AOI's (aoi1 and aoi2)
	*/
	public static int getTransitionNum(AOI aoi1, AOI aoi2, Queue<Fixation> data) {
		Queue<Fixation> temp = new LinkedList<Fixation>(data);
		int transition_num = -1;
		Fixation f = temp.poll();
		int currentAOI = 0;
		while (!temp.isEmpty()) {
			f = temp.poll();
			int nextAOI;
			if(aoi1.isInAOI(f)){
				nextAOI = 1;
			} else if(aoi2.isInAOI(f)){
				nextAOI = 2;
			} else{
				nextAOI = currentAOI;
			}
			int before = transition_num;
			if(currentAOI != nextAOI){
				currentAOI = nextAOI;
				transition_num++;
			}
			// System.out.println(nextAOI + " " + before + " -> " + transition_num);
		}
	return transition_num;
	}

	/**
	*Returns number of transitions per second between two different AOI's (aoi1 and aoi2)
	*/
	public static double getTransitionsPerSecond(int transition_num, int strt,int stop){
		if(stop - strt < 0){
			return 0.0;
		}
		double seconds = ((double)(stop - strt)) / 1000;
		double tnum = transition_num;
		double tps = (tnum) / seconds;
		// System.out.println("Trans #:" + tnum + " / " + seconds + " = " + tps);
		return tps;
	}
}
