"use strict";

import { AS, DCT, LDCV, PDGR, RDF, RDFS, XSD } from "@/lib/constants.js";

const N3 = require("n3");
const { namedNode, literal, defaultGraph, quad } = N3.DataFactory;

/**
 * Transforms a n3Store to a canonical turtle representation as a string.
 *
 * @param n3Store
 * @return string ttl
 */
export async function getTextTurtle(n3Store) {
  var ttl = "";
  var quads = n3Store.getQuads(null, null, null, null);
  for (var quad of quads) {
    ttl += "<" + quad.subject.value + "> ";
    ttl += "<" + quad.predicate.value + "> ";
    if (quad.object.termType === "Literal") {
      ttl +=
        '"' + quad.object.value + '"^^<' + quad.object.datatypeString + "> .\n";
    } else {
      ttl += "<" + quad.object.value + "> .\n";
    }
  }
  return ttl;
}

/**
 *
 * @param type
 * @param actor
 * @param target
 * @param object
 * @param seeAlso
 * @return n3Store
 */
export async function createActivityStreamItem(
  type,
  actor,
  target,
  object,
  seeAlso
) {
  var store = new N3.Store();
  var quads = [];
  quads.push(
    quad(
      namedNode(""),
      namedNode(RDF("type")),
      namedNode(AS(type)),
      defaultGraph()
    )
  );
  quads.push(
    quad(
      namedNode(""),
      namedNode(AS("actor")),
      namedNode(actor),
      defaultGraph()
    )
  );
  quads.push(
    quad(
      namedNode(""),
      namedNode(AS("target")),
      namedNode(target),
      defaultGraph()
    )
  );
  quads.push(
    quad(
      namedNode(""),
      namedNode(AS("object")),
      namedNode(object),
      defaultGraph()
    )
  );
  quads.push(
    quad(
      namedNode(""),
      namedNode(RDFS("seeAlso")),
      namedNode(seeAlso),
      defaultGraph()
    )
  );
  store.addQuads(quads);
  return { store: store, baseIRI: "" };
}

export async function createLinkedPedigreePart(actor, object, prevPart) {
  var store = new N3.Store();
  var quads = [];
  quads.push(
    quad(
      namedNode(""),
      namedNode(RDF("type")),
      namedNode(PDGR("Pedigree")),
      defaultGraph()
    )
  );
  if (prevPart === null || prevPart === undefined) {
    quads.push(
      quad(
        namedNode(""),
        namedNode(PDGR("hasPedigreeStatus")),
        namedNode(PDGR("Initial")),
        defaultGraph()
      )
    );
  } else {
    quads.push(
      quad(
        namedNode(""),
        namedNode(PDGR("hasPedigreeStatus")),
        namedNode(PDGR("Intermediate")),
        defaultGraph()
      )
    );
    quads.push(
      quad(
        namedNode(""),
        namedNode(PDGR("hasReceivedPedigree")),
        namedNode(prevPart),
        defaultGraph()
      )
    );
  }
  quads.push(
    quad(
      namedNode(""),
      namedNode(PDGR("pedigreeCreationTime")),
      literal(new Date().toISOString(), XSD("dateTime")),
      defaultGraph()
    )
  );
  quads.push(
    quad(
      namedNode(""),
      namedNode(PDGR("hasProductInfo")),
      namedNode(object),
      defaultGraph()
    )
  );
  // optional begin vv
  quads.push(
    quad(
      namedNode(""),
      namedNode(PDGR("hasTransactionInfo")),
      namedNode("#transactionInfo"),
      defaultGraph()
    )
  );
  quads.push(
    quad(
      namedNode(""),
      namedNode(PDGR("hasConsignmentInfo")),
      namedNode("#consignmentInfo"),
      defaultGraph()
    )
  );
  // optional end ^^
  quads.push(
    quad(
      namedNode(""),
      namedNode(DCT("publisher")),
      namedNode(actor),
      defaultGraph()
    )
  );
  quads.push(
    quad(
      namedNode(""),
      namedNode(LDCV("hasValidationInfo")),
      namedNode("http://aifb-ls3-vm4.aifb.kit.edu:9999/rdf/blockchainInfo"),
      defaultGraph()
    )
  );
  store.addQuads(quads);
  return { store: store, baseIRI: "" };
}
