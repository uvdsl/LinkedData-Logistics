"use strict";

import { getN3 } from "@/lib/n3Requests.js";
import {
  AS,
  ETHON,
  FOAF,
  LDCV,
  LDP,
  PDGR,
  RDF,
  RDFS
} from "@/lib/constants.js";

var N3 = require("n3");
const { namedNode } = N3.DataFactory;

async function getWallletAddress(n3Store, baseIRI) {
  var accountURI = n3Store.getQuads(
    namedNode(baseIRI),
    ETHON("controlsAccount"),
    null
  )[0].object.value;
  if (!accountURI.startsWith("#")) {
    // if not IRI, but external resource
    return getN3(accountURI).then(
      n3 =>
        n3.store.getQuads(namedNode(n3.baseIRI), ETHON("address"), null)[0]
          .object.value
    );
  }
  // if it is IRI
  return n3Store.getQuads(namedNode(accountURI), ETHON("address"), null)[0]
    .object.value;
}

export async function getProfileDataFromN3(n3Store, baseIRI) {
  var name = n3Store.getQuads(namedNode(baseIRI), FOAF("name"), null)[0].object
    .value;
  var depiction = n3Store.getQuads(
    namedNode(baseIRI),
    FOAF("depiction"),
    null
  )[0].object.value;
  var inbox = n3Store.getQuads(namedNode(baseIRI), LDP("inbox"), null)[0].object
    .value;
  var outbox = n3Store.getQuads(namedNode(baseIRI), LDP("outbox"), null)[0]
    .object.value;
  var docbox = n3Store.getQuads(namedNode(baseIRI), LDP("docbox"), null)[0]
    .object.value;
  var wallet = await getWallletAddress(n3Store);
  var ledger = n3Store.getQuads(
    namedNode(baseIRI),
    LDCV("hasDefaultConnection"),
    null
  )[0].object.value;
  var hasher = n3Store.getQuads(
    namedNode(baseIRI),
    LDCV("hasHasherServer"),
    null
  )[0].object.value;
  return {
    uri: baseIRI,
    name: name,
    depiction: depiction,
    inbox: inbox,
    outbox: outbox,
    docbox: docbox,
    wallet: wallet,
    ledger: ledger,
    hasher: hasher
  };
}

export function getBoxContentFromN3(n3Store, baseIRI) {
  return n3Store
    .getQuads(namedNode(baseIRI), LDP("contains"), null)
    .map(quad => quad.object.value);
}

export function getItemContentFromN3(n3Store, baseIRI) {
  var type = n3Store.getQuads(namedNode(baseIRI), RDF("type"), null)[0].object
    .value;
  var actor = n3Store.getQuads(namedNode(baseIRI), AS("actor"), null)[0].object
    .value;
  var target = n3Store.getQuads(namedNode(baseIRI), AS("target"), null)[0]
    .object.value;
  var object = n3Store.getQuads(namedNode(baseIRI), AS("object"), null)[0]
    .object.value;
  var seeAlso = n3Store.getQuads(namedNode(baseIRI), RDFS("seeAlso"), null)[0]
    .object.value;
  return {
    uri: baseIRI,
    type: type,
    actor: actor,
    target: target,
    object: object,
    seeAlso: seeAlso
  };
}

export function getLabelFromN3(n3Store, baseIRI) {
  return n3Store.getQuads(namedNode(baseIRI), RDFS("label"), null)[0].object
    .value;
}

// hash query in LPP. couldn't be bothered that day...

export function getPedigreeInfo(n3Store, baseIRI) {
  var status = n3Store.getQuads(
    namedNode(baseIRI),
    PDGR("hasPedigreeStatus"),
    null
  )[0];

  if (status === undefined) {
    baseIRI += "#";
  }
  status = n3Store.getQuads(
    namedNode(baseIRI),
    PDGR("hasPedigreeStatus"),
    null
  )[0];
  status = status.object.value;

  var prevPart = n3Store.getQuads(
    namedNode(baseIRI),
    PDGR("hasReceivedPedigree"),
    null
  )[0];

  if (prevPart !== undefined) {
    prevPart = prevPart.object.value;
  }

  return { prevPart: prevPart, status: status };
}

export async function getContractAddress(n3Store, baseIRI) {
  var validationIRI = n3Store.getQuads(
    namedNode(baseIRI),
    LDCV("hasValidationInfo"),
    null
  )[0];
  if (validationIRI === undefined) {
    validationIRI = n3Store.getQuads(
      namedNode(baseIRI + "#"),
      LDCV("hasValidationInfo"),
      null
    )[0];
  }
  validationIRI = validationIRI.object.value;
  return getN3(validationIRI)
    .then(n3 => {
      var result = n3.store.getQuads(
        namedNode(n3.baseIRI),
        LDCV("hasContractAccount"),
        null
      )[0];
      if (result === undefined) {
        result = n3.store.getQuads(
          namedNode(n3.baseIRI + "#"),
          LDCV("hasContractAccount"),
          null
        )[0];
      }
      return result.object.value;
    })
    .then(getN3)
    .then(n3 => {
      var result = n3.store.getQuads(
        namedNode(n3.baseIRI),
        ETHON("address"),
        null
      )[0];
      if (result === undefined) {
        result = n3.store.getQuads(
          namedNode(n3.baseIRI + "#"),
          ETHON("address"),
          null
        )[0];
      }
      return result.object.value;
    });
}
