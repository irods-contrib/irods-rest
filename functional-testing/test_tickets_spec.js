/**
 * test_tickets.js
 * Frisby test spec for tickets against live server
 */

var frisby = require('frisby');
var testProps = require('./testing_props.js');

var fs = require('fs');
var path = require('path');
var FormData = require('form-data');

// Global setup for all tests
frisby.globalSetup({
  request: {
    headers:{'Accept': 'application/json'},
    inspectOnFailure: true
  }
});

var testCollForCreate = "/RESTtestCreateCollAsLoggedInUser";
var ticketFolder =  testProps.userHome(testProps.zone, testProps.user1) + testCollForCreate;
var ticketString1 = "RESTtest-ticket1";

/*

frisby.create('test get environment as logged in user').get(testProps.urlPrefix("server")).auth(testProps.user1, testProps.password1).expectStatus(200).toss();

frisby.create('test create collection as logged in user - RESTtestCreateCollASLoggedInUser').put(testProps.urlPrefix("collection" + testProps.userHome(testProps.zone, testProps.user1) + testCollForCreate )).auth(testProps.user1, testProps.password1).expectStatus(200).inspectJSON()
    .toss();

frisby.create('test get info on collection I just created - RESTtestCreateCollASLoggedInUser').get(testProps.urlPrefix("collection" + testProps.userHome(testProps.zone, testProps.user1) + testCollForCreate )).auth(testProps.user1, testProps.password1).expectStatus(200).inspectJSON()
    .toss();

    */

/**
 * TODO: fix ref https://github.com/DICE-UNC/irods-rest/issues/50
 */

/*
frisby.create('test get info on collection that doesnt exist get a 404').get(testProps.urlPrefix("collection" + testProps.userHome(testProps.zone, testProps.user1) + testCollForCreate + "idontexistbtw" )).auth(testProps.user1, testProps.password1).expectStatus(500)
    .toss();

*/
frisby.create("delete a ticket on your collection").delete(testProps.urlPrefix("ticket/" + ticketString1)).auth(testProps.user1, testProps.password1).expectStatus(204)
    .toss();


frisby.create("add a ticket on your collection").post(testProps.urlPrefix("ticket"), {
        mode: 'read',
        object_path: ticketFolder,
        ticket_string: ticketString1}
    , {json: true},{ headers: { "Content-Type": "application/json"}}).auth(testProps.user1, testProps.password1).expectStatus(200).inspectRequest().inspectBody()
    .expectHeaderContains('Content-Type', 'application/json').inspectJSON()
    .toss();

var form = new FormData();

form.append('uploadFile',fs.createReadStream(testProps.testfile1Path()), {
    knownLength: fs.statSync(testProps.testfile1Path()).size         // we need to set the knownLength so we can call  form.getLengthSync()
});

var uploadFileName = "upload1.txt";

frisby.create('file upload with ticket, anonymous')
    .post(testProps.urlPrefix("fileContents" + ticketFolder + "/" + uploadFileName + "?ticket=" + ticketString1), form, {
        json: false,
        headers: {
            'content-type': 'multipart/form-data; boundary=' + form.getBoundary(),
            'content-length': form.getLengthSync(),
        }
    })
    .expectStatus(200)
    .inspectJSON()
    .toss();