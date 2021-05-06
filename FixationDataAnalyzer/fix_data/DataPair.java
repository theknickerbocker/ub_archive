package fix_data;

import java.util.HashMap;

/**
*Data structure whose pupose is to return two values from other functions.
*
*Author: Kevin Ratbun
*/
public class DataPair<T1, T2>{

  private T1 _data1;
  private T2 _data2;


  public DataPair(T1 data1, T2 data2){
    _data1 = data1;
    _data2 = data2;
  }

  /*
  *Returns first data object.
  */
  public T1 getData1(){
    return _data1;
  }

  /*
  *Returns second data object.
  */
  public T2 getData2(){
    return _data2;
  }
}
