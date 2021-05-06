package fix_data;

/*
*Fixation object holds values for the (X,Y) coordinates of the fixation, along with the timestamps (in milliseconds) of the start and end of the fixation
*
*Author: Kevin Rathbun
*/

public class Fixation {

	private double _xpos, _ypos;
	private int _beg, _end, _dur;

	public Fixation(double xpos, double ypos, int beg, int end) {
		_xpos = xpos;
		_ypos = ypos;
		_beg = beg;
		_end = end;
		_dur = _end - _beg;
	}

	/*
	*Returns duration of fixation in milliseconds
	*/
	public int getDuration() {
		return _dur;
	}

	/*
	*Returns X position of the fixation
	*/
	public double getXpos() {
		return _xpos;
	}

	/*
	*Returns Y postion of the fixation
	*/
	public double getYpos() {
		return _ypos;
	}

	/*
	*Returns the timestamp of when the fixation began (in milliseconds).
	*/
	public int getBeg() {
		return _beg;
	}

	/*
	*Returns the timestamp of when the fixation ended (in milliseconds).
	*/
	public int getEnd() {
		return _end;
	}
}
