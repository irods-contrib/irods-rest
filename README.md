# Project: iRODS Rest API
## Date: 1/25/2017
## Release Version: 4.1.10-0-RC1
## Git tag: 4.1.10.0-RC1

Release candidate for REST API including Ticket support

https://github.com/DICE-UNC/irods-rest

iRODS Rest API based on Jargon 4.1.10.0, certified against iRODS 3.0+ as well as iRODS Consortium 4.1.x releases up to 4.1.10, with provisional support for 4.2.x.  See included docs folder for comprehensive user documentation and install instructions

See https://github.com/DICE-UNC/irods-rest/issues for support and known issues


### Requirements

* Depends on Java 1.8+
* Built using Apache Maven2, see POM for dependencies
	
### Changes

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

#### add ticket support #9

REST API now supports the use of iRODS tickets, allowing anonymous, token based access to iRODS REST resources

#### Line endings in rules are stripped #54

Added tests for reported rule error (stripping line endings in rule text)

#### Downloaded files include parameters in name #48

Made sure file name is flattened in content disposition header per user request

#### Add SSL support for connection to resource server #40

Updated to jargon 4.1.10.0 with SSL support.  This necessitates the addition of two new properties in the /etc/irods-ext/irods-rest-properties

```
# NO_NEGOTIATION, CS_NEG_REFUSE, CS_NEG_REQUIRE, CS_NEG_DONT_CARE
ssl.negotiation.policy=CS_NEG_DONT_CARE
# jargon now supports checksum calculation for streaming uploads.  This does not currently verify, but does store if set to true
compute.checksum=false

```

So those need to be updated on your installation.  The compute checksum allows a checksum to be computed on the iRODS server after a file is uploaded, the ssl negotiation policy is used to configure the underlying jargon configuration for ssl negotiation in the same manner that it is set in the irods_environment.json file for icommands.

#### Docker deploy #60

A  Docker deploy option was added.  This involves a Dockerfile added to the project, along with a 'runit.sh' as the endpoint.  The Docker image is alpine:tomcat8. The image mounts the /etc/irods-ext/irods-rest.properties from the host system, as well as an optional cert directory that will cause the import of an SSL cert if you are working with self signed certs.  See the Docker.md file for Docker instructions.  Please give that a whirl and report results and thoughts.  


#### issue posting metada updates #69

Fixes for metadata update, removing old XML namespace semantics.
