package fix_data;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Queue;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

/**
*Class basically brings everything together. Rushed so it's kind of messy with some hardcoding
*
*Author: Kevin Rathbun
*/
public class Driver extends Analyzer {

	public HashMap<Integer,ArrayList<Video>> _vidByNum;
	public HashMap<Style,ArrayList<Video>> _vidByStyle;
	private String _outputDestination;

	/**
	*Hard coded number of videos (vid_num), number of users (user_num), the path to the directory that contains video start and stop info for each user(vid_dirpath), and the path to the directory with all fixation data (tobii_dirpath)
	*Hard coded Face AOI (aoi1) and Caption AOI(aoi2).
	*
	*Easily changed by adding params but since my info never changed it was quicker in testing to just hardcode it
	*/
	public Driver(int vid_num, int user_num, String vid_dirpath, String tobii_dirpath, String vidorder_filepath, String output_destination){
		_vidByNum = new HashMap<Integer,ArrayList<Video>>();
		_vidByStyle = new HashMap<Style,ArrayList<Video>>();
		_outputDestination = output_destination;

		AOI aoi1 = new AOI("HEAD", 800,240,300,460);
		AOI aoi2 = new AOI("CAPTION", 600,750,750,320);
		AOI[] aois = {aoi1, aoi2};

		User[] users = makeUserList(user_num, vid_num, tobii_dirpath, vid_dirpath, vidorder_filepath);
		for (User user : users) {
			analyzeUserFiles(user, aoi1, aoi2);
		}
		createIMapCSV(_vidByNum,0,"HeadPFIdata");
		createIMapCSV(_vidByNum,1,"CaptionPFIdata");
		createIMapCSV(_vidByNum,2,"TransitionIdata");
		createSMapCSV(_vidByStyle,0,"HeadPFSdata");
		createSMapCSV(_vidByStyle,1,"CaptionPFSdata");
		createSMapCSV(_vidByStyle,2,"TransitionSdata");
	}

	/**
	*Creates a list of all users (aka participants) in the experiment
	*/
	public User[] makeUserList(int user_num, int vid_num, String tobii_dirpath, String vid_dirpath, String vidorder_filepath) {
		User[] users = new User[user_num];
		for (int i = 0; i < user_num; i++) {
			String userID = (i < 9) ? '0' + Integer.toString(i + 1) : Integer.toString(i + 1);
			String vid_filepath = vid_dirpath + "timestamps_" + i + ".txt";
			users[i] = new User(userID, vid_num, tobii_dirpath, vid_filepath, vidorder_filepath);
		}
		return users;
	}

	/**
	*Analyzes and sorts each user's info into hashmaps organized by video number (1-12) and video style (NO_CHANGE,COLOR_C,ITALICS_U,DEL_U). Videos 13-24 ommited because they were purely meant to test comprehension of the videos.
	*/

	public void analyzeUserFiles(User user, AOI... aois) {
		String user_dirpath = user.getFixationDirectoryPath();
		for (int i = 0; i < 12; i++) {
			String vidnum;
			if(i < 9){
				vidnum = "0" + Integer.toString(i + 1);
			} else if(i < 12){
				vidnum = Integer.toString(i + 1);
			} else{
				vidnum = Integer.toString(i+2);
			}
			String fixfile = user_dirpath + vidnum + ".txt";
			Video video = user.getVideoList()[i];
			Style style = video.getStyle();
			int strt = video.getStart();
			int stop = video.getStop();

			Queue<Fixation> fix_queue = parseFixations(fixfile, strt, stop);
			DataPair<String,HashMap<String,Double>> propFixationData = getFixationTimeData(fix_queue, strt, stop, aois);
			DataPair<String,HashMap<String,Double>> transitionData =  getTransitionData(fix_queue, strt, stop, aois);

			String data = "VIDEO" + (i+1) + " (" + style + ") ANALYSIS\n" + propFixationData.getData1() + transitionData.getData1() + "\n";
			System.out.println("User" + user.getID() + "	Video" + (i+1) + "	Analysis complete");
			writeFile(_outputDestination + "user" + user.getID(), data);

			double headpf = propFixationData.getData2().get("HEAD");
			double captionpf = propFixationData.getData2().get("CAPTION");
			double tps = transitionData.getData2().get("HEAD-CAPTION");
			video.setData(headpf,captionpf,tps);
			putVideoInIMap(_vidByNum, video.getNumber(), video);
			putVideoInSMap(_vidByStyle, style, video);
		}
	}

	/**
	*Writes data to a file with specified filename
	*/
	public static void writeFile(String filename,String data){
		try {
			FileWriter fw = new FileWriter(filename, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(data);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	*Puts video into a hash map by video number
	*/
	public void putVideoInIMap(HashMap<Integer,ArrayList<Video>> map, int num, Video video){
		if(!map.containsKey(num)){
			map.put(num, new ArrayList<Video>());
		}
		map.get(num).add(video);
	}

	/**
	*Puts video into a hash map by video style
	*/
	public void putVideoInSMap(HashMap<Style,ArrayList<Video>> map, Style st, Video video){
		if(!map.containsKey(st)){
			map.put(st, new ArrayList<Video>());
		}
		map.get(st).add(video);
	}

	public void createIMapCSV(HashMap<Integer,ArrayList<Video>> map, int data, String filename){
		Set<Integer> keySet = map.keySet();
		String out = "";
		for(Integer key: keySet){
			// System.out.print(key + ",");
			ArrayList<Video> list = map.get(key);
			out += key + ",";
			for(int j = 0; j < list.size(); j++){
				Video video = list.get(j);
				out += getDataType(video,data) + ",";
				// System.out.print(video.getNumber() + ",");
			}
			// System.out.println("");
			out += "\n";
		}
		writeFile(_outputDestination + filename + ".csv",out);
	}

	/**
	*Creates CSV file based of of map with style keys
	*/
	public void createSMapCSV(HashMap<Style,ArrayList<Video>> map, int data, String filename){
		Set<Style> keySet = map.keySet();
		String out = "";
		for(Style key: keySet){
			// System.out.print(key + ",");
			ArrayList<Video> list = map.get(key);
			out += key + ",";
			for(int j = 0; j < list.size(); j++){
				Video video = list.get(j);
				out += getDataType(video,data) + ",";
				// System.out.print(video.getNumber() + ",");
			}
			// System.out.println("");
			out += "\n";
		}
		writeFile(_outputDestination + filename + ".csv",out);
	}

	/**
	*Creates CSV file based of of map with integer keys
	*/
	public String getDataType(Video video, int data){
		String out = "";
		switch(data){
			case 0:
				out = Double.toString(video.getHeadPFTime());
				break;
			case 1:
				out = Double.toString(video.getCaptionPFTime());
				break;
			case 2:
				out = Double.toString(video.getTPS());
				break;
			default:
				System.out.println("Data Type not found for Video " + video.getNumber());
				break;
		}
		return out;
	}
}
