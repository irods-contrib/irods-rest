/**
 * test_tickets.js
 * Frisby test spec for tickets against live server
 */

var frisby = require('frisby');
var testProps = require('./testing_props.js');

// Global setup for all tests
frisby.globalSetup({
  request: {
    headers:{'Accept': 'application/json'},
    inspectOnFailure: true
  }
});

var testCollForCreate = "/RESTtestCreateCollAsLoggedInUser";

frisby.create('test get environment as logged in user').get(testProps.urlPrefix("server")).auth(testProps.user1, testProps.password1).expectStatus(200).toss();

frisby.create('test create collection as logged in user - RESTtestCreateCollASLoggedInUser').put(testProps.urlPrefix("collection" + testProps.userHome(testProps.zone, testProps.user1) + testCollForCreate )).auth(testProps.user1, testProps.password1).expectStatus(200).inspectJSON()
    .toss();

frisby.create('test get info on collection I just created - RESTtestCreateCollASLoggedInUser').get(testProps.urlPrefix("collection" + testProps.userHome(testProps.zone, testProps.user1) + testCollForCreate )).auth(testProps.user1, testProps.password1).expectStatus(200).inspectJSON()
    .toss();

/**
 * TODO: fix ref https://github.com/DICE-UNC/irods-rest/issues/50
 */
frisby.create('test get info on collection that doesnt exist get a 404').get(testProps.urlPrefix("collection" + testProps.userHome(testProps.zone, testProps.user1) + testCollForCreate + "idontexistbtw" )).auth(testProps.user1, testProps.password1).expectStatus(500)
    .toss();





