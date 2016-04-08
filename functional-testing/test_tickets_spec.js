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


frisby.create('test get environment as logged in user').get(testProps.urlPrefix("server")).auth(testProps.user1, testProps.password1).inspectStatus(200).toss();

