package fix_data;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
*Video object holds UserID of user who watched it, the video number of it, the start timestamp, and the end timestamp (in milliseconds)
*
*Author: Kevin Rathbun
*/

public class Video {

	private int _num;
	private int _strt;
	private int _stop;
	private Style _style;
	private double _headPF_time;
	private double _captionPF_time;
	private double _tps;

	/*
	*Got lazy and hardcoded in the file path. Either change to correct file path or alter program to ask for it upon starting.
	*/
	private File _video_order_file;

	public Video(String userID, int num, int strt, int stop, String video_order_file){
		_num = num;
		_strt = strt;
		_stop = stop;
		_headPF_time = -1;
		_captionPF_time = -1;
		_tps = -1;
		_video_order_file = new File(video_order_file);

		int user = Integer.parseInt(userID) - 1;
		_style = findStyle(user, num);
	}

	/*
	*Returns video number
	*/
	public int getNumber(){
		return _num;
	}

	/*
	*Returns timestamp of when the video started.
	*/
	public int getStart(){
		return _strt;
	}

	/*
	*Returns timestamp of when the video ended.
	*/
	public int getStop(){
		return _stop;
	}

	/*
	*returns caption style of text
	*/
	public Style getStyle(){
		return _style;
	}

	/*
	*Sets Proportional Fixation Time on Face for video
	*/
	public void setHeadPFTime(double n){
		_headPF_time = n;
	}

	/*
	*Returns Proportional Fixation Time on Face for video
	*/
	public double getHeadPFTime(){
		return _headPF_time;
	}

	/*
	*Sets Proportional Fixation Time on Captioning for video
	*/
	public void setCaptionPFTime(double n){
		_captionPF_time = n;
	}

	/*
	*Returns Proportional Fixation Time on Captioning for video
	*/
	public double getCaptionPFTime(){
		return _captionPF_time;
	}

	/*
	*Set Transitions per Second for video
	*/
	public void setTPS(double n){
		_tps = n;
	}

	/*
	*Returns Transitions per Second for video
	*/
	public double getTPS(){
		return _tps;
	}

	/*
	*Sets all metrics for video (Face PFT, Caption PFT, Face<->Caption TPS)
	*/
	public void setData(double nhead, double ncaption, double tps){
		setHeadPFTime(nhead);
		setCaptionPFTime(ncaption);
		setTPS(tps);
	}

	/*
	*Finds the caption markup style by using the user number and video number along with the _video_order_file
	*/
	public Style findStyle(int userID, int num){
		Style out = Style.NO_CHANGE;
		num = (num > 12)? num - 12: num;

		try(BufferedReader br = new BufferedReader(new FileReader(_video_order_file))){
			String line;
			while((line = br.readLine()) != null){
				String[] arr = line.split(",");
				int user = Integer.parseInt(arr[0]);
				if(user == userID){
					String entry = arr[num];
					// System.out.println("FOUND User" + user + " Video " + entry);
					String style_str = (entry.charAt(2) == '_')? entry.substring(7, entry.length()-4): entry.substring(8, entry.length()-4);
					out = switchToStyle(style_str);
					// System.out.println("Style: " + out);
					return out;
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("USER: " + userID + " VIDEO: " + num + " NOT FOUND");
		return out;
	}

	/*
	*Changes string to Style if string matches a style enum.
	*/
	public Style switchToStyle(String style_str){
		Style out;
		switch(style_str){
			case "baseline":
				out = Style.NO_CHANGE;
				break;
			case "it":
				out = Style.ITALICS_U;
				break;
			case "color_c":
				out = Style.COLOR_C;
				break;
			case "del":
				out = Style.DEL_U;
				break;
			default :
				System.out.println("Could not find Style for: " + style_str);
				out = Style.NO_CHANGE;
		}
		return out;
	}
}
