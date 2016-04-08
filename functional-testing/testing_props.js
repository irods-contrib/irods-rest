/**
 * Holder (hard coded at first) to handle properties to use in testing
 * Created by mconway on 4/8/16.
 */


exports.host = "localhost";
exports.port = "8080";
exports.user1 = "test1";
exports.password1 = "test";
exports.anonymous = "anonymous";
exports.protocol = 'http://';
exports.context = "/irods-rest/rest";
exports.zone = "zone1";
exports.ticket1 = "frisby-test-tkt1";

/**
 * Return the url up to the given resource (protocol://host:port/context/resource)
 * @param resource rest resource in the URL string
 */
exports.urlPrefix = function(resource)  {
    if (!resource) {
        throw("missing resource");
    }

    return exports.protocol + exports.host + ":" + exports.port + exports.context +"/" + resource;

};

/**
 * Get the user home path
 * @param zone
 * @param user
 * @returns {string}
 */
exports.userHome = function(zone, user) {

    if (!zone) {
        throw("missing zone");
    }

    if (!user) {
        throw("missing user");
    }

    return "/" + zone + "/home/" + user;


};