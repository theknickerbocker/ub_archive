package fix_data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
*User Object holds ID number, how many videos they watched, the path to the directory with user's fixation data, and an array of all the videos the user watched.
*
*Author: Kevin Rathbun
*/

public class User {

	private String _ID;
	private int _vid_num;
	private String _fix_dirpath;
	private String _vid_filepath;
	private Video[] _videolist;

	public User(String userID, int vid_num, String dirpath, String vid_filepath, String vidorder_filepath){
		_ID = userID;
		_vid_num = vid_num;
		_fix_dirpath = dirpath + "user" + userID + "/";
		_vid_filepath = vid_filepath;
		_videolist = makeVideoList(userID, vid_num, vid_filepath, vidorder_filepath);
	}

	/**
	*Returns ID number in string format "##"
	*/
	public String getID(){
		return _ID;
	}

	/**
	*Returns number of videos the user watched
	*/
	public int getVideoNumber(){
		return _vid_num;
	}

	/**
	*Returns the path to the directory that holds the user's fixation data as a string.
	*/
	public String getFixationDirectoryPath(){
		return _fix_dirpath;
	}

	/**
	*Returns the path to the file that specifies the start and stop times of each video the user watched
	*/
	public String getVideoFilePath(){
		return _vid_filepath;
	}

	/**
	*Returns the list of video objects that correspond to the videos the user watched
	*/
	public Video[] getVideoList(){
		return _videolist;
	}

	/**
	*
	*/
	public Video getVideo(int i){
		return _videolist[i];
	}

	/**
	*Creates an array of videos based on the user's ID, number of videos, and filepath to the videos start and stop times. Basically creates an array of the videos the user watched
	*/
	public static Video[] makeVideoList(String userID, int vid_num, String vid_filepath, String vidorder_filepath){
		Video[] videos = new Video[vid_num];
		File vidfile = new File(vid_filepath);
		try(BufferedReader br = new BufferedReader(new FileReader(vidfile))){
			for(int i = 0; i < vid_num; i++){
				// System.out.println(br.readLine());
				// System.out.println(br.readLine());
				String[] line1 = br.readLine().split(",");
				String[] line2 = br.readLine().split(",");
				int strt;
				int stop;
				try{
					strt = Integer.parseInt(line1[3]);
					stop = Integer.parseInt(line2[3]);
				} catch (IndexOutOfBoundsException e){
					strt = 0;
					stop = 0;
					System.out.println(vid_filepath +"	Video" + (i + 1) + " has missing start or stop times");
				}
				// System.out.println("Video" + (i+1) + " STRT: " + strt + " STOP: " + stop);
				videos[i] = new Video(userID,i + 1,strt,stop, vidorder_filepath);
				// System.out.println("Video" + videos[i].getNumber() + " STRT: " + videos[i].getStart() + " STOP: " + videos[i].getStop());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return videos;
	}
}
