package edu.kit.ontology;

public class LinkedOntology {
	// prefix
	private static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String uri_ontology = "http://people.aifb.kit.edu/co1683/2019/ld-chain/vocab#";
	private static final String ethon = "http://ethon.consensys.net/";
	private static final String pedigree = "http://purl.org/pedigree#";
	// predicates
	public static final String PREDICATE_TYPE = "<" + rdf + "type" + ">";
	public static final String PREDICATE_VALIDATION_INFO = "<" + uri_ontology + "hasValidationInfo" + ">";
	public static final String PREDICATE_CONTRACT_ACCOUNT = "<" + uri_ontology + "hasContractAccount" + ">";
	public static final String PREDICATE_CONTRACT_ADDRESS = "<" + ethon + "address" + ">";
	public static final String PREDICATE_PREVIOUS_PART = "<" + pedigree + "hasReceivedPedigree" + ">";
	public static final String PREDICATE_PEDIGREE_STATUS = "<" + pedigree + "hasPedigreeStatus" + ">";
	// objects
	public static final String OBJECT_PEDIGREE = "<" + pedigree + "Pedigree" + ">";
	public static final String OBJECT_INITIAL_PART = "<" + pedigree + "Initial" + ">";
	
}