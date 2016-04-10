# Detailed description of the HappyCycling Android application

The application is divided into four layouts, from which the "activity_gpstracker.xml" is the most important. The application starts with the "apploader.xml" where the user is logging with his/her credentials. 
After successful logging, the "activity_gpstracker.xml" is started which is divided into three TabHosts (Tracking, Table, History). At the bottom of the screen on all three Tabs is the "Tracking on/off" button, which is working independently from the other tasks performed by the application. By clicking this button, the user activates his/her GPS locator and the application periodically every 8 seconds stores locally the phone's current location.

The first tab, Tracking, is where the user can check the current temperature in the city (by requesting it from "http://api.openweathermap.org").  
While the tracking mode is on, the user on this tab can also see the calculated distance covered, as well as the points obtained by the passed distance , the time elapsed and the CO2 saved (compared with a car drive). By clicking the button "Upload", the user is uploading the obtained points to the server.

The second tab, Table, is where the user can fetch the ranking table of obtained points by the community and see where is he/she on the score list. Also, by clicking the button "Get voucher", the user can see all the promotion vouchers for discounts at some shops together with their price expressed in points. If the user has enough points, a voucher may be booked and a random generated code will appear which the user should use in order to get his voucher at the shop. 

The third tab is the History area, where the user can see all of his biking routes which are stored locally. 

# The architecture design
![alt tag](https://github.com/dimcey/HappyCycling/blob/master/Architecture.png)

The project was developed according to the SOA design principles:

Loose Coupling – Independence and interoperability

Service Abstraction - Hide underlying details

Service Reusability - Front and back end (and their services) could be reused elsewhere

Service Autonomy - All the components are autonomous

Service Composability - The front end is composed of services

Service Interoperability - The API communications are done through REST

Service Statelessness - Client and server side contain only the informations they need to operate
