package edu.kit.ontology;

public class LinkedOntology {
	private static final String uri_ontology = "http://www.student.kit.edu/~uvdsl/ontologies/linkedPedigree.ttl#";
	public static final String PREDICATE_CONTRACT_ADDRESS = "<" + uri_ontology + "verifiedBy" + ">";
	public static final String PREDICATE_PREVIOUS_PART = "<" + uri_ontology + "previousPart" + ">";
	public static final String OBJECT_BIRTH_CERT = "<" + uri_ontology + "BirthCertificate" + ">";
	public static final String PREDICATE_TYPE = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
}