package edu.kit;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.semanticweb.yars.nx.Nodes;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import edu.kit.eth.HashStore;
import edu.kit.eth.Web3jClient;
import edu.kit.linkedData.RWLDclient;
import edu.kit.ontology.LinkedOntology;

/**
 * 
 * @author Christoph
 *
 */
public class AppService {

	private RWLDclient rwldClient = null;
	private Web3jClient web3jClient = null;

	/**
	 * Start Linked Data client.
	 */
	public AppService() {
		System.out.println(" [INFO] Starting LinkedData-Chain Client ...");
		rwldClient = new RWLDclient();
	}

	/**
	 * login to a Ethereum wallet.
	 * 
	 * @param wallet_pass password to wallet
	 * @param wallet_home path to wallet file
	 * @throws CipherException when password is incorrect
	 * @throws IOException     when wallet file could not be found
	 */
	public void loginToWallet(String wallet_pass, String wallet_home) throws IOException, CipherException {
		web3jClient = new Web3jClient(wallet_pass, wallet_home);
		System.out.println(" [INFO] Thank you and welcome home: " + web3jClient.getWalletAddress() + "\n");
	}

	/**
	 * Store a hash via the Smart Contract. This method checks, if a previous
	 * Pedigree Part exists or not. If there exists a previous part, the
	 * pedigreePart is added. Else, the pedigreePart is registered as a new initial
	 * part.
	 * 
	 * @param pedigreeURI
	 * @param newOwnerAddress
	 * @return gas usesd [Action performed]
	 */
	public String storeHash(String pedigreeURI, String newOwnerAddress) {
		String result = null;
		try {
			byte[] hash = rwldClient.hashGraph(rwldClient.requestLinkedData(pedigreeURI));
			String prevPartURI = getPreviousPartURI(pedigreeURI);
			HashStore contract = web3jClient.loadContract(getContractAddress(pedigreeURI));
			CompletableFuture<TransactionReceipt> transaction = null;
			if (prevPartURI == null) {
				// register a new initial LP part
				checkURInotUsed(contract, pedigreeURI);
				System.out.println("\n [INFO] Issuing transaction.");
				transaction = contract.initProduct(pedigreeURI, hash, newOwnerAddress).sendAsync();
				System.out.println(" [INFO] Transaction pending ...");
				result = "Gas used: " + transaction.get().getGasUsed().toString() + " [Registration]";
			} else {
				// add a intermedate LP part
				checkAuthorisation(contract, prevPartURI);
				checkApproval(contract, prevPartURI);
				System.out.println("\n [INFO] Issuing transaction.");
				transaction = contract.storeAndTransfer(prevPartURI, pedigreeURI, hash, newOwnerAddress).sendAsync();
				System.out.println(" [INFO] Transaction pending ...");
				result = "Gas used: " + transaction.get().getGasUsed().toString() + " [Appending]";

			}

		} catch (InterruptedException | ExecutionException e) {
			System.err.println("\t" + e.getLocalizedMessage());
			result = " [INFO] Returning to main menu.";
		}
		return result;
	}

	/**
	 * Approve a pedigreeURI
	 * 
	 * @param pedigreeURI
	 * @return gas used
	 */
	public String approve(String pedigreeURI) {
		String result = null;
		try {
			HashStore contract = web3jClient.loadContract(getContractAddress(pedigreeURI));
			checkAuthorisation(contract, pedigreeURI);
			if (verfiedHash(contract, pedigreeURI)) {
				System.out.println("\n [INFO] Issuing transaction.");
				CompletableFuture<TransactionReceipt> transaction = contract.approve(pedigreeURI).sendAsync();
				System.out.println(" [INFO] Transaction pending ...");
				result = "Gas used: " + transaction.get().getGasUsed().toString() + " [Approval]";
			} else {
				result = " [ERROR] Hash check on approval was not successfull!\n" + " [INFO] Approval not possible.\n";
			}
			return result;
		} catch (Exception e) {
			System.err.println("\t" + e.getLocalizedMessage());
			result = " [INFO] Returning to main menu.";
		}
		return result;
	}

	/**
	 * Look up the smart contract address in the validation info.
	 * 
	 * @param pedigreePartURI
	 * @return String Smart Contract Address
	 */
	public String getContractAddress(String pedigreePartURI) {
		System.out.println("\nRequesting Contract Address " + pedigreePartURI + "\n");

		// get Validation Info
		List<Node> validationInfo = rwldClient.queryObjectForPredicate(pedigreePartURI,
				LinkedOntology.PREDICATE_VALIDATION_INFO);
		if (validationInfo.size() != 1) {
			System.err.println(" [WARN] validationInfo.size() != 1");
		}
		String validationURI = strip(validationInfo.get(0).toString());
		System.out.println(" [INFO] ValidationInfo-URI: " + validationURI);

		// get Contract Account
		List<Node> accounts = rwldClient.queryObjectForPredicate(validationURI,
				LinkedOntology.PREDICATE_CONTRACT_ACCOUNT);
		if (accounts.size() != 1) {
			System.err.println(" [WARN] accounts.size() != 1");
		}
		String contractAccount = strip(accounts.get(0).toString());
		System.out.println(" [INFO] SmartContract-Account: " + contractAccount);

		// get Contract Address
		List<Node> addresses = rwldClient.queryObjectForPredicate(contractAccount,
				LinkedOntology.PREDICATE_CONTRACT_ADDRESS);
		if (addresses.size() != 1) {
			System.err.println(" [WARN] addresses.size() != 1");
		}
		String contractAddress = strip(stripDataType(addresses.get(0).toString()));
		System.out.println(" [INFO] SmartContract-Address: " + contractAddress);

		return contractAddress;
	}

	/**
	 * Retrieve the previous part (URI) of the input pedigree part.
	 * 
	 * @param pedigreePartURI
	 * @return previous part URI
	 */
	private String getPreviousPartURI(String pedigreePartURI) {
		String result = null;
		List<Node> prevPartList = rwldClient.queryObjectForPredicate(pedigreePartURI,
				LinkedOntology.PREDICATE_PREVIOUS_PART);

		if (prevPartList == null) {
			System.err.println("\n [WARN] Did not find a preceding Linked Pedigree part.");
			boolean isInitial = isInitialPart(pedigreePartURI);
			System.err.println(((isInitial) ? " [INFO] Did in fact" : " [ERROR] Did not")
					+ " reach initial Linked Pedigree part.");
			if (!isInitial) {
				// assume this part to be initial? Or look it up? (hence the negation.)
				isInitial = !App.requestConfirmation("\nLook up preceding Linked Pedigree part URI in Smart Contract?");
				// if still not assumed to be initial
				if (!isInitial) {
					try {
						result = web3jClient.loadContract(this.getContractAddress(pedigreePartURI))
								.getPreviousPedigreeURI(pedigreePartURI).send();
					} catch (Exception e) {
						System.err.println("\t" + e.getMessage());
					}
				}
			}
		} else if (prevPartList.size() > 1) {
			int i = 0;
			System.err.println("\n [ERROR] Found more than one preceding Linked Pedigree part:");
			for (Node potentialPrevPart : prevPartList) {
				System.err.println(" [INFO] \t(" + i++ + ") " + strip(potentialPrevPart.toString()));
			}

			String response = App.request("\nPlease provide the index of the Linked Pedigree part to use:");
			i = (response != null) ? Integer.valueOf(response) : 0;
			result = prevPartList.get(i).toString();
		} else {
			result = prevPartList.get(0).toString();
		}
		if (result != null) {
			result = strip(result);
			System.out.println("\n [INFO] Found preceding Linked Pedigree part: " + result);
		}
		return result;
	}

	/**
	 * Check if the input pedigree part is the initial part.
	 * 
	 * @param pedigreePartURI
	 * @return boolean
	 */
	private boolean isInitialPart(String pedigreePartURI) {
		List<Node> type_objects = rwldClient.queryObjectForPredicate(pedigreePartURI,
				LinkedOntology.PREDICATE_PEDIGREE_STATUS);
		boolean isInitial = false;
		for (Node n : type_objects) {
			if (strip(n.toString()).equals(strip(LinkedOntology.OBJECT_INITIAL_PART))) {
				isInitial = true;
			}
		}
		return isInitial;
	}

	/**
	 * Retrieve the linked pedigree (URI : Content) by starting in the end of the
	 * linked pedigree chain.
	 * 
	 * @param pedigreePartURI last known Linked Pedigree part URI
	 * @return linked pedigree as mapping of URI to content
	 */
	public LinkedHashMap<String, String> fetchLinkedPedigree(String pedigreePartURI) {
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		while (pedigreePartURI != null) {
			result.put(pedigreePartURI, fetchContent(pedigreePartURI));
			pedigreePartURI = getPreviousPartURI(pedigreePartURI);
		}
		return result;
	}

	/**
	 * Retrieve the content of a linked pedigree parts's URI
	 * 
	 * @param pedigreePartURI
	 * @return its content
	 */
	String fetchContent(String pedigreePartURI) {
		Iterable<Node[]> linkedData = rwldClient.requestLinkedData(pedigreePartURI);
		StringBuilder sBuilder = new StringBuilder();
		for (Node[] nx : linkedData) {
			sBuilder.append(Nodes.toString(nx) + "\n");
		}
		return sBuilder.toString();
	}

	/**
	 * Recursively check the validity of the hashes stored via the smart contract
	 * against the hashes directly generated from the linked data retrieved from the
	 * linked pedigree's URIs.
	 * 
	 * @param pedigreePartURI
	 * @param contract
	 * @return list of mapping URI to boolean: true if hashes match (are correct and
	 *         correctly positioned)
	 */
	// get, hash, check : repeat
	public LinkedHashMap<String, Boolean> checkLinkedPedigree(String pedigreePartURI) {
		LinkedHashMap<String, Boolean> result = new LinkedHashMap<>();
		while (pedigreePartURI != null) {
			HashStore contract = web3jClient.loadContract(getContractAddress(pedigreePartURI));
			result.put(pedigreePartURI, verfiedHash(contract, pedigreePartURI));
			pedigreePartURI = getPreviousPartURI(pedigreePartURI);
		}
		return result;
	}

	/**
	 * Generate a syntactic hash of the linked data available at the input URI.
	 * 
	 * @param uri
	 * @return syntactic hash byte[]
	 */
	public byte[] hashURI(String pedigreeURI) {
		return rwldClient.hashGraph(rwldClient.requestLinkedData(pedigreeURI));
	}

	/**
	 * Look up owner and approval status on the blockchain.
	 * 
	 * @param uri
	 */
	public String getInfo(String pedigreeURI) {
		StringBuilder result = new StringBuilder();
		String contractAddress = getContractAddress(pedigreeURI);
		HashStore contract = web3jClient.loadContract(contractAddress);
		result.append("next Owner\t:\t" + getOwner(contract, pedigreeURI) + "\n");
		result.append("Approved\t:\t" + getApprovalStatus(contract, pedigreeURI) + "\n");
		result.append("Prev Part\t:\t" + getPreviousPartURI(contract, pedigreeURI) + "\n");
		return result.toString();
	}

	//
	// pre modifier checks
	//

	/**
	 * Verifies a fresh generated hash against the contract hash of the input uri.
	 * 
	 * @param pedigreeURI
	 * @return
	 */
	public boolean verfiedHash(HashStore contract, String pedigreeURI) {
		try {
			byte[] contractHash = contract.getHash(pedigreeURI).send();
			byte[] uriHash = rwldClient.hashGraph(rwldClient.requestLinkedData(pedigreeURI));
			boolean truth = Arrays.areEqual(contractHash, uriHash);
			if (!truth) {
				System.err.println("\n [WARN] Hash check was not successfull: " + pedigreeURI);
			} else {
				System.err.println("\n [INFO] Hash check was successfull: " + pedigreeURI);
			}
			return truth;
		} catch (Exception e) {
			System.err.println("\t" + e.getLocalizedMessage());
		}
		return false;
	}

	/**
	 * Checks if the current wallet is current owner of the input Link Pedigree
	 * 
	 * @param pedigreeURI
	 * @return
	 * @throws InterruptedException
	 */
	public boolean checkAuthorisation(HashStore contract, String pedigreeURI) throws InterruptedException {
		String wallet = web3jClient.getWalletAddress();
		String owner = getOwner(contract, pedigreeURI);
		boolean truth = owner.equals(wallet);
		if (!truth) {
			System.out.println("\n [WARN] User not authorised: " + pedigreeURI);
			System.out.println(" [WARN] Transaction will be rejected by Smart Contract.");
			if (!App.requestConfirmation("Proceed anyway? (Will result in failing transaction: 0x0 (out of gas?)")) {
				throw new InterruptedException("\n [INFO] Aborting transaction.");
			}
		} else {
			System.err.println("\n [INFO] User authorised: " + pedigreeURI);
		}
		return truth;
	}

	/**
	 * Checks if the input Link Pedigree has been approved.
	 * 
	 * @param pedigreeURI
	 * @return
	 * @throws InterruptedException
	 */
	public boolean checkApproval(HashStore contract, String pedigreeURI) throws InterruptedException {
		boolean truth = getApprovalStatus(contract, pedigreeURI);
		if (!truth) {
			System.out.println("\n [WARN] Not approved: " + pedigreeURI);
			System.out.println(" [WARN] Transaction will be rejected by Smart Contract.");
			if (!App.requestConfirmation("Proceed anyway? (Will result in failing transaction: 0x0 (out of gas?)")) {
				throw new InterruptedException("\n [INFO] Aborting transaction.");
			}
		} else {
			System.err.println("\n [INFO] Approved: " + pedigreeURI);
		}
		return truth;
	}

	/**
	 * Checks if the input Link Pedigree has been already been used.
	 * 
	 * @param pedigreeURI
	 * @return
	 * @throws InterruptedException
	 */
	public boolean checkURInotUsed(HashStore contract, String pedigreeURI) throws InterruptedException {
		String owner = getOwner(contract, pedigreeURI);
		boolean truth = owner.equals("0x0000000000000000000000000000000000000000");
		if (!truth) {
			System.out.println("\n [WARN] Already used: " + pedigreeURI);
			System.out.println(" [WARN] Transaction will be rejected by Smart Contract.");
			if (!App.requestConfirmation("Proceed anyway? (Will result in failing transaction: 0x0 (out of gas?)")) {
				throw new InterruptedException("\n [INFO] Aborting transaction.");
			}
		} else {
			System.err.println("\n [INFO] Using: " + pedigreeURI);
		}
		return truth;
	}

	//
	// other
	//

	/**
	 * Strip URIs, remove starting or ending ",<,>
	 * 
	 * @param uri
	 * @return stripped string
	 */
	private String strip(String uri) {
		if (uri.startsWith("<") || uri.startsWith("\"")) {
			uri = uri.substring(1);
		}
		if (uri.endsWith(">") || uri.endsWith("\"")) {
			uri = uri.substring(0, uri.length() - 1);
		}
		return uri;
	}

	private String stripDataType(String uri) {
		return uri.substring(0, uri.indexOf("^^"));
	}

	//
	// DEV FUNCTIONS
	//
	public String getOwner(HashStore contract, String pedigreeURI) {
		String result;
		try {
			result = contract.getOwner(pedigreeURI).send();
		} catch (Exception e) {
			System.err.println("\t" + e.getMessage());
			result = " [INFO] Returning to main menu.";
		}
		return result;
	}

	public boolean getApprovalStatus(HashStore contract, String pedigreeURI) {
		try {
			return contract.getApproval(pedigreeURI).send();
		} catch (Exception e) {
			System.err.println("\t" + e.getMessage());
		}
		return false;
	}

	public String getPreviousPartURI(HashStore contract, String pedigreeURI) {
		String result;
		try {
			return contract.getPreviousPedigreeURI(pedigreeURI).send();
		} catch (Exception e) {
			System.err.println("\t" + e.getMessage());
			result = " [INFO] Returning to main menu.";
		}
		return result;
	}

}