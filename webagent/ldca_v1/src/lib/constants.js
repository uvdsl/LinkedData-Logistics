"use strict";

function Namespace(namespace) {
  return thing => namespace.concat(thing);
}

// Namespaces
const FOAF = Namespace("http://xmlns.com/foaf/0.1/");
const DCT = Namespace("http://purl.org/dc/terms/");
const RDF = Namespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
const RDFS = Namespace("http://www.w3.org/2000/01/rdf-schema#");
const WDT = Namespace("http://www.wikidata.org/prop/direct/");
const WD = Namespace("http://www.wikidata.org/entity/");
const LDP = Namespace("http://www.w3.org/ns/ldp#");
const AS = Namespace("https://www.w3.org/ns/activitystreams#");
const XSD = Namespace("http://www.w3.org/2001/XMLSchema#");
const ETHON = Namespace("http://ethon.consensys.net/");
const PDGR = Namespace("http://purl.org/pedigree#");
const LDCV = Namespace(
  "http://people.aifb.kit.edu/co1683/2019/ld-chain/vocab#"
);

module.exports = {
  FOAF,
  DCT,
  RDF,
  RDFS,
  WD,
  WDT,
  LDP,
  AS,
  XSD,
  ETHON,
  PDGR,
  LDCV
};
