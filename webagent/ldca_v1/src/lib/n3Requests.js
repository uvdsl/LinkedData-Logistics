"use strict";

var N3 = require("n3");

async function parseN3(text, baseIRI) {
  var store = new N3.Store();
  var parser = new N3.Parser({ baseIRI: stripFragment(baseIRI) });
  return new Promise((resolve, reject) => {
    parser.parse(text, (error, quad) => {
      if (error) reject(error);
      if (quad) store.addQuad(quad);
      else {
        console.log("### N3\t\t| Parsing completed\n" + baseIRI);
        resolve({ store: store, baseIRI: baseIRI });
      }
    });
  });
}

/**
 * Send a `XMLHttpRequest`: POST, uri, async providing `text/turtle`
 *
 * @param uri: the URI of the N3 to post to
 * @param body: the N3 to provide
 * @return Promise<String> of the created URI from the response `Location` header
 */
async function postN3(uri, body) {
  console.log("### N3\t\t| POST\n" + uri);
  return new Promise((resolve, reject) => {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", uri, true);
    xhr.setRequestHeader("Content-Type", "text/turtle");

    xhr.onreadystatechange = function() {
      if (this.readyState === XMLHttpRequest.DONE) {
        if (this.status === 201) {
          resolve(xhr.getResponseHeader("Location"));
        } else {
          console.error(xhr);
          reject(new Error("HTTP request failed: status != 201"));
        }
      }
    };

    xhr.send(body);
  });
}

/**
 * Send a `XMLHttpRequest`: POST to a hasher server, async,
 * providing `text/turtle` and baseURI header, accepting `text/turtle`
 *
 * @param uri: the URI of the rdf hasher server (the N3 to post to)
 * @param body: the N3 to provide
 * @param baseIRI: the baseURI header
 * @return Promise<n3Store> of the hashResponse-rdf-statement
 */
async function requestN3Hash(uri, body, baseIRI) {
  return new Promise((resolve, reject) => {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", uri, true);
    xhr.setRequestHeader("Accept", "text/turtle");
    xhr.setRequestHeader("Content-Type", "text/turtle");
    xhr.setRequestHeader("baseURI", baseIRI);

    xhr.onreadystatechange = function() {
      if (this.readyState === XMLHttpRequest.DONE) {
        if (this.status === 200) {
          resolve(xhr.responseText);
        } else {
          console.error(xhr);
          reject(new Error("HTTP request failed: status != 200"));
        }
      }
    };

    xhr.send(body);
  });
}

/**
 * Send a `XMLHttpRequest`: GET, uri, async requesting `text/turtle`
 *
 * @param uri: the URI of the N3 to get
 * @return Promise<String> of the responseText
 */
async function requestN3(uri) {
  return new Promise((resolve, reject) => {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", uri, true);
    xhr.setRequestHeader("Accept", "text/turtle");

    xhr.onreadystatechange = function() {
      if (this.readyState === XMLHttpRequest.DONE) {
        if (this.status === 200) {
          resolve(xhr.responseText);
        } else {
          console.error(xhr);
          reject(new Error("HTTP request failed: status != 200"));
        }
      }
    };

    xhr.send();
  });
}

/**
 * Send a `XMLHttpRequest`: DELETE, uri, async
 *
 * @param uri: the URI of the N3 to delete
 * @return void
 */
async function deleteN3(uri) {
  return new Promise((resolve, reject) => {
    var xhr = new XMLHttpRequest();
    xhr.open("DELETE", uri, true);

    xhr.onreadystatechange = function() {
      if (this.readyState === XMLHttpRequest.DONE) {
        if (this.status === 204) {
          resolve();
        } else {
          console.error(xhr);
          reject(new Error("HTTP request failed: status != 200"));
        }
      }
    };

    xhr.send();
  });
}

/**
 *
 * @param uri: the URI to strip from its fragment #
 * @return substring of the uri prior to fragment #
 */
function stripFragment(uri) {
  var indexOfFragment = uri.indexOf("#");
  if (indexOfFragment !== -1) {
    uri = uri.substring(0, indexOfFragment);
  }
  return uri;
}

/**
 *  Makes a XMLHttpRequest to get the N3 and parses it into a N3 Store.
 *
 * @param uri: the URI of the N3 to get
 * @returns: N3 Store
 */
async function getN3(uri) {
  console.log("### N3\t\t| GET\n" + uri);
  return requestN3(uri).then(responseBody => parseN3(responseBody, uri));
}

/**
 * Send a `XMLHttpRequest`: POST to a hasher server, async,
 * providing `text/turtle` and baseURI header, accepting `text/turtle`
 *
 * @param uri: the URI of the rdf hasher server (the N3 to post to)
 * @param body: the N3 to provide
 * @param baseIRI: the baseURI header
 * @return Promise<n3Store> of the hashResponse-rdf-statement
 */
async function hashN3(uri, body, baseIRI) {
  console.log("### N3\t\t| HASH\n" + uri);
  return requestN3Hash(uri, body, baseIRI).then(responseBody =>
    parseN3(responseBody, baseIRI)
  );
}

module.exports = {
  getN3,
  postN3,
  hashN3,
  deleteN3
};
