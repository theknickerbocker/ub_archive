/*
 * This is an example that demonstrates how to connect to the EyeX Engine and subscribe to the fixation data stream.
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 *
 * Code base is from an example in the Tobii EyeX SDK. Code has been restructured and functionality added.
 * Code now takes in number of viewers and number of videos per viewer as arguments. This allowed for the program
 * to be run on a server and keep track of users and the fixations of any videos they watch individually.
 * AUTHOR: Kevin Rathbun
 */

#include <Windows.h>
#include <stdio.h>
#include <conio.h>
#include <assert.h>
#include <string.h>
#include <iostream>
#include <fstream>
#include <time.h>
#include <math.h>
#include<direct.h>
#include <chrono>
#include "include/eyex/EyeX.h"

#pragma comment (lib, "Tobii.EyeX.Client.lib")

// ID of the global interactor that provides our data stream; must be unique within the application.
static const TX_STRING InteractorId = "Rainbow Dash";

// global variables
static TX_HANDLE g_hGlobalInteractorSnapshot = TX_EMPTY_HANDLE;
static std::string _videoNum;
static std::string _userID;

/*
 * Initializes g_hGlobalInteractorSnapshot with an interactor that has the Fixation Data behavior.
 */
BOOL InitializeGlobalInteractorSnapshot(TX_CONTEXTHANDLE hContext)
{
	TX_HANDLE hInteractor = TX_EMPTY_HANDLE;
	TX_FIXATIONDATAPARAMS params = { TX_FIXATIONDATAMODE_SENSITIVE };
	BOOL success;

	success = txCreateGlobalInteractorSnapshot(
		hContext,
		InteractorId,
		&g_hGlobalInteractorSnapshot,
		&hInteractor) == TX_RESULT_OK;
	success &= txCreateFixationDataBehavior(hInteractor, &params) == TX_RESULT_OK;

	txReleaseObject(&hInteractor);

	return success;
}

/*
 * Callback function invoked when a snapshot has been committed.
 */
void TX_CALLCONVENTION OnSnapshotCommitted(TX_CONSTHANDLE hAsyncData, TX_USERPARAM param)
{
	// check the result code using an assertion.
	// this will catch validation errors and runtime errors in debug builds. in release builds it won't do anything.

	TX_RESULT result = TX_RESULT_UNKNOWN;
	txGetAsyncDataResultCode(hAsyncData, &result);
	assert(result == TX_RESULT_OK || result == TX_RESULT_CANCELLED);
}

/*
 * Callback function invoked when the status of the connection to the EyeX Engine has changed.
 */
void TX_CALLCONVENTION OnEngineConnectionStateChanged(TX_CONNECTIONSTATE connectionState, TX_USERPARAM userParam)
{
	switch (connectionState) {
	case TX_CONNECTIONSTATE_CONNECTED: {
			BOOL success;
			printf("The connection state is now CONNECTED (We are connected to the EyeX Engine)\n");
			// commit the snapshot with the global interactor as soon as the connection to the engine is established.
			// (it cannot be done earlier because committing means "send to the engine".)
			success = txCommitSnapshotAsync(g_hGlobalInteractorSnapshot, OnSnapshotCommitted, NULL) == TX_RESULT_OK;
			if (!success) {
				printf("Failed to initialize the data stream.\n");
			}
			else
			{
				printf("Waiting for fixation data to start streaming...\n");
			}
		}
		break;

	case TX_CONNECTIONSTATE_DISCONNECTED:
		printf("The connection state is now DISCONNECTED (We are disconnected from the EyeX Engine)\n");
		break;

	case TX_CONNECTIONSTATE_TRYINGTOCONNECT:
		printf("The connection state is now TRYINGTOCONNECT (We are trying to connect to the EyeX Engine)\n");
		break;

	case TX_CONNECTIONSTATE_SERVERVERSIONTOOLOW:
		printf("The connection state is now SERVER_VERSION_TOO_LOW: this application requires a more recent version of the EyeX Engine to run.\n");
		break;

	case TX_CONNECTIONSTATE_SERVERVERSIONTOOHIGH:
		printf("The connection state is now SERVER_VERSION_TOO_HIGH: this application requires an older version of the EyeX Engine to run.\n");
		break;
	}
}

/*
 *Used in main method to create directories for data docs
 */
void initializeDirectories(){
	std::string tobii_dir_name= "Tobii_Log\\";
	std::string ts_dir_name = "Tobii_Log\\timestamps";
	mkdir(tobii_dir_name.c_str());
	mkdir(ts_dir_name.c_str());
}

/*
 * Gets current date in format: MM/DD/YYYY GMT
 */
using namespace std::chrono;
std::string getDate(){
	time_t t = time(0);
	struct tm *now = gmtime(&t);
	int day = now->tm_mday;
	int month = (now->tm_mon + 1);
	int year = now->tm_year + 1900;

	std::string sday = (day < 10)? "0" + std::to_string(day) : std::to_string(day);
	std::string smonth = (month < 10)? "0" + std::to_string(month) : std::to_string(month);

	std::string date = smonth + "/" + sday + "/" + std::to_string(year);
	return date;
}

/*
 *Gets the current timestamp in current milliseconds GMT
 */
std::string getTimeStamp(){
	time_t t = time(0);
	struct tm *now = gmtime(&t);
	int yday = now->tm_yday;
	int year = now->tm_year + 1900;

	milliseconds ms = duration_cast<milliseconds>(system_clock::now().time_since_epoch());
	int milli = ms.count() - (((year - 1970)*floor(1000*60*60*24*365.25)) + ((yday)*floor(1000*60*60*24)));
	std::string time_td = std::to_string(milli);
	return time_td;
}

/*
 * Handles an event from the fixation data stream.
 */
void OnFixationDataEvent(TX_HANDLE hFixationDataBehavior)
{
	TX_FIXATIONDATAEVENTPARAMS eventParams;
	TX_FIXATIONDATAEVENTTYPE eventType;
	std::string eventDescription;

	if (txGetFixationDataEventParams(hFixationDataBehavior, &eventParams) == TX_RESULT_OK) {
		eventType = eventParams.EventType;

		std::ofstream userTS;
		time_t  timev;
		userTS.open("Tobii_Log\\timestamps\\user" + _userID + "\\" + _videoNum + ".txt", std::ios_base::app);

		if(eventType == TX_FIXATIONDATAEVENTTYPE_END){
			eventDescription = "END";
			userTS << "," << eventDescription << "," << getTimeStamp() << std::endl;
			std::cout << "," << eventDescription << "," << getTimeStamp() << std::endl;
			}
			else if(eventType == TX_FIXATIONDATAEVENTTYPE_BEGIN){
				eventDescription = "BEG";
				userTS << "Fixation " << getDate() << "," << eventParams.X << "," << eventParams.Y << "," << eventDescription << "," << getTimeStamp();
				std::cout << "Fixation " << getDate() << "," << eventParams.X << "," << eventParams.Y << "," << eventDescription << "," << getTimeStamp();
			}

			userTS.close();

	// printf("Fixation %s: (%.1f, %.1f) timestamp %.0f ms\n", eventDescription, eventParams.X, eventParams.Y, eventParams.Timestamp);
	}
	else {
		printf("Failed to interpret fixation data event packet.\n");
	}
}

/*
 * Writes string num onto the end of the file "filename"
 */
void writeFileNum(std::string filename, std::string num){
	std::ofstream os;
	os.open(filename, std::ios_base::app);
	if (os.is_open()){
		os << num + "\n";
	}
	os.close();
}

/*
 * Returns the last line of file "filename"
 */
std::string getFileNum(std::string filename){
	std::ifstream is;
	is.open(filename);
	std::string line;
	std::string out = "";
	if (is.is_open()){
		while (std::getline(is,line)){
				if(line != "") out = line;
		}
	}

	return out;
}

/*
 * Works but isn't very flexible and should be changed. Written and not changed due to time constraints of project
 * For a string with format "##" it will return string "##" + 1
 * EX:
 * incrementFileNum("02") -> "03"
 */
std::string incrementFileNum(std::string filename){
	char number1 = '0';
	char number2 = '0';
	std::string out = "";

	std::string line = getFileNum(filename);
	if(line != ""){
		number1 = line[0];
		number2 = line[1];
	}
	if (number2 == '9'){
		number2 = '0';
		number1++;
	}
	else{
		number2++;
	}
	out = out + ((char)number1) + ((char)number2);
	return out;
}

/*
 * Returns an array with:
 * current video number in index 0
 * current user number in index 1
 *
 * VPS (Videos per User) determines how many videos are watched before a new user is created
 */
std::string* getWatchInfo(std::string vps){
	std::string* out = new std::string[2];
	std::string vn_filename = "Tobii_Log\\video_num.txt";
	std::string u_filename = "Tobii_Log\\users.txt";

	std::string vid_num = incrementFileNum(vn_filename);
	std::string user_num = getFileNum(u_filename);
	vid_num = (vid_num == vps)? "01": vid_num;
	if(vid_num == "01"){
		user_num = incrementFileNum(u_filename);
		writeFileNum(u_filename, user_num);
		std::string dir_name = "Tobii_Log\\timestamps\\user" + user_num;
		mkdir(dir_name.c_str());
	}
	writeFileNum(vn_filename, vid_num);
	out[0] = vid_num;
	out[1] = user_num;
	return out;
}

/*
 * Callback function invoked when an event has been received from the EyeX Engine.
 */
void TX_CALLCONVENTION HandleEvent(TX_CONSTHANDLE hAsyncData, TX_USERPARAM userParam)
{
	TX_HANDLE hEvent = TX_EMPTY_HANDLE;
	TX_HANDLE hBehavior = TX_EMPTY_HANDLE;

	txGetAsyncDataContent(hAsyncData, &hEvent);

	// NOTE. Uncomment the following line of code to view the event object. The same function can be used with any interaction object.
	//OutputDebugStringA(txDebugObject(hEvent));

	if (txGetEventBehavior(hEvent, &hBehavior, TX_BEHAVIORTYPE_FIXATIONDATA) == TX_RESULT_OK) {
		OnFixationDataEvent(hBehavior);
		txReleaseObject(&hBehavior);
	}

	// NOTE since this is a very simple application with a single interactor and a single data stream,
	// our event handling code can be very simple too. A more complex application would typically have to
	// check for multiple behaviors and route events based on interactor IDs.

	txReleaseObject(&hEvent);
}

/*
 * Application entry point.
 * Argument 1 is the number of videos per user
 */
int main(int argc, char* argv[])
{
	TX_CONTEXTHANDLE hContext = TX_EMPTY_HANDLE;
	TX_TICKET hConnectionStateChangedTicket = TX_INVALID_TICKET;
	TX_TICKET hEventHandlerTicket = TX_INVALID_TICKET;
	BOOL success;

	initializeDirectories();

	//Get userID
	std::string* numArr = getWatchInfo(argv[1]);
	_videoNum = numArr[0];
	_userID = numArr[1];
	delete[] numArr;

	// initialize and enable the context that is our link to the EyeX Engine.
	success = txInitializeEyeX(TX_EYEXCOMPONENTOVERRIDEFLAG_NONE, NULL, NULL, NULL, NULL) == TX_RESULT_OK;
	success &= txCreateContext(&hContext, TX_FALSE) == TX_RESULT_OK;
	success &= InitializeGlobalInteractorSnapshot(hContext);
	success &= txRegisterConnectionStateChangedHandler(hContext, &hConnectionStateChangedTicket, OnEngineConnectionStateChanged, NULL) == TX_RESULT_OK;
	success &= txRegisterEventHandler(hContext, &hEventHandlerTicket, HandleEvent, NULL) == TX_RESULT_OK;
	success &= txEnableConnection(hContext) == TX_RESULT_OK;

	// let the events flow until a key is pressed.
	if (success) {
		printf("Initialization was successful.\n");
	} else {
		printf("Initialization failed.\n");
	}
	printf("Press any key to exit...\n");
	_getch();
	printf("Exiting.\n");

	// disable and delete the context.
	txDisableConnection(hContext);
	txReleaseObject(&g_hGlobalInteractorSnapshot);
	success = txShutdownContext(hContext, TX_CLEANUPTIMEOUT_DEFAULT, TX_FALSE) == TX_RESULT_OK;
	success &= txReleaseContext(&hContext) == TX_RESULT_OK;
	success &= txUninitializeEyeX() == TX_RESULT_OK;
	if (!success) {
		printf("EyeX could not be shut down cleanly. Did you remember to release all handles?\n");
	}

	return 0;
}
