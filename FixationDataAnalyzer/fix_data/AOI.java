package fix_data;

/*
*AOI (Area of Interest) are areas where researchers wish to record fixations. So basically these are the screen areas that matter whether a participant is looking at.
*AOI object keeps track of the name of the AOI, upper left corner (X,Y) location, and the width and height of the area.
*
*Author: Kevin Rathbun
*/
public class AOI {

	private String _name;
	private double _xpos, _ypos, _width, _height;

	public AOI(String name, double xpos, double ypos, double x, double y) {
		_name = name;
		_xpos = xpos;
		_ypos = ypos;
		_width = x;
		_height = y;
	}

	/*
	*Returns name of AOI
	*/
	public String getName(){
		return _name;
	}

	/*
	*Returns X coordinate of upper left corner of AOI
	*/
	public double getXpos() {
		return _xpos;
	}

	/*
	*Returns Y coordinate of upper left corner of AOI
	*/
	public double getYpos() {
		return _ypos;
	}

	/*
	*Returns width (length on X-axis) of the AOI
	*/
	public double getWidth() {
		return _width;
	}

	/*
	*Returns height (length on Y-axis) of the AOI
	*/
	public double getHeight() {
		return _height;
	}

	/*
	*Returns whether the given location is in the AOI
	*/
	public boolean isInAOI(double xpos_t, double ypos_t) {
		double xend = _xpos + _width;
		double yend = _ypos + _height;
		// System.out.println("POS: (" + xpos_t+ "," + ypos_t + ") AOI: X(" + _xpos + " - " + xend + ") Y(" + _ypos + " - " + yend + ")");
		// System.out.println((_xpos <= xpos_t && xpos_t <= xend && _ypos <= ypos_t && ypos_t <= yend));
		return (_xpos <= xpos_t && xpos_t <= xend && _ypos <= ypos_t && ypos_t <= yend);
	}

	/*
	*Returns whether the given fixation is in the AOI 
	*/
	public boolean isInAOI(Fixation f){
		return isInAOI(f.getXpos(),f.getYpos());
	}
}
