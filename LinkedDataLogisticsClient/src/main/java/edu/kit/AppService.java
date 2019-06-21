package edu.kit;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bouncycastle.util.Arrays;
import org.semanticweb.yars.nx.Node;
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

	private static RWLDclient rwldClient = null;
	private static Web3jClient web3jClient = null;

	public AppService() {
		System.out.println(" [INFO] Starting Service ...");
		rwldClient = new RWLDclient();
	}

	/**
	 * Switch to another wallet.
	 * 
	 * @param wallet_pass password to wallet
	 * @param wallet_home path to wallet file
	 * @throws CipherException when password is incorrect
	 * @throws IOException     when wallet file could not be found
	 */
	public void switchWallets(String wallet_pass, String wallet_home) throws IOException, CipherException {
		web3jClient = new Web3jClient(wallet_pass, wallet_home);
		System.out.println(" [INFO] Thank you and welcome home: " + web3jClient.getWalletAddress() + "\n");
	}

	/**
	 * Store a hash via the Smart Contract. This method checks, if a previous
	 * Pedigree Part exists or not. If there exists a previous part, the
	 * pedigreePart is added. Else, the pedigreePart is registered as a new initial
	 * part.
	 * 
	 * @param pedigreePartURI
	 * @param newOwnerAddress
	 * @return gas usesd [Action performed]
	 */
	public String storeHash(String pedigreePartURI, String newOwnerAddress) {
		String result = null;

		if (web3jClient == null)
			App.switchWallets();

		try {
			byte[] hash = rwldClient.hashGraph(rwldClient.requestLinkedData(pedigreePartURI));
			String prevPartURI = getPreviousPartURI(pedigreePartURI);
			HashStore contract = web3jClient.loadContract(getContractAddress(pedigreePartURI));
			System.out.println("\n [INFO] Issuing transaction.");
			CompletableFuture<TransactionReceipt> transaction = null;
			if (prevPartURI == null) {
				// register a new initial LP part
				transaction = contract.initProduct(pedigreePartURI, hash, newOwnerAddress).sendAsync();
				System.out.println(" [INFO] Transaction pending ...");
				result = "Gas used: " + transaction.get().getGasUsed().toString() + " [Registration]";
			} else {
				// add a intermedate LP part
				transaction = contract.storeAndTransfer(prevPartURI, pedigreePartURI, hash, newOwnerAddress)
						.sendAsync();
				System.out.println(" [INFO] Transaction pending ...");
				result = "Gas used: " + transaction.get().getGasUsed().toString() + " [Appending]";
			}

		} catch (Exception e) {
			e.printStackTrace();
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
			System.err.println(
					((isInitial) ? " [INFO] Did in fact" : " [ERROR] Did not") + " reach initial Linked Pedigree part.");
			if (!isInitial) {
				try {
					// assume this part to be initial? Or look it up? (hence the negation.)
					isInitial = !App
							.requestConfirmation("\nLook up preceding Linked Pedigree part URI in Smart Contract?");
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
				// if still not assumed to be initial
				if (!isInitial) {
					if (web3jClient == null)
						App.switchWallets();
					try {
						result = web3jClient.loadContract(this.getContractAddress(pedigreePartURI))
								.getPreviousPedigreeURI(pedigreePartURI).send();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else if (prevPartList.size() > 1) {
			int i = 0;
			System.err.println("\n [ERROR] Found more than one preceding Linked Pedigree part:");
			for (Node potentialPrevPart : prevPartList) {
				System.err.println(" [INFO] \t(" + i++ + ") " + strip(potentialPrevPart.toString()));
			}
			String response = "0";
			try {
				response = App.request("\nPlease provide the index of the Linked Pedigree part to use:");
			} catch (IOException e) {
				e.printStackTrace();
			}
			i = Integer.valueOf(response);
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
		if (web3jClient == null)
			App.switchWallets();

		LinkedHashMap<String, Boolean> result = new LinkedHashMap<>();
		while (pedigreePartURI != null) {
			HashStore contract = web3jClient.loadContract(getContractAddress(pedigreePartURI));
			result.put(pedigreePartURI, isVerified(contract, pedigreePartURI));
			pedigreePartURI = getPreviousPartURI(pedigreePartURI);
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
		if (web3jClient == null)
			App.switchWallets();
		String result = null;
		try {
			HashStore contract = web3jClient.loadContract(getContractAddress(pedigreeURI));
			if (isVerified(contract, pedigreeURI)) {
				System.out.println("\n [INFO] Issuing transaction.");
				CompletableFuture<TransactionReceipt> transaction = contract.approve(pedigreeURI).sendAsync();
				System.out.println(" [INFO] Transaction pending ...");
				result = "Gas used: " + transaction.get().getGasUsed().toString() + " [Approval]";
			} else {
				result = " [ERROR] Hash check on approval was not successfull!\n" + " [INFO] Approval not possible.\n";
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
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
		if (web3jClient == null)
			App.switchWallets();
		StringBuilder result = new StringBuilder();
		String contractAddress = getContractAddress(pedigreeURI);
		HashStore contract = web3jClient.loadContract(contractAddress);
		result.append("next Owner\t:\t" + getOwner(contract, pedigreeURI) + "\n");
		result.append("Approved\t:\t" + getApprovalStatus(contract, pedigreeURI) + "\n");
		result.append("Prev Part\t:\t" + getPreviousPartURI(contract, pedigreeURI) + "\n");
		return result.toString();
	}

	/**
	 * Verifies a fresh generated hash against the contract hash of the input uri.
	 * 
	 * @param pedigreeURI
	 * @return
	 */
	public boolean isVerified(HashStore contract, String pedigreeURI) {
		if (web3jClient == null)
			App.switchWallets();
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
			e.printStackTrace();
		}
		return false;
	}

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
		if (web3jClient == null)
			App.switchWallets();
		try {
			return contract.getOwner(pedigreeURI).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean getApprovalStatus(HashStore contract, String pedigreeURI) {
		if (web3jClient == null)
			App.switchWallets();
		try {
			return contract.getApproval(pedigreeURI).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getPreviousPartURI(HashStore contract, String pedigreeURI) {
		if (web3jClient == null)
			App.switchWallets();
		try {
			return contract.getPreviousPedigreeURI(pedigreeURI).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
