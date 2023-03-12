# Internship Test Task - Location System
This project is an implementation of a location system web server that allows users to create locations, share locations with other users, manage access to shared locations, and get all locations available for the user.

## Requirements
To run this project, you need to have Java 8 (OpenJDK), Gradle 6.8.3, and Spring Boot 2.6.4 installed on your system. The project also uses Spock Framework 1.3-groovy-2.4 for testing.

## How to run the project
To run the project, open the terminal and navigate to the project's root directory. Then run the following command: `./gradlew bootRun` or `gradlew bootRun` 

## Functionality
The web-server provides the following functionalities:

* Register a new user account by email
* Create a location
* Share a location with another user from the system
* Get all friend users on the location
* Manage access for friend user on owner’s location
* Get all locations available for the user, including shared locations

## Testing
The project has unit tests and integration tests implemented using Spock Framework.
