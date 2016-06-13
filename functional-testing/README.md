# Functional testing framework

This functional test framework utilizes the Frisbyjs framework: http://frisbyjs.com/ and includes tools to launch the rest api and prepare test data.
 
This is an experimental version at the moment so it will involve hand configuring certain aspects of testing. The current purpose is to verify the behavior of ticket and other filter operations


Frisby is built on top of the Jasmine BDD framework, and uses the jasmine-node
test runner to run spec tests.


#### Install jasmine-node

npm install -g jasmine-node

#### File naming conventions

Files must end with spec.js to run with jasmine-node.

Suggested file naming is to append the filename with _spec, like mytests_spec.js and moretests_spec.js

#### Run it from the CLI

cd your/project

jasmine-node .