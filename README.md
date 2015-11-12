* Project: iRODS Rest API
* Date: 6/29/2015
* Release Version: 4.0.2.4-RC1	
* Git tag: 4.0.2.4-rc1

https://github.com/DICE-UNC/irods-rest

iRODS Rest API based on Jargon 4.0.2.4, certified against iRODS 3.0+ as well as iRODS Consortium 4.1.x releases up to 4.1.7.  See included docs folder for comprehensive user documentation and install instructions

See https://github.com/DICE-UNC/irods-rest/issues for support and known issues


### Requirements

* Depends on Java 1.7+
* Built using Apache Maven2, see POM for dependencies


### Bug Fixes

### Features

#### #2 CORS header support

Add support for Cross Origin Resource Sharing through customizable configuration

#### Add PAM support #6

Add support for PAM authentication through customizable configuration

#### add temp password for user #10

New /user/userName/temppassword signatures available to obtain a temporary iRODS password, including in admin mode.

#### use packing i/o for stream performance #14

Added optional use of packing input and output streams for upload and download.  This uses a simple read-ahead and write-behind buffer approach to optimize iRODS buffer sizes.  The behavior may be controlled by adjusting the configuration property for 'utilizePackingStreams' in RestConfig.

#### use an etc file for production deployment #30

Added a /etc/irods-ext/irods-rest-properties file that can configure settings, as per the example in the irods-rest project.  This allows deployment as a pre-packaged war and configuration and settings will be controlled by these etc properties.

Note that for testing purposes, these properties are generated locally by running mvn install, and the unit tests will refer to the test-irods-rest.properties found in src/test/resources.  This also eases integration with Jenkins for CI testing purposes.

####  add/test PAM login support #31 

Added unit test for PAM auth, added a capability to override rest api configs and prepend PAM* or STANDARD* to th euser id
in basic auth to force standard iRODS auth or PAM auth behavior.

See the docs/ section for installation and configuration instructions, as well as notes on using PAM and Standard iRODS Auth.

