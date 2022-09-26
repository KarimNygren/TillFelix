# TillFelix
A prayer time app for muslims and a Apache CXF Rest Service for Encrypted password management with JS/HTML/CSS frontend


Password Manager:

Service is using protocol HTTP and uses port 8080.

Service is using:

APACHE CXF as a service framework.
SQLITE version for persistance of passwords.
SLF4J for logging.
AES Encryption for encryption




Prayer Application:

Uses AlAdhan API. Default city/location is set to Kista. The prayer times changes every day for every location so instead of making API calls every time the application
is opened, the values are saved to file and then loaded from file when the app is started/resumed again. If there is a new day/date, a new API call will be made.

API calls will also be made on Swipe To Refresh and when changing date in Calendar View.
